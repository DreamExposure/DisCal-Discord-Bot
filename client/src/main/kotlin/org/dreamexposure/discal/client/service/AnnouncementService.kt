package org.dreamexposure.discal.client.service

import discord4j.common.util.Snowflake
import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.entity.Role
import discord4j.core.`object`.entity.channel.GuildMessageChannel
import discord4j.core.spec.MessageCreateSpec
import discord4j.rest.http.client.ClientException
import io.netty.handler.codec.http.HttpResponseStatus
import org.dreamexposure.discal.client.DisCalClient
import org.dreamexposure.discal.client.message.embed.AnnouncementEmbed
import org.dreamexposure.discal.core.`object`.announcement.Announcement
import org.dreamexposure.discal.core.`object`.announcement.AnnouncementCache
import org.dreamexposure.discal.core.database.DatabaseManager
import org.dreamexposure.discal.core.entities.Calendar
import org.dreamexposure.discal.core.entities.Event
import org.dreamexposure.discal.core.enums.announcement.AnnouncementModifier
import org.dreamexposure.discal.core.enums.announcement.AnnouncementType.*
import org.dreamexposure.discal.core.extensions.discord4j.getCalendar
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.utils.GlobalVal
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.function.TupleUtils
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

@Component
class AnnouncementService : ApplicationRunner {
    private val maxDifferenceMs = Duration.ofMinutes(5).toMillis()

    private val cached = ConcurrentHashMap<Snowflake, AnnouncementCache>()

    // Start
    override fun run(args: ApplicationArguments?) {
        Flux.interval(Duration.ofMinutes(5))
            .onBackpressureBuffer()
            .flatMap { doAnnouncementCycle() }
            .doOnError { LOGGER.error(GlobalVal.DEFAULT, "!-Announcement run error-!", it) }
            .subscribe()
    }

    // Runner
    private fun doAnnouncementCycle(): Mono<Void> {
        //TODO: This should come in through DI once other legacy is removed/rewritten
        if (DisCalClient.client == null) return Mono.empty()

        return DisCalClient.client!!.guilds.flatMap { guild ->
            DatabaseManager.getEnabledAnnouncements(guild.id)
                .flatMapMany { Flux.fromIterable(it) }
                .flatMap { announcement ->
                    when (announcement.modifier) {
                        AnnouncementModifier.BEFORE -> handleBeforeModifier(guild, announcement)
                        AnnouncementModifier.DURING -> handleDuringModifier(guild, announcement)
                        AnnouncementModifier.END -> handleEndModifier(guild, announcement)
                    }
                }.doOnError {
                    LOGGER.error(GlobalVal.DEFAULT, "Announcement error", it)
                }.onErrorResume { Mono.empty() }
        }.doOnError {
            LOGGER.error(GlobalVal.DEFAULT, "Announcement error", it)
        }.onErrorResume {
            Mono.empty()
        }.doFinally {
            cached.clear()
        }.then()
    }

