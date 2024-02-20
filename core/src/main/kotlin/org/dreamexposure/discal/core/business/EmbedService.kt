package org.dreamexposure.discal.core.business

import discord4j.common.util.Snowflake
import discord4j.core.DiscordClient
import discord4j.core.spec.EmbedCreateSpec
import kotlinx.coroutines.reactor.awaitSingle
import org.dreamexposure.discal.core.config.Config
import org.dreamexposure.discal.core.entities.Event
import org.dreamexposure.discal.core.enums.time.DiscordTimestampFormat
import org.dreamexposure.discal.core.extensions.asDiscordTimestamp
import org.dreamexposure.discal.core.extensions.discord4j.getCalendar
import org.dreamexposure.discal.core.extensions.discord4j.getSettings
import org.dreamexposure.discal.core.extensions.embedFieldSafe
import org.dreamexposure.discal.core.extensions.toMarkdown
import org.dreamexposure.discal.core.`object`.GuildSettings
import org.dreamexposure.discal.core.`object`.new.Rsvp
import org.dreamexposure.discal.core.utils.GlobalVal
import org.dreamexposure.discal.core.utils.getCommonMsg
import org.dreamexposure.discal.core.utils.getEmbedMessage
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.getBean
import org.springframework.stereotype.Component

@Component
class EmbedService(
    private val beanFactory: BeanFactory,
) {
    private val discordClient: DiscordClient
        get() = beanFactory.getBean()


    private suspend fun defaultEmbedBuilder(settings: GuildSettings): EmbedCreateSpec.Builder {
        val guild = discordClient.getGuildById(settings.guildID).data.awaitSingle()

        val iconUrl = if (settings.branded && guild.icon().isPresent)
            "${GlobalVal.discordCdnUrl}/icons/${settings.guildID.asString()}/${guild.icon().get()}.png"
        else GlobalVal.iconUrl

        return EmbedCreateSpec.builder()
            .author(
                if (settings.branded) guild.name() else getCommonMsg("bot.name", settings),
                Config.URL_BASE.getString(),
                iconUrl
            )
    }

    suspend fun rsvpDmFollowupEmbed(rsvp: Rsvp, userId: Snowflake): EmbedCreateSpec {
        // TODO: These will be replaced by service calls eventually as I migrate components over to new patterns
        val restGuild = discordClient.getGuildById(rsvp.guildId)
        val guildData = restGuild.data.awaitSingle()
        val guildSettings = restGuild.getSettings().awaitSingle()
        val event = restGuild.getCalendar(rsvp.calendarNumber)
            .flatMap { it.getEvent(rsvp.eventId) }.awaitSingle()


        val iconUrl = if (guildData.icon().isPresent)
            "${GlobalVal.discordCdnUrl}/icons/${rsvp.guildId.asString()}/${guildData.icon().get()}.png"
        else GlobalVal.iconUrl

        val builder = EmbedCreateSpec.builder()
            // Even without branding enabled, we want the user to know what guild this is because it's in DMs
            .author(guildData.name(), Config.URL_BASE.getString(), iconUrl)
            .title(getEmbedMessage("rsvp", "waitlist.title", guildSettings))
            .description(getEmbedMessage("rsvp", "waitlist.desc", guildSettings, userId.asString(), event.name))
            .addField(
                getEmbedMessage("rsvp", "waitlist.field.start", guildSettings),
                event.start.asDiscordTimestamp(DiscordTimestampFormat.LONG_DATETIME),
                true
            ).addField(
                getEmbedMessage("rsvp", "waitlist.field.end", guildSettings),
                event.end.asDiscordTimestamp(DiscordTimestampFormat.LONG_DATETIME),
                true
            ).footer(getEmbedMessage("rsvp", "waitlist.footer", guildSettings, event.eventId), null)

        if (event.location.isNotBlank()) builder.addField(
            getEmbedMessage("rsvp", "waitlist.field.location", guildSettings),
            event.location.toMarkdown().embedFieldSafe(),
            false
        )

        if (event.image.isNotBlank()) builder.thumbnail(event.image)


        return builder.build()
    }

    suspend fun rsvpListEmbed(event: Event, rsvp: Rsvp, settings: GuildSettings): EmbedCreateSpec {
        val waitlistDisplayLimit = Config.EMBED_RSVP_WAITLIST_DISPLAY_LENGTH.getInt()

        val role = if (rsvp.role != null) "<@&${rsvp.role.asString()}>" else "None"

        val goingOnTime = rsvp.goingOnTime.map {
            discordClient.getUserById(it).data.awaitSingle()
        }.joinToString(", ") {
            it.globalName().orElse(it.username())
        }.ifEmpty { "N/a" }

        val late = rsvp.goingLate.map {
            discordClient.getUserById(it).data.awaitSingle()
        }.joinToString(", ") {
            it.globalName().orElse(it.username())
        }.ifEmpty { "N/a" }

        val undecided = rsvp.undecided.map {
            discordClient.getUserById(it).data.awaitSingle()
        }.joinToString(", ") {
            it.globalName().orElse(it.username())
        }.ifEmpty { "N/a" }

        val notGoing = rsvp.notGoing.map {
            discordClient.getUserById(it).data.awaitSingle()
        }.joinToString(", ") {
            it.globalName().orElse(it.username())
        }.ifEmpty { "N/a" }

        val waitList = if (rsvp.waitlist.size > waitlistDisplayLimit) {
            rsvp.waitlist.map {
                discordClient.getUserById(it).data.awaitSingle()
            }.joinToString(", ") {
                it.globalName().orElse(it.username())
            }.plus("+${rsvp.waitlist.size - waitlistDisplayLimit} more")
        } else {
            rsvp.waitlist.map {
                discordClient.getUserById(it).data.awaitSingle()
            }.joinToString(", ") {
                it.globalName().orElse(it.username())
            }.ifEmpty { "N/a" }
        }

        val limitValue = if (rsvp.limit < 0) {
            getEmbedMessage("rsvp", "list.field.limit.value", settings, "${rsvp.getCurrentCount()}")
        } else "${rsvp.getCurrentCount()}/${rsvp.limit}"



        return defaultEmbedBuilder(settings)
            .color(event.color.asColor())
            .title(getEmbedMessage("rsvp", "list.title", settings))
            .addField(getEmbedMessage("rsvp", "list.field.event", settings), rsvp.eventId, false)
            .addField(getEmbedMessage("rsvp", "list.field.limit", settings), limitValue, true)
            .addField(getEmbedMessage("rsvp", "list.field.role", settings), role, true)
            .addField(getEmbedMessage("rsvp", "list.field.onTime", settings), goingOnTime, false)
            .addField(getEmbedMessage("rsvp", "list.field.late", settings), late, false)
            .addField(getEmbedMessage("rsvp", "list.field.unsure", settings), undecided, false)
            .addField(getEmbedMessage("rsvp", "list.field.notGoing", settings), notGoing, false)
            .addField(getEmbedMessage("rsvp", "list.field.waitList", settings), waitList, false)
            .footer(getEmbedMessage("rsvp", "list.footer", settings), null)
            .build()
    }
}
