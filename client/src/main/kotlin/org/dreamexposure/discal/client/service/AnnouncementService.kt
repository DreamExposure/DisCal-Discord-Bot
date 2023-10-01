package org.dreamexposure.discal.client.service

import discord4j.common.util.Snowflake
import discord4j.core.GatewayDiscordClient
import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.entity.channel.GuildMessageChannel
import discord4j.core.spec.MessageCreateSpec
import discord4j.rest.http.client.ClientException
import io.netty.handler.codec.http.HttpResponseStatus
import org.dreamexposure.discal.client.message.embed.AnnouncementEmbed
import org.dreamexposure.discal.core.database.DatabaseManager
import org.dreamexposure.discal.core.entities.Calendar
import org.dreamexposure.discal.core.entities.Event
import org.dreamexposure.discal.core.enums.announcement.AnnouncementModifier
import org.dreamexposure.discal.core.enums.announcement.AnnouncementType.*
import org.dreamexposure.discal.core.extensions.discord4j.getCalendar
import org.dreamexposure.discal.core.extensions.messageContentSafe
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.`object`.announcement.Announcement
import org.dreamexposure.discal.core.`object`.announcement.AnnouncementCache
import org.dreamexposure.discal.core.utils.GlobalVal
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

@Component
class AnnouncementService(
    private val discordClient: GatewayDiscordClient
) : ApplicationRunner {
    private val maxDifferenceMs = Duration.ofMinutes(5).toMillis()

    private val cached = ConcurrentHashMap<Snowflake, AnnouncementCache>()

    // Start
    override fun run(args: ApplicationArguments?) {
        Flux.interval(Duration.ofMinutes(5))
            .onBackpressureDrop()
            .flatMap { doAnnouncementCycle() }
            .doOnError { LOGGER.error(GlobalVal.DEFAULT, "!-Announcement run error-!", it) }
            .subscribe()
    }

    // Runner
    private fun doAnnouncementCycle(): Mono<Void> {
        return discordClient.guilds.flatMap { guild ->
            DatabaseManager.getEnabledAnnouncements(guild.id).flatMapMany { Flux.fromIterable(it) }.flatMap { announcement ->
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
        /*
        // Get announcements for this shard, then group by guild to make caching easier
        return DatabaseManager.getAnnouncementsForShard(Application.getShardCount(), getShardIndex().toInt()).map { list ->
            list.groupBy { it.guildId }
        }.flatMapMany { groupedAnnouncements ->
            Flux.fromIterable(groupedAnnouncements.entries).flatMap { entry ->
                DisCalClient.client!!.getGuildById(entry.key).flatMapMany { guild ->
                    val announcements = groupedAnnouncements[guild.id] ?: emptyList()

                    Flux.fromIterable(announcements).flatMap { announcement ->
                        when (announcement.modifier) {
                            AnnouncementModifier.BEFORE -> handleBeforeModifier(guild, announcement)
                            AnnouncementModifier.DURING -> handleDuringModifier(guild, announcement)
                            AnnouncementModifier.END -> handleEndModifier(guild, announcement)
                        }
                    }.doOnError {
                        LOGGER.error(GlobalVal.DEFAULT, "Announcement error", it)
                    }.onErrorResume { Mono.empty() }
                }.onErrorResume(ClientException.isStatusCode(403)) {
                    //FIXME: great way to wipe the database, not sure what the fuck happened here.
                    // DisCal is no longer in the guild, remove all it's from the database

                    //DatabaseManager.deleteAllDataForGuild(entry.key).then()
                    Mono.empty()
                }
            }
        */
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
                    .flatMap { DatabaseManager.deleteAnnouncement(announcement.id) }
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
                return DatabaseManager.deleteAnnouncement(announcement.id)
                    .thenReturn(false)
            }
            return Mono.just(false)
        } else return Mono.just(difference <= maxDifferenceMs)
    }

    private fun sendAnnouncement(guild: Guild, announcement: Announcement, event: Event): Mono<Message> {
        return guild.getChannelById(Snowflake.of(announcement.announcementChannelId))
            .ofType(GuildMessageChannel::class.java)
            .flatMap { channel ->
                AnnouncementEmbed.determine(announcement, event, guild).flatMap { embed ->
                    channel.createMessage(
                        MessageCreateSpec.builder()
                            .content(announcement.buildMentions().messageContentSafe())
                            .addEmbed(embed)
                            .build()
                    )
                }.flatMap { message ->
                    if (announcement.publish) {
                        message.publish()
                    } else Mono.just(message)
                }
            }.onErrorResume(ClientException::class.java) {
                Mono.just(it)
                    .filter(HttpResponseStatus.NOT_FOUND::equals)
                    // Channel announcement should post to was deleted
                    .flatMap { DatabaseManager.deleteAnnouncement(announcement.id) }
                    .then(Mono.empty())
            }
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
        if (cached.events.contains(announcement.calendarNumber))
            return Flux.fromIterable(cached.events[announcement.calendarNumber]!!)

        return getCalendar(guild, announcement).flatMapMany {
            it.getUpcomingEvents(20)
        }.collectList()
            .doOnNext { cached.events[announcement.calendarNumber] = it }
            .flatMapIterable { it }
    }

    private fun getCached(guildId: Snowflake): AnnouncementCache {
        if (!cached.contains(guildId))
            cached[guildId] = AnnouncementCache(guildId)

        return cached[guildId]!!
    }
}
