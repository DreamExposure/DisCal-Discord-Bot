package org.dreamexposure.discal.core.`object`.event

import discord4j.common.util.Snowflake
import discord4j.core.DiscordClient
import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.entity.Role
import discord4j.core.spec.EmbedCreateSpec
import discord4j.discordjson.json.GuildData
import discord4j.discordjson.json.GuildUpdateData
import discord4j.discordjson.json.MessageData
import discord4j.rest.http.client.ClientException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.dreamexposure.discal.core.`object`.BotSettings
import org.dreamexposure.discal.core.`object`.GuildSettings
import org.dreamexposure.discal.core.entities.Event
import org.dreamexposure.discal.core.enums.time.DiscordTimestampFormat.LONG_DATETIME
import org.dreamexposure.discal.core.extensions.asDiscordTimestamp
import org.dreamexposure.discal.core.extensions.discord4j.getCalendar
import org.dreamexposure.discal.core.extensions.discord4j.getSettings
import org.dreamexposure.discal.core.extensions.embedFieldSafe
import org.dreamexposure.discal.core.extensions.toMarkdown
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.serializers.SnowflakeAsStringSerializer
import org.dreamexposure.discal.core.utils.GlobalVal
import org.dreamexposure.discal.core.utils.getEmbedMessage
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.function.TupleUtils
import java.util.concurrent.CopyOnWriteArrayList

