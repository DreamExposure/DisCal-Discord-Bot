package org.dreamexposure.discal.core.business

import discord4j.common.util.Snowflake
import discord4j.core.DiscordClient
import discord4j.core.spec.EmbedCreateSpec
import kotlinx.coroutines.reactor.awaitSingle
import org.dreamexposure.discal.Application
import org.dreamexposure.discal.GitProperty
import org.dreamexposure.discal.core.config.Config
import org.dreamexposure.discal.core.entities.Calendar
import org.dreamexposure.discal.core.entities.Event
import org.dreamexposure.discal.core.enums.announcement.AnnouncementStyle
import org.dreamexposure.discal.core.enums.time.DiscordTimestampFormat
import org.dreamexposure.discal.core.extensions.*
import org.dreamexposure.discal.core.extensions.discord4j.getCalendar
import org.dreamexposure.discal.core.extensions.discord4j.getSettings
import org.dreamexposure.discal.core.`object`.GuildSettings
import org.dreamexposure.discal.core.`object`.new.Announcement
import org.dreamexposure.discal.core.`object`.new.Rsvp
import org.dreamexposure.discal.core.utils.GlobalVal
import org.dreamexposure.discal.core.utils.getCommonMsg
import org.dreamexposure.discal.core.utils.getEmbedMessage
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.getBean
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

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

    ////////////////////////////
    ////// General Embeds //////
    ////////////////////////////
    suspend fun discalInfoEmbed(settings: GuildSettings, calendarCount: Long, announcementCount: Long): EmbedCreateSpec {
        val guildCount = discordClient.guilds.count().awaitSingle()

        return defaultEmbedBuilder(settings)
            .color(GlobalVal.discalColor)
            .title(getEmbedMessage("discal", "info.title", settings))
            .addField(getEmbedMessage("discal", "info.field.version", settings), GitProperty.DISCAL_VERSION.value, false)
            .addField(getEmbedMessage("discal", "info.field.library", settings), "Discord4J ${GitProperty.DISCAL_VERSION_D4J.value}", false)
            .addField(getEmbedMessage("discal", "info.field.shard", settings), "${Application.getShardIndex()}/${Application.getShardCount()}", true)
            .addField(getEmbedMessage("discal", "info.field.guilds", settings), "$guildCount", true)
            .addField(
                getEmbedMessage("discal", "info.field.uptime", settings),
                Application.getUptime().getHumanReadable(),
                false
            ).addField(getEmbedMessage("discal", "info.field.calendars", settings), "$calendarCount", true)
            .addField(getEmbedMessage("discal", "info.field.announcements", settings), "$announcementCount", true)
            .addField(getEmbedMessage("discal", "info.field.links", settings),
                getEmbedMessage("discal",
                    "info.field.links.value",
                    settings,
                    "${Config.URL_BASE.getString()}/commands",
                    Config.URL_SUPPORT.getString(),
                    Config.URL_INVITE.getString(),
                    "https://www.patreon.com/Novafox"
                ),
                false
            ).footer(getEmbedMessage("discal", "info.footer", settings), null)
            .build()
    }

    /////////////////////////////
    ////// Calendar Embeds //////
    /////////////////////////////
    suspend fun calendarOverviewEmbed(calendar: Calendar, settings: GuildSettings, showUpdate: Boolean): EmbedCreateSpec {
        val builder = defaultEmbedBuilder(settings)

        // Get the events to build the overview
        val events = calendar.getUpcomingEvents(15)
            .collectList()
            .map { it.groupByDate() }
            .awaitSingle()

        //Handle optional fields
        if (calendar.name.isNotBlank())
            builder.title(calendar.name.toMarkdown().embedTitleSafe())
        if (calendar.description.isNotBlank())
            builder.description(calendar.description.toMarkdown().embedDescriptionSafe())

        // Truncate dates to 23 due to discord enforcing the field limit
        val truncatedEvents = mutableMapOf<ZonedDateTime, List<Event>>()
        for (event in events) {
            if (truncatedEvents.size < 23) {
                truncatedEvents[event.key] = event.value
            } else break
        }

        // Show events
        truncatedEvents.forEach { date ->
            val title = date.key.toInstant().humanReadableDate(calendar.timezone, settings.timeFormat, longDay = true)

            // sort events
            val sortedEvents = date.value.sortedBy { it.start }

            val content = StringBuilder()

            sortedEvents.forEach {
                // Start event
                content.append("```\n")

                // determine time length
                val timeDisplayLen = ("${it.start.humanReadableTime(it.timezone, settings.timeFormat)} -" +
                    " ${it.end.humanReadableTime(it.timezone, settings.timeFormat)} ").length

                // Displaying time
                if (it.isAllDay()) {
                    content.append(getCommonMsg("generic.time.allDay", settings).padCenter(timeDisplayLen))
                        .append("| ")
                } else {
                    // Add start text
                    var str = if (it.start.isBefore(date.key.toInstant())) {
                        "${getCommonMsg("generic.time.continued", settings)} - "
                    } else {
                        "${it.start.humanReadableTime(it.timezone, settings.timeFormat)} - "
                    }
                    // Add end text
                    str += if (it.end.isAfter(date.key.toInstant().plus(1, ChronoUnit.DAYS))) {
                        getCommonMsg("generic.time.continued", settings)
                    } else {
                        "${it.end.humanReadableTime(it.timezone, settings.timeFormat)} "
                    }
                    content.append(str.padCenter(timeDisplayLen))
                        .append("| ")
                }
                // Display name or ID if not set
                if (it.name.isNotBlank()) content.append(it.name)
                else content.append(getEmbedMessage("calendar", "link.field.id", settings)).append(" ${it.eventId}")
                content.append("\n")
                if (it.location.isNotBlank()) content.append("    Location: ")
                    .append(it.location.embedFieldSafe())
                    .append("\n")

                // Finish event
                content.append("```\n")
            }

            if (content.isNotBlank())
                builder.addField(title, content.toString().embedFieldSafe(), false)
        }

        // set footer
        if (showUpdate) {
            val lastUpdate = Instant.now().asDiscordTimestamp(DiscordTimestampFormat.RELATIVE_TIME)
            builder.footer(getEmbedMessage("calendar", "link.footer.update", settings, lastUpdate), null)
                .timestamp(Instant.now())
        } else builder.footer(getEmbedMessage("calendar", "link.footer.default", settings), null)

        // finish and return
        return builder.addField(getEmbedMessage("calendar", "link.field.timezone", settings), calendar.zoneName, true)
            .addField(getEmbedMessage("calendar", "link.field.number", settings), "${calendar.calendarNumber}", true)
            .url(calendar.link)
            .color(GlobalVal.discalColor)
            .build()
    }

    suspend fun linkCalendarEmbed(calendarNumber: Int, settings: GuildSettings, overview: Boolean): EmbedCreateSpec {
        val calendar = discordClient.getGuildById(settings.guildID).getCalendar(calendarNumber).awaitSingle()
        return if (overview) calendarOverviewEmbed(calendar, settings, showUpdate = false)
        else linkCalendarEmbed(calendar, settings)
    }

    suspend fun linkCalendarEmbed(calendar: Calendar, settings: GuildSettings): EmbedCreateSpec {
        val builder = defaultEmbedBuilder(settings)

        //Handle optional fields
        if (calendar.name.isNotBlank())
            builder.title(calendar.name.toMarkdown().embedTitleSafe())
        if (calendar.description.isNotBlank())
            builder.description(calendar.description.toMarkdown().embedDescriptionSafe())

        return builder.addField(getEmbedMessage("calendar", "link.field.timezone", settings), calendar.zoneName, false)
            .addField(getEmbedMessage("calendar", "link.field.host", settings), calendar.calendarData.host.name, true)
            .addField(getEmbedMessage("calendar", "link.field.number", settings), "${calendar.calendarNumber}", true)
            .addField(getEmbedMessage("calendar", "link.field.id", settings), calendar.calendarId, false)
            .url(calendar.link)
            .footer(getEmbedMessage("calendar", "link.footer.default", settings), null)
            .color(GlobalVal.discalColor)
            .build()
    }

    /////////////////////////
    ////// RSVP Embeds //////
    /////////////////////////
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

    /////////////////////////////////
    ////// Announcement Embeds //////
    /////////////////////////////////
    suspend fun determineAnnouncementEmbed(announcement: Announcement, event: Event, settings: GuildSettings): EmbedCreateSpec {
        return when(settings.announcementStyle) {
            AnnouncementStyle.FULL -> fullAnnouncementEmbed(announcement, event, settings)
            AnnouncementStyle.SIMPLE -> simpleAnnouncementEmbed(announcement, event, settings)
            AnnouncementStyle.EVENT -> eventAnnouncementEmbed(announcement, event, settings)
        }
    }

    suspend fun fullAnnouncementEmbed(announcement: Announcement, event: Event, settings: GuildSettings): EmbedCreateSpec {
        val builder = defaultEmbedBuilder(settings)
            .color(event.color.asColor())
            .title(getEmbedMessage("announcement", "full.title", settings))

        if (event.name.isNotBlank()) builder.addField(
            getEmbedMessage("announcement", "full.field.name", settings),
            event.name.toMarkdown().embedFieldSafe(),
            false
        )
        if (event.description.isNotBlank()) builder.addField(
            getEmbedMessage("announcement", "full.field.desc", settings),
            event.description.toMarkdown().embedFieldSafe(),
            false
        )

        builder.addField(
            getEmbedMessage("announcement", "full.field.start", settings),
            event.start.asDiscordTimestamp(DiscordTimestampFormat.LONG_DATETIME),
            true
        )
        builder.addField(
            getEmbedMessage("announcement", "full.field.end", settings),
            event.end.asDiscordTimestamp(DiscordTimestampFormat.LONG_DATETIME),
            true
        )

        if (event.location.isNotBlank()) builder.addField(
            getEmbedMessage("announcement", "full.field.location", settings),
            event.location.toMarkdown().embedFieldSafe(),
            false
        )

        if (announcement.info.isNotBlank() && !announcement.info.equals("None", true)) builder.addField(
            getEmbedMessage("announcement", "full.field.info", settings),
            announcement.info.toMarkdown().embedFieldSafe(),
            false
        )

        builder.addField(
            getEmbedMessage("announcement", "full.field.calendar", settings),
            "${event.calendar.calendarNumber}",
            true
        )
        builder.addField(getEmbedMessage("announcement", "full.field.event", settings), event.eventId, true)

        if (event.image.isNotBlank())
            builder.image(event.image)

        builder.footer(getEmbedMessage("announcement", "full.footer", settings, announcement.id), null)

        return builder.build()
    }

    suspend fun simpleAnnouncementEmbed(announcement: Announcement, event: Event, settings: GuildSettings): EmbedCreateSpec {
        val builder = defaultEmbedBuilder(settings)
            .color(event.color.asColor())
            .title(getEmbedMessage("announcement", "simple.title", settings))

        if (event.name.isNotBlank()) builder.addField(
            getEmbedMessage("announcement", "simple.field.name", settings),
            event.name.toMarkdown().embedFieldSafe(),
            false
        )
        if (event.description.isNotBlank()) builder.addField(
            getEmbedMessage("announcement", "simple.field.desc", settings),
            event.description.toMarkdown().embedFieldSafe(),
            false
        )

        builder.addField(
            getEmbedMessage("announcement", "simple.field.start", settings),
            event.start.asDiscordTimestamp(DiscordTimestampFormat.LONG_DATETIME),
            true
        )

        if (event.location.isNotBlank()) builder.addField(
            getEmbedMessage("announcement", "simple.field.location", settings),
            event.location.toMarkdown().embedFieldSafe(),
            false
        )

        if (announcement.info.isNotBlank() && !announcement.info.equals("None", true)) builder.addField(
            getEmbedMessage("announcement", "simple.field.info", settings),
            announcement.info.toMarkdown().embedFieldSafe(),
            false
        )

        if (event.image.isNotEmpty())
            builder.image(event.image)

        builder.footer(getEmbedMessage("announcement", "simple.footer", settings, announcement.id), null)

        return builder.build()
    }

    suspend fun eventAnnouncementEmbed(announcement: Announcement, event: Event, settings: GuildSettings): EmbedCreateSpec {
        val builder = defaultEmbedBuilder(settings)
            .color(event.color.asColor())
            .title(getEmbedMessage("announcement", "event.title", settings))

        if (event.name.isNotBlank()) builder.addField(
            getEmbedMessage("announcement", "event.field.name", settings),
            event.name.toMarkdown().embedFieldSafe(),
            false
        )
        if (event.description.isNotBlank()) builder.addField(
            getEmbedMessage("announcement", "event.field.desc", settings),
            event.description.toMarkdown().embedFieldSafe(),
            false
        )

        builder.addField(
            getEmbedMessage("announcement", "event.field.start", settings),
            event.start.asDiscordTimestamp(DiscordTimestampFormat.LONG_DATETIME),
            true
        )
        builder.addField(
            getEmbedMessage("announcement", "event.field.end", settings),
            event.end.asDiscordTimestamp(DiscordTimestampFormat.LONG_DATETIME),
            true
        )

        if (event.location.isNotBlank()) builder.addField(
            getEmbedMessage("announcement", "event.field.location", settings),
            event.location.toMarkdown().embedFieldSafe(),
            false
        )

        builder.addField(
            getEmbedMessage("announcement", "event.field.calendar", settings),
            "${event.calendar.calendarNumber}",
            true
        )
        builder.addField(getEmbedMessage("announcement", "event.field.event", settings), event.eventId, true)

        if (announcement.info.isNotBlank() && !announcement.info.equals("None", true)) builder.addField(
            getEmbedMessage("announcement", "event.field.info", settings),
            announcement.info.toMarkdown().embedFieldSafe(),
            false
        )

        if (event.image.isNotBlank())
            builder.image(event.image)

        builder.footer(getEmbedMessage("announcement", "event.footer", settings, announcement.id), null)

        return builder.build()
    }
}