    // Modifier handling
    private fun handleBeforeModifier(guild: Guild, announcement: Announcement): Mono<Void> {
        when (announcement.type) {
            SPECIFIC -> {
                return getCalendar(guild, announcement)
                    .flatMap { it.getEvent(announcement.eventId) }
                      //Event announcement is tied to was deleted -- This should now be handled at a lower level
                    //.switchIfEmpty(DatabaseManager.deleteAnnouncement(announcement.id.toString()).then(Mono.empty()))
                    .filterWhen { isInRange(announcement, it) }
                    .flatMap { sendAnnouncement(guild, announcement, it) }
                      // Delete specific announcement after posted
                    .flatMap { DatabaseManager.deleteAnnouncement(announcement.id.toString()) }
                    .then()
            }
            UNIVERSAL -> {
                return getEvents(guild, announcement)
                    .filterWhen { isInRange(announcement, it) }
                    .flatMap { sendAnnouncement(guild, announcement, it) }
                    .then()
            }
            COLOR -> {
                return getEvents(guild, announcement)
                    .filter { it.color == announcement.eventColor }
                    .filterWhen { isInRange(announcement, it) }
                    .flatMap { sendAnnouncement(guild, announcement, it) }
                    .then()
            }
            RECUR -> {
                return getEvents(guild, announcement)
                    .filter { it.eventId.contains("_") && it.eventId.split("_")[0] == announcement.eventId }
                    .filterWhen { isInRange(announcement, it) }
                    .flatMap { sendAnnouncement(guild, announcement, it) }
                    .then()
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun handleDuringModifier(guild: Guild, announcement: Announcement): Mono<Void> {
        //TODO: Not yet implemented

        return Mono.empty()
    }

    @Suppress("UNUSED_PARAMETER")
    private fun handleEndModifier(guild: Guild, announcement: Announcement): Mono<Void> {
        //TODO: Not yet implemented

        return Mono.empty()
    }

    // Utility
    private fun isInRange(announcement: Announcement, event: Event): Mono<Boolean> {
        val announcementTime = Duration
            .ofHours(announcement.hoursBefore.toLong())
            .plusMinutes(announcement.minutesBefore.toLong())
            .toMillis()
        val timeUntilEvent = event.start.minusMillis(System.currentTimeMillis()).toEpochMilli()

        val difference = timeUntilEvent - announcementTime

        if (difference < 0) {
            //event past, delete if specific type
            if (announcement.type == SPECIFIC) {
                return DatabaseManager.deleteAnnouncement(announcement.id.toString())
                    .thenReturn(false)
            }
            return Mono.just(false)
        } else return Mono.just(difference <= maxDifferenceMs)
    }

    private fun sendAnnouncement(guild: Guild, announcement: Announcement, event: Event): Mono<Message> {
        val embedMono = AnnouncementEmbed.determine(announcement, event, guild)
        val mentionsMono = buildMentions(guild, announcement).onErrorReturn("")

        return guild.getChannelById(Snowflake.of(announcement.announcementChannelId))
            .ofType(GuildMessageChannel::class.java)
            .flatMap { channel ->
                Mono.zip(embedMono, mentionsMono).flatMap(TupleUtils.function { embed, mentions ->
                    if (mentions.isEmpty())
                        return@function channel.createMessage(embed)
                    else
                        return@function channel.createMessage(
                            MessageCreateSpec.builder()
                                .content(mentions)
                                .addEmbed(embed)
                                .build()
                        )
                }).flatMap { message ->
                    if (announcement.publish) {
                        message.publish()
                    } else Mono.just(message)
                }
            }.onErrorResume(ClientException::class.java) {
                Mono.just(it)
                    .filter(HttpResponseStatus.NOT_FOUND::equals)
                      // Channel announcement should post to was deleted
                    .flatMap { DatabaseManager.deleteAnnouncement(announcement.id.toString()) }
                    .then(Mono.empty())
            }
    }

    private fun buildMentions(guild: Guild, announcement: Announcement): Mono<String> {
        val userMentions = Flux.fromIterable(announcement.subscriberUserIds)
            .flatMap { guild.getMemberById(Snowflake.of(it)) }
            .map { it.nicknameMention }
            .onErrorReturn("")
            .collectList()
            .defaultIfEmpty(listOf())

        val roleMentions = Flux.fromIterable(announcement.subscriberRoleIds)
            .flatMap {
                if (it.equals("everyone", true)) guild.everyoneRole.map(Role::getMention)
                else if (it.equals("here", true)) Mono.just("here")
                else guild.getRoleById(Snowflake.of(it)).map(Role::getMention)
            }
            .onErrorReturn("")
            .collectList()
            .defaultIfEmpty(listOf())

        return Mono.zip(userMentions, roleMentions).map(TupleUtils.function { users, roles ->
            if (users.isEmpty() && roles.isEmpty()) ""
            else {
                val mentions = StringBuilder()

                mentions.append("Subscribers: ")

                for (u in users) mentions.append("$u ")
                for (r in roles) mentions.append("$r ")

                mentions.toString()
            }
        })
    }

    // Cache things
    private fun getCalendar(guild: Guild, announcement: Announcement): Mono<Calendar> {
        val cached = getCached(announcement.guildId)

        return if (!cached.calendars.contains(announcement.calendarNumber)) {
            guild.getCalendar(announcement.calendarNumber)
                .doOnNext { cached.calendars[it.calendarNumber] = it }
        } else Mono.justOrEmpty(cached.calendars[announcement.calendarNumber])
    }

    private fun getEvents(guild: Guild, announcement: Announcement): Flux<Event> {
        val cached = getCached(announcement.guildId)

        return if (!cached.events.contains(announcement.calendarNumber)) {
            getCalendar(guild, announcement).flatMapMany {
                it.getUpcomingEvents(20).cache()
            }
        } else cached.events[announcement.calendarNumber]!!
    }

    private fun getCached(guildId: Snowflake): AnnouncementCache {
        if (!cached.contains(guildId))
            cached[guildId] = AnnouncementCache(guildId)

        return cached[guildId]!!
    }
}