@Serializable
data class RsvpData(
    @Serializable(with = SnowflakeAsStringSerializer::class)
    @SerialName("guild_id")
    val guildId: Snowflake,
    @SerialName("event_id")
    val eventId: String,
    @SerialName("calendar_number")
    val calendarNumber: Int = 1,
) {
    @SerialName("event_end")
    var eventEnd: Long = 0

    var limit: Int = -1
        set(value) {
            field = value.coerceAtLeast(-1)
        }

    @Serializable(with = SnowflakeAsStringSerializer::class)
    @SerialName("role_id")
    var roleId: Snowflake? = null
        private set

    @SerialName("on_time")
    val goingOnTime: MutableList<String> = CopyOnWriteArrayList()

    @SerialName("late")
    val goingLate: MutableList<String> = CopyOnWriteArrayList()

    @SerialName("not_going")
    val notGoing: MutableList<String> = CopyOnWriteArrayList()

    val undecided: MutableList<String> = CopyOnWriteArrayList()

    val waitlist: MutableList<String> = CopyOnWriteArrayList()

    fun getCurrentCount() = this.goingOnTime.size + this.goingLate.size

    fun hasRoom(userId: String): Boolean {
        return if (limit < 0 || getCurrentCount() + 1 <= limit) true
        //Check if they are in a list that counts toward limit, if true, that means they will fit in the event
        else goingOnTime.contains(userId) || goingLate.contains(userId)
    }

    private fun hasRoom() = limit < 0 || getCurrentCount() + 1 <= limit

    fun setRole(id: Snowflake, client: DiscordClient): Mono<Void> {
        roleId = id

        return Mono.just(client.getGuildById(guildId)).flatMap { guild ->
            val addOnTimeRoles = Flux.fromIterable(goingOnTime).flatMap { id ->
                guild.addMemberRole(Snowflake.of(id), roleId, "Role added to RSVP for event with ID: $eventId")
            }.then()
            val addLateRole = Flux.fromIterable(goingLate).flatMap { id ->
                guild.addMemberRole(Snowflake.of(id), roleId, "Role added to RSVP for event with ID: $eventId")
            }.then()

            Mono.`when`(addOnTimeRoles, addLateRole)
        }

    }

    fun setRole(role: Role) = setRole(role.id, role.client.rest())

    fun setRole(id: Snowflake?) {
        roleId = id
    }

    fun clearRole(client: DiscordClient): Mono<Void> {
        //Attempt to remove the role from all users RSVP'd...
        return Mono.just(client.getGuildById(guildId)).flatMap { guild ->
            val removeOnTimeRoles = Flux.fromIterable(goingOnTime).flatMap { id ->
                guild.removeMemberRole(Snowflake.of(id), roleId, "Role removed from event with ID: $eventId")
            }.then()
            val removeLateRole = Flux.fromIterable(goingLate).flatMap { id ->
                guild.addMemberRole(Snowflake.of(id), roleId, "Role removed from event with ID: $eventId")
            }

            Mono.`when`(removeOnTimeRoles, removeLateRole).doFinally { this.setRole(null) }
        }
    }

    //Functions
    fun removeCompletely(userId: String, client: DiscordClient, doWaitlistOp: Boolean = false): Mono<Void> {
        // Remove from all lists
        goingOnTime.removeAll { userId == it }
        goingLate.removeAll { userId == it }
        notGoing.removeAll { userId == it }
        undecided.removeAll { userId == it }
        waitlist.removeAll { userId == it }

        // Remove role if one is set
        val roleMono = if (roleId != null) {
            removeRole(userId, roleId!!, "Removed RSVP to event with ID $eventId", client)
        } else {
            Mono.empty()
        }

        // If there is now room, add the next waiting user as going
        val waitListMono = if (doWaitlistOp && waitlist.isNotEmpty() && hasRoom(waitlist.first())) {
            handleWaitListedUser(waitlist.removeFirst(), client)
        } else {
            Mono.empty()
        }

        return roleMono.then(waitListMono)
    }

    fun removeCompletely(member: Member, doWaitlistOp: Boolean = false): Mono<Void> =
        removeCompletely(member.id.asString(), member.client.rest(), doWaitlistOp)

    fun addGoingOnTime(userId: String, client: DiscordClient): Mono<Void> {
        return Mono.just(userId)
            .doOnNext(goingOnTime::add)
            .flatMap {
                if (roleId != null) {
                    addRole(it, roleId!!, "RSVP'd to event with ID: $eventId", client)
                } else Mono.empty()
            }
    }

    fun addGoingOnTime(member: Member): Mono<Void> = addGoingOnTime(member.id.asString(), member.client.rest())

    fun addGoingLate(userId: String, client: DiscordClient): Mono<Void> {
        return Mono.just(userId)
            .doOnNext(goingLate::add)
            .flatMap {
                if (roleId != null) {
                    addRole(it, roleId!!, "RSVP'd to event with ID: $eventId", client)
                } else Mono.empty()
            }
    }

    fun handleWaitListedUser(userId: String, client: DiscordClient): Mono<Void> {
        val guild = client.getGuildById(guildId)
        val eventMono = guild.getCalendar(calendarNumber).flatMap { it.getEvent(eventId) }
        val guildDataMono = guild.data
        val settingsMono = guild.getSettings()

        val embedMono = Mono.zip(guildDataMono, settingsMono, eventMono).map(
            TupleUtils.function { data, settings, event ->
                followupEmbed(data, settings, userId, event)
            }
        )

        /* Add the user as attending on time
        (it would be rude to show up late if other people want to attend the full event)
        */
        return addGoingOnTime(userId, client).then(embedMono).flatMap {
            dmUser(userId, it, client)
        }.then()
    }

    fun handleWaitListedUser(member: Member): Mono<Void> = handleWaitListedUser(member.id.asString(), member.client.rest())

    fun fillRemaining(guild: Guild, settings: GuildSettings): Mono<RsvpData> {
        val eventMono = guild.getCalendar(calendarNumber).flatMap { it.getEvent(eventId) }.cache()

        return Flux.fromIterable(waitlist)
            .takeWhile { hasRoom() }
            .concatMap { userId ->
                /* Add the user as attending on time
                (it would be rude to show up late if other people want to attend the full event)
                */
                addGoingOnTime(userId, guild.client.rest()).then(eventMono).flatMap { event ->
                    // Send DM
                    val embed = followupEmbed(guild.data, settings, userId, event)
                    dmUser(userId, embed, guild.client.rest())
                }
            }.doOnError {
                LOGGER.error(GlobalVal.DEFAULT, "RSVP waitlist processing failed", it)
            }.onErrorResume {
                Mono.empty()
            }.then().thenReturn(this)
    }

    fun addGoingLate(member: Member): Mono<Void> = addGoingLate(member.id.asString(), member.client.rest())

    fun shouldBeSaved(): Boolean {
        return this.goingOnTime.isNotEmpty()
            || this.goingLate.isNotEmpty()
            || this.notGoing.isNotEmpty()
            || this.undecided.isNotEmpty()
            || this.waitlist.isNotEmpty()
            || limit != -1
            || roleId != null
    }

    private fun addRole(userId: String, roleId: Snowflake, reason: String, client: DiscordClient): Mono<Void> {
        return client.getGuildById(this.guildId)
            .addMemberRole(Snowflake.of(userId), roleId, reason)
            .onErrorResume(ClientException::class.java) { Mono.empty() }
    }

    private fun removeRole(userId: String, roleId: Snowflake, reason: String, client: DiscordClient): Mono<Void> {
        return client.getGuildById(this.guildId)
            .removeMemberRole(Snowflake.of(userId), roleId, reason)
            .onErrorResume(ClientException::class.java) { Mono.empty() }
    }

    private fun followupEmbed(guild: GuildUpdateData, settings: GuildSettings, userId: String, event: Event): EmbedCreateSpec {
        val iconUrl = if (guild.icon().isPresent)
            "${GlobalVal.discordCdnUrl}/icons/${guild.id().asString()}/${guild.icon().get()}.png"
        else GlobalVal.iconUrl

        val builder = EmbedCreateSpec.builder()
            // Even without branding enabled, we want the user to know what guild this is because it's in DMs
            .author(guild.name(), BotSettings.BASE_URL.get(), iconUrl)
            .title(getEmbedMessage("rsvp", "waitlist.title", settings))
            .description(getEmbedMessage("rsvp", "waitlist.desc", settings, userId, event.name, event.eventId))
            .addField(
                getEmbedMessage("rsvp", "waitlist.field.start", settings),
                event.start.asDiscordTimestamp(LONG_DATETIME),
                true
            ).addField(
                getEmbedMessage("rsvp", "waitlist.field.end", settings),
                event.end.asDiscordTimestamp(LONG_DATETIME),
                true
            ).footer(getEmbedMessage("rsvp", "waitlist.footer", settings, event.eventId), null)

        if (event.location.isNotBlank()) builder.addField(
            getEmbedMessage("rsvp", "waitlist.field.location", settings),
            event.location.toMarkdown().embedFieldSafe(),
            false
        )

        if (event.image.isNotBlank()) builder.thumbnail(event.image)


        return builder.build()
    }

    private fun followupEmbed(guild: GuildData, settings: GuildSettings, userId: String, event: Event): EmbedCreateSpec {
        val iconUrl = if (guild.icon().isPresent)
            "${GlobalVal.discordCdnUrl}/icons/${guild.id().asString()}/${guild.icon().get()}.png"
        else GlobalVal.iconUrl

        val builder = EmbedCreateSpec.builder()
            // Even without branding enabled, we want the user to know what guild this is because it's in DMs
            .author(guild.name(), BotSettings.BASE_URL.get(), iconUrl)
            .title(getEmbedMessage("rsvp", "waitlist.title", settings))
            .description(getEmbedMessage("rsvp", "waitlist.desc", settings, userId, event.name, event.eventId))
            .addField(
                getEmbedMessage("rsvp", "waitlist.field.start", settings),
                event.start.asDiscordTimestamp(LONG_DATETIME),
                true
            ).addField(
                getEmbedMessage("rsvp", "waitlist.field.end", settings),
                event.end.asDiscordTimestamp(LONG_DATETIME),
                true
            ).footer(getEmbedMessage("rsvp", "waitlist.footer", settings, event.eventId), null)

        if (event.location.isNotBlank()) builder.addField(
            getEmbedMessage("rsvp", "waitlist.field.location", settings),
            event.location.toMarkdown().embedFieldSafe(),
            false
        )

        if (event.image.isNotBlank()) builder.thumbnail(event.image)


        return builder.build()
    }

    private fun dmUser(userId: String, embedCreateSpec: EmbedCreateSpec, client: DiscordClient): Mono<MessageData> {
        return client.getUserById(Snowflake.of(userId)).privateChannel.flatMap { channelData ->
            client.getChannelById(Snowflake.of(channelData.id())).createMessage(embedCreateSpec.asRequest())
        }.doOnError {
            LOGGER.error("Failed to DM user for RSVP Followup", it)
        }.onErrorResume {
            Mono.empty()
        }
    }
}
