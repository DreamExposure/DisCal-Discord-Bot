package org.dreamexposure.discal.core.business

import discord4j.common.util.Snowflake
import discord4j.core.DiscordClient
import discord4j.core.spec.EmbedCreateSpec
import kotlinx.coroutines.reactor.awaitSingle
import org.dreamexposure.discal.AnnouncementWizardState
import org.dreamexposure.discal.Application
import org.dreamexposure.discal.CalendarWizardState
import org.dreamexposure.discal.GitProperty
import org.dreamexposure.discal.core.config.Config
import org.dreamexposure.discal.core.enums.event.EventColor
import org.dreamexposure.discal.core.enums.time.DiscordTimestampFormat
import org.dreamexposure.discal.core.enums.time.DiscordTimestampFormat.LONG_DATETIME
import org.dreamexposure.discal.core.extensions.*
import org.dreamexposure.discal.core.`object`.new.*
import org.dreamexposure.discal.core.`object`.new.GuildSettings.AnnouncementStyle.*
import org.dreamexposure.discal.core.utils.GlobalVal
import org.dreamexposure.discal.core.utils.getCommonMsg
import org.dreamexposure.discal.core.utils.getEmbedMessage
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.getBean
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

@Component
class EmbedService(
    private val settingsService: GuildSettingsService,
    private val beanFactory: BeanFactory,
) {
    private val discordClient: DiscordClient
        get() = beanFactory.getBean()


    private suspend fun defaultEmbedBuilder(settings: GuildSettings): EmbedCreateSpec.Builder {
        val guild = discordClient.getGuildById(settings.guildId).data.awaitSingle()

        val iconUrl = if (settings.interfaceStyle.branded && guild.icon().isPresent)
            "${GlobalVal.discordCdnUrl}/icons/${settings.guildId.asString()}/${guild.icon().get()}.png"
        else GlobalVal.iconUrl

        return EmbedCreateSpec.builder()
            .author(
                if (settings.interfaceStyle.branded) guild.name() else getCommonMsg("bot.name", settings.locale),
                Config.URL_BASE.getString(),
                iconUrl
            )
    }

    ////////////////////////////
    ////// General Embeds //////
    ////////////////////////////
    suspend fun discalInfoEmbed(settings: GuildSettings, guildCount: Long, calendarCount: Long, announcementCount: Long): EmbedCreateSpec {
        return defaultEmbedBuilder(settings)
            .color(GlobalVal.discalColor)
            .title(getEmbedMessage("discal", "info.title", settings.locale))
            .addField(getEmbedMessage("discal", "info.field.version", settings.locale), GitProperty.DISCAL_VERSION.value, false)
            .addField(getEmbedMessage("discal", "info.field.library", settings.locale), "Discord4J ${GitProperty.DISCAL_VERSION_D4J.value}", false)
            .addField(getEmbedMessage("discal", "info.field.shard", settings.locale), "${Application.getShardIndex()}/${Application.getShardCount()}", true)
            .addField(getEmbedMessage("discal", "info.field.guilds", settings.locale), "$guildCount", true)
            .addField(
                getEmbedMessage("discal", "info.field.uptime", settings.locale),
                Application.getUptime().getHumanReadable(),
                false
            ).addField(getEmbedMessage("discal", "info.field.calendars", settings.locale), "$calendarCount", true)
            .addField(getEmbedMessage("discal", "info.field.announcements", settings.locale), "$announcementCount", true)
            .addField(getEmbedMessage("discal", "info.field.links", settings.locale),
                getEmbedMessage("discal",
                    "info.field.links.value",
                    settings.locale,
                    "${Config.URL_BASE.getString()}/commands",
                    Config.URL_SUPPORT.getString(),
                    Config.URL_INVITE.getString(),
                    "https://www.patreon.com/Novafox"
                ),
                false
            ).footer(getEmbedMessage("discal", "info.footer", settings.locale), null)
            .build()
    }

    /////////////////////////////
    ////// Settings Embeds //////
    /////////////////////////////
    suspend fun settingsEmbeds(settings: GuildSettings): EmbedCreateSpec {
        val controlRoleValue = if (settings.controlRole == null) "<@&${settings.guildId.asLong()}>" else "<@&${settings.controlRole}>"

        return defaultEmbedBuilder(settings)
            .title(getEmbedMessage("settings", "view.title", settings.locale))
            .addField(getEmbedMessage("settings", "view.field.role", settings.locale), controlRoleValue, false)
            .addField(getEmbedMessage("settings", "view.field.style", settings.locale), settings.interfaceStyle.announcementStyle.name, true)
            .addField(getEmbedMessage("settings", "view.field.format", settings.locale), settings.interfaceStyle.timeFormat.name, true)
            .addField(getEmbedMessage("settings", "view.field.eventKeepDuration", settings.locale), "${settings.eventKeepDuration}", true)
            .addField(getEmbedMessage("settings", "view.field.lang", settings.locale), settings.locale.displayName, false)
            .addField(getEmbedMessage("settings", "view.field.patron", settings.locale), "${settings.patronGuild}", true)
            .addField(getEmbedMessage("settings", "view.field.dev", settings.locale), "${settings.devGuild}", true)
            .addField(getEmbedMessage("settings", "view.field.cal", settings.locale), "${settings.maxCalendars}", true)
            .addField(getEmbedMessage("settings", "view.field.brand", settings.locale), "${settings.interfaceStyle.branded}", false)
            .footer(getEmbedMessage("settings", "view.footer", settings.locale), null)
            .build()
    }

    /////////////////////////////
    ////// Calendar Embeds //////
    /////////////////////////////
    suspend fun calendarOverviewEmbed(calendar: Calendar, events: List<Event>, showUpdate: Boolean): EmbedCreateSpec {
        val settings = settingsService.getSettings(calendar.metadata.guildId)
        val builder = defaultEmbedBuilder(settings)

        // Get events sorted and grouped
        val groupedEvents = events.groupByDate()

        //Handle optional fields
        if (calendar.name.isNotBlank())
            builder.title(calendar.name.toMarkdown().embedTitleSafe())
        if (calendar.description.isNotBlank())
            builder.description(calendar.description.toMarkdown().embedDescriptionSafe())

        // Truncate dates to 23 due to discord enforcing the field limit
        val truncatedEvents = mutableMapOf<ZonedDateTime, List<Event>>()
        for (event in groupedEvents) {
            if (truncatedEvents.size < 23) {
                truncatedEvents[event.key] = event.value
            } else break
        }

        // Show events
        truncatedEvents.forEach { date ->
            val title = date.key.toInstant().humanReadableDate(calendar.timezone, settings.interfaceStyle.timeFormat, longDay = true)

            // sort events
            val sortedEvents = date.value.sortedBy { it.start }

            val content = StringBuilder()

            sortedEvents.forEach {
                // Start event
                content.append("```\n")

                // determine time length
                val timeDisplayLen = ("${it.start.humanReadableTime(it.timezone, settings.interfaceStyle.timeFormat)} -" +
                    " ${it.end.humanReadableTime(it.timezone, settings.interfaceStyle.timeFormat)} ").length

                // Displaying time
                if (it.isAllDay()) {
                    content.append(getCommonMsg("generic.time.allDay", settings.locale).padCenter(timeDisplayLen))
                        .append("| ")
                } else {
                    // Add start text
                    var str = if (it.start.isBefore(date.key.toInstant())) {
                        "${getCommonMsg("generic.time.continued", settings.locale)} - "
                    } else {
                        "${it.start.humanReadableTime(it.timezone, settings.interfaceStyle.timeFormat)} - "
                    }
                    // Add end text
                    str += if (it.end.isAfter(date.key.toInstant().plus(1, ChronoUnit.DAYS))) {
                        getCommonMsg("generic.time.continued", settings.locale)
                    } else {
                        "${it.end.humanReadableTime(it.timezone, settings.interfaceStyle.timeFormat)} "
                    }
                    content.append(str.padCenter(timeDisplayLen))
                        .append("| ")
                }
                // Display name or ID if not set
                if (it.name.isNotBlank()) content.append(it.name)
                else content.append(getEmbedMessage("calendar", "link.field.id", settings.locale)).append(" ${it.id}")
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
            builder.footer(getEmbedMessage("calendar", "link.footer.update", settings.locale, lastUpdate), null)
                .timestamp(Instant.now())
        } else builder.footer(getEmbedMessage("calendar", "link.footer.default", settings.locale), null)

        // finish and return
        return builder.addField(getEmbedMessage("calendar", "link.field.timezone", settings.locale), calendar.timezone.id, true)
            .addField(getEmbedMessage("calendar", "link.field.number", settings.locale), "${calendar.metadata.number}", true)
            .url(calendar.link)
            .color(GlobalVal.discalColor)
            .build()
    }

    suspend fun linkCalendarEmbed(calendar: Calendar, events: List<Event>?): EmbedCreateSpec {
        return if (events != null) calendarOverviewEmbed(calendar, events, showUpdate = false)
        else linkCalendarEmbed(calendar)
    }

    suspend fun linkCalendarEmbed(calendar: Calendar): EmbedCreateSpec {
        val settings = settingsService.getSettings(calendar.metadata.guildId)
        val builder = defaultEmbedBuilder(settings)

        //Handle optional fields
        if (calendar.name.isNotBlank())
            builder.title(calendar.name.toMarkdown().embedTitleSafe())
        if (calendar.description.isNotBlank())
            builder.description(calendar.description.toMarkdown().embedDescriptionSafe())

        return builder.addField(getEmbedMessage("calendar", "link.field.timezone", settings.locale), calendar.timezone.id, false)
            .addField(getEmbedMessage("calendar", "link.field.host", settings.locale), calendar.metadata.host.name, true)
            .addField(getEmbedMessage("calendar", "link.field.number", settings.locale), "${calendar.metadata.number}", true)
            .addField(getEmbedMessage("calendar", "link.field.id", settings.locale), calendar.metadata.id, false)
            .url(calendar.link)
            .footer(getEmbedMessage("calendar", "link.footer.default", settings.locale), null)
            .color(GlobalVal.discalColor)
            .build()
    }

    suspend fun calendarTimeEmbed(calendar: Calendar, settings: GuildSettings): EmbedCreateSpec {
        val formattedTime = Instant.now().humanReadableFullSimple(calendar.timezone, settings.interfaceStyle.timeFormat)
        val formattedLocal = Instant.now().asDiscordTimestamp(DiscordTimestampFormat.SHORT_DATETIME)

        return defaultEmbedBuilder(settings)
            .title(getEmbedMessage("time", "embed.title", settings.locale))
            .addField(getEmbedMessage("time", "embed.field.current", settings.locale), formattedTime, true)
            .addField(getEmbedMessage("time", "embed.field.timezone", settings.locale), calendar.timezone.id, true)
            .addField(getEmbedMessage("time", "embed.field.local", settings.locale), formattedLocal, false)
            .footer(getEmbedMessage("time", "embed.footer", settings.locale), null)
            .url(calendar.link)
            .color(GlobalVal.discalColor)
            .build()
    }

    suspend fun calendarWizardEmbed(wizard: CalendarWizardState, settings: GuildSettings): EmbedCreateSpec {
        val builder = defaultEmbedBuilder(settings)
            .title(getEmbedMessage("calendar", "wizard.title", settings.locale))
            .footer(getEmbedMessage("calendar", "wizard.footer", settings.locale), null)
            .addField(
                getEmbedMessage("calendar", "wizard.field.name", settings.locale),
                wizard.entity.name.toMarkdown().embedFieldSafe(),
                false
            ).addField(
                getEmbedMessage("calendar", "wizard.field.description", settings.locale),
                wizard.entity.description.ifEmpty { getCommonMsg("embed.unset", settings.locale) }.toMarkdown().embedFieldSafe(),
                false
            ).addField(
                getEmbedMessage("calendar", "wizard.field.timezone", settings.locale),
                wizard.entity.timezone.id,
                true
            ).addField(
                getEmbedMessage("calendar", "wizard.field.host", settings.locale),
                wizard.entity.metadata.host.name,
                true
            )

        if (wizard.editing) builder.addField(
            getEmbedMessage("calendar", "wizard.field.id", settings.locale),
            wizard.entity.metadata.id,
            false
        )

        return builder.build()
    }

    //////////////////////////
    ////// Event Embeds //////
    //////////////////////////
    suspend fun fullEventEmbed(event: Event, settings: GuildSettings): EmbedCreateSpec {
        val builder = defaultEmbedBuilder(settings)
            .footer(getEmbedMessage("event", "full.footer", settings.locale, event.id), null)
            .color(event.color.asColor())

        if (event.name.isNotBlank())
            builder.title(event.name.toMarkdown().embedTitleSafe())
        if (event.description.isNotBlank())
            builder.description(event.description.toMarkdown().embedDescriptionSafe())

        builder.addField(
            getEmbedMessage("event", "full.field.start", settings.locale),
            event.start.asDiscordTimestamp(LONG_DATETIME),
            true)
        builder.addField(
            getEmbedMessage("event", "full.field.end", settings.locale),
            event.end.asDiscordTimestamp(LONG_DATETIME),
            true
        )

        if (event.location.isNotBlank()) builder.addField(
            getEmbedMessage("event", "full.field.location", settings.locale),
            event.location.toMarkdown().embedFieldSafe(),
            false
        )

        builder.addField(getEmbedMessage("event", "full.field.cal", settings.locale), "${event.calendarNumber}", false)

        if (event.image.isNotEmpty())
            builder.image(event.image)

        return builder.build()
    }

    suspend fun condensedEventEmbed(event: Event, settings: GuildSettings): EmbedCreateSpec {
        val builder = defaultEmbedBuilder(settings)
            .footer(getEmbedMessage("event", "con.footer", settings.locale, event.id), null)
            .color(event.color.asColor())

        if (event.name.isNotBlank())
            builder.title(event.name.toMarkdown().embedTitleSafe())

        builder.addField(
            getEmbedMessage("event", "con.field.start", settings.locale),
            event.start.asDiscordTimestamp(LONG_DATETIME),
            true
        )

        if (event.location.isNotBlank()) builder.addField(
            getEmbedMessage("event", "con.field.location", settings.locale),
            event.location.toMarkdown().embedFieldSafe(),
            false
        )

        if (event.image.isNotBlank())
            builder.thumbnail(event.image)

        return builder.build()
    }

    /////////////////////////
    ////// RSVP Embeds //////
    /////////////////////////
    suspend fun rsvpDmFollowupEmbed(rsvp: Rsvp, event: Event, userId: Snowflake): EmbedCreateSpec {
        val restGuild = discordClient.getGuildById(rsvp.guildId)
        val guildData = restGuild.data.awaitSingle()
        val settings = settingsService.getSettings(rsvp.guildId)


        val iconUrl = if (guildData.icon().isPresent)
            "${GlobalVal.discordCdnUrl}/icons/${rsvp.guildId.asString()}/${guildData.icon().get()}.png"
        else GlobalVal.iconUrl

        val builder = EmbedCreateSpec.builder()
            // Even without branding enabled, we want the user to know what guild this is because it's in DMs
            .author(guildData.name(), Config.URL_BASE.getString(), iconUrl)
            .title(getEmbedMessage("rsvp", "waitlist.title", settings.locale))
            .description(getEmbedMessage("rsvp", "waitlist.desc", settings.locale, userId.asString(), event.name))
            .addField(
                getEmbedMessage("rsvp", "waitlist.field.start", settings.locale),
                event.start.asDiscordTimestamp(LONG_DATETIME),
                true
            ).addField(
                getEmbedMessage("rsvp", "waitlist.field.end", settings.locale),
                event.end.asDiscordTimestamp(LONG_DATETIME),
                true
            ).footer(getEmbedMessage("rsvp", "waitlist.footer", settings.locale, event.id), null)

        if (event.location.isNotBlank()) builder.addField(
            getEmbedMessage("rsvp", "waitlist.field.location", settings.locale),
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
            getEmbedMessage("rsvp", "list.field.limit.value", settings.locale, "${rsvp.getCurrentCount()}")
        } else "${rsvp.getCurrentCount()}/${rsvp.limit}"



        return defaultEmbedBuilder(settings)
            .color(event.color.asColor())
            .title(getEmbedMessage("rsvp", "list.title", settings.locale))
            .addField(getEmbedMessage("rsvp", "list.field.event", settings.locale), rsvp.eventId, false)
            .addField(getEmbedMessage("rsvp", "list.field.limit", settings.locale), limitValue, true)
            .addField(getEmbedMessage("rsvp", "list.field.role", settings.locale), role, true)
            .addField(getEmbedMessage("rsvp", "list.field.onTime", settings.locale), goingOnTime, false)
            .addField(getEmbedMessage("rsvp", "list.field.late", settings.locale), late, false)
            .addField(getEmbedMessage("rsvp", "list.field.unsure", settings.locale), undecided, false)
            .addField(getEmbedMessage("rsvp", "list.field.notGoing", settings.locale), notGoing, false)
            .addField(getEmbedMessage("rsvp", "list.field.waitList", settings.locale), waitList, false)
            .footer(getEmbedMessage("rsvp", "list.footer", settings.locale), null)
            .build()
    }

    /////////////////////////////////
    ////// Announcement Embeds //////
    /////////////////////////////////
    suspend fun determineAnnouncementEmbed(announcement: Announcement, event: Event): EmbedCreateSpec {
        val settings = settingsService.getSettings(announcement.guildId)
        return when (settings.interfaceStyle.announcementStyle) {
            FULL -> fullAnnouncementEmbed(announcement, event, settings)
            SIMPLE -> simpleAnnouncementEmbed(announcement, event, settings)
            EVENT -> eventAnnouncementEmbed(announcement, event, settings)
        }
    }

    suspend fun fullAnnouncementEmbed(announcement: Announcement, event: Event, settings: GuildSettings): EmbedCreateSpec {
        val builder = defaultEmbedBuilder(settings)
            .color(event.color.asColor())
            .title(getEmbedMessage("announcement", "full.title", settings.locale))

        if (event.name.isNotBlank()) builder.addField(
            getEmbedMessage("announcement", "full.field.name", settings.locale),
            event.name.toMarkdown().embedFieldSafe(),
            false
        )
        if (event.description.isNotBlank()) builder.addField(
            getEmbedMessage("announcement", "full.field.desc", settings.locale),
            event.description.toMarkdown().embedFieldSafe(),
            false
        )

        builder.addField(
            getEmbedMessage("announcement", "full.field.start", settings.locale),
            event.start.asDiscordTimestamp(LONG_DATETIME),
            true
        )
        builder.addField(
            getEmbedMessage("announcement", "full.field.end", settings.locale),
            event.end.asDiscordTimestamp(LONG_DATETIME),
            true
        )

        if (event.location.isNotBlank()) builder.addField(
            getEmbedMessage("announcement", "full.field.location", settings.locale),
            event.location.toMarkdown().embedFieldSafe(),
            false
        )

        if (!announcement.info.isNullOrBlank()) builder.addField(
            getEmbedMessage("announcement", "full.field.info", settings.locale),
            announcement.info.toMarkdown().embedFieldSafe(),
            false
        )

        builder.addField(
            getEmbedMessage("announcement", "full.field.calendar", settings.locale),
            "${event.calendarNumber}",
            true
        )
        builder.addField(getEmbedMessage("announcement", "full.field.event", settings.locale), event.id, true)

        if (event.image.isNotBlank())
            builder.image(event.image)

        builder.footer(getEmbedMessage("announcement", "full.footer", settings.locale, announcement.id), null)

        return builder.build()
    }

    suspend fun simpleAnnouncementEmbed(announcement: Announcement, event: Event, settings: GuildSettings): EmbedCreateSpec {
        val builder = defaultEmbedBuilder(settings)
            .color(event.color.asColor())
            .title(getEmbedMessage("announcement", "simple.title", settings.locale))

        if (event.name.isNotBlank()) builder.addField(
            getEmbedMessage("announcement", "simple.field.name", settings.locale),
            event.name.toMarkdown().embedFieldSafe(),
            false
        )
        if (event.description.isNotBlank()) builder.addField(
            getEmbedMessage("announcement", "simple.field.desc", settings.locale),
            event.description.toMarkdown().embedFieldSafe(),
            false
        )

        builder.addField(
            getEmbedMessage("announcement", "simple.field.start", settings.locale),
            event.start.asDiscordTimestamp(LONG_DATETIME),
            true
        )

        if (event.location.isNotBlank()) builder.addField(
            getEmbedMessage("announcement", "simple.field.location", settings.locale),
            event.location.toMarkdown().embedFieldSafe(),
            false
        )

        if (!announcement.info.isNullOrBlank()) builder.addField(
            getEmbedMessage("announcement", "simple.field.info", settings.locale),
            announcement.info.toMarkdown().embedFieldSafe(),
            false
        )

        if (event.image.isNotEmpty())
            builder.image(event.image)

        builder.footer(getEmbedMessage("announcement", "simple.footer", settings.locale, announcement.id), null)

        return builder.build()
    }

    suspend fun eventAnnouncementEmbed(announcement: Announcement, event: Event, settings: GuildSettings): EmbedCreateSpec {
        val builder = defaultEmbedBuilder(settings)
            .color(event.color.asColor())
            .title(getEmbedMessage("announcement", "event.title", settings.locale))

        if (event.name.isNotBlank()) builder.addField(
            getEmbedMessage("announcement", "event.field.name", settings.locale),
            event.name.toMarkdown().embedFieldSafe(),
            false
        )
        if (event.description.isNotBlank()) builder.addField(
            getEmbedMessage("announcement", "event.field.desc", settings.locale),
            event.description.toMarkdown().embedFieldSafe(),
            false
        )

        builder.addField(
            getEmbedMessage("announcement", "event.field.start", settings.locale),
            event.start.asDiscordTimestamp(LONG_DATETIME),
            true
        )
        builder.addField(
            getEmbedMessage("announcement", "event.field.end", settings.locale),
            event.end.asDiscordTimestamp(LONG_DATETIME),
            true
        )

        if (event.location.isNotBlank()) builder.addField(
            getEmbedMessage("announcement", "event.field.location", settings.locale),
            event.location.toMarkdown().embedFieldSafe(),
            false
        )

        builder.addField(
            getEmbedMessage("announcement", "event.field.calendar", settings.locale),
            "${event.calendarNumber}",
            true
        )
        builder.addField(getEmbedMessage("announcement", "event.field.event", settings.locale), event.id, true)

        if (!announcement.info.isNullOrBlank()) builder.addField(
            getEmbedMessage("announcement", "event.field.info", settings.locale),
            announcement.info.toMarkdown().embedFieldSafe(),
            false
        )

        if (event.image.isNotBlank())
            builder.image(event.image)

        builder.footer(getEmbedMessage("announcement", "event.footer", settings.locale, announcement.id), null)

        return builder.build()
    }

    suspend fun viewAnnouncementEmbed(announcement: Announcement, settings: GuildSettings): EmbedCreateSpec {
        val builder = defaultEmbedBuilder(settings)
            .title(getEmbedMessage("announcement", "view.title", settings.locale))
            .addField(getEmbedMessage("announcement", "view.field.type", settings.locale), announcement.type.name, true)
            .addField(getEmbedMessage("announcement", "view.field.modifier", settings.locale), announcement.modifier.name, true)
            .addField(getEmbedMessage("announcement", "view.field.channel", settings.locale), "<#${announcement.channelId.asLong()}>", false)
            .addField(getEmbedMessage("announcement", "view.field.hours", settings.locale), "${announcement.hoursBefore}", true)
            .addField(getEmbedMessage("announcement", "view.field.minutes", settings.locale), "${announcement.minutesBefore}", true)

        if (!announcement.info.isNullOrBlank()) {
            builder.addField(getEmbedMessage("announcement", "view.field.info", settings.locale), announcement.info.toMarkdown().embedFieldSafe(), false)
        }

        builder.addField(getEmbedMessage("announcement", "view.field.calendar", settings.locale), "${announcement.calendarNumber}", true)

        if (announcement.type == Announcement.Type.RECUR || announcement.type == Announcement.Type.SPECIFIC)
            builder.addField(getEmbedMessage("announcement", "view.field.event", settings.locale), announcement.eventId!!, true)

        if (announcement.type == Announcement.Type.COLOR) {
            builder.color(announcement.eventColor.asColor())
            builder.addField(getEmbedMessage("announcement", "view.field.color", settings.locale), announcement.eventColor.name, true)
        } else builder.color(GlobalVal.discalColor)

        return builder.addField(getEmbedMessage("announcement", "view.field.id", settings.locale), announcement.id, false)
            .addField(getEmbedMessage("announcement", "view.field.enabled", settings.locale), "${announcement.enabled}", true)
            .addField(getEmbedMessage("announcement", "view.field.publish", settings.locale), "${announcement.publish}", true)
            .build()
    }

    suspend fun condensedAnnouncementEmbed(announcement: Announcement, settings: GuildSettings): EmbedCreateSpec {
        val builder = defaultEmbedBuilder(settings)
            .title(getEmbedMessage("announcement", "con.title", settings.locale))
            .addField(getEmbedMessage("announcement", "con.field.id", settings.locale), announcement.id, false)
            .addField(getEmbedMessage("announcement", "con.field.time", settings.locale), "${announcement.hoursBefore}H${announcement.minutesBefore}m", true)
            .addField(getEmbedMessage("announcement", "con.field.enabled", settings.locale), "${announcement.enabled}", true)
            .footer(getEmbedMessage("announcement", "con.footer", settings.locale, announcement.type.name, announcement.modifier.name), null)

        if (announcement.type == Announcement.Type.COLOR) builder.color(announcement.eventColor.asColor())
        else builder.color(GlobalVal.discalColor)

        return builder.build()
    }

    suspend fun announcementWizardEmbed(wizard: AnnouncementWizardState, settings: GuildSettings): EmbedCreateSpec {
        val announcement = wizard.entity

        val builder = defaultEmbedBuilder(settings)
            .title(getEmbedMessage("announcement", "wizard.title", settings.locale))
            .footer(getEmbedMessage("announcement", "wizard.footer", settings.locale), null)
            .color(announcement.eventColor.asColor())
            //fields
            .addField(getEmbedMessage("announcement", "wizard.field.type", settings.locale), announcement.type.name, true)
            .addField(getEmbedMessage("announcement", "wizard.field.modifier", settings.locale), announcement.modifier.name, true)

        if (announcement.type == Announcement.Type.COLOR) {
            if (announcement.eventColor == EventColor.NONE) builder.addField(
                getEmbedMessage("announcement", "wizard.field.color", settings.locale),
                getCommonMsg("embed.unset", settings.locale),
                false
            ) else builder.addField(
                getEmbedMessage("announcement", "wizard.field.color", settings.locale),
                announcement.eventColor.name,
                false
            )
        }

        if (announcement.type == Announcement.Type.SPECIFIC || announcement.type == Announcement.Type.RECUR) {
            if (announcement.eventId.isNullOrBlank()) builder.addField(
                getEmbedMessage("announcement", "wizard.field.event", settings.locale),
                getCommonMsg("embed.unset", settings.locale),
                false
            ) else builder.addField(
                getEmbedMessage("announcement", "wizard.field.event", settings.locale),
                announcement.eventId,
                false
            )
        }

        if (announcement.info.isNullOrBlank()) builder.addField(
            getEmbedMessage("announcement", "wizard.field.info", settings.locale),
            getCommonMsg("embed.unset", settings.locale),
            false
        ) else builder.addField(
            getEmbedMessage("announcement", "wizard.field.info", settings.locale),
            announcement.info.embedFieldSafe().toMarkdown(),
            false
        )

        builder.addField(getEmbedMessage("announcement", "wizard.field.channel", settings.locale), "<#${announcement.channelId.asLong()}>", false)
        builder.addField(getEmbedMessage("announcement", "wizard.field.minutes", settings.locale), "${announcement.minutesBefore}", true)
        builder.addField(getEmbedMessage("announcement", "wizard.field.hours", settings.locale), "${announcement.hoursBefore}", true)

        if (wizard.editing) builder.addField(getEmbedMessage("announcement", "wizard.field.id", settings.locale), announcement.id, false)
        else builder.addField(
            getEmbedMessage("announcement", "wizard.field.id", settings.locale),
            getCommonMsg("embed.unset", settings.locale),
            false
        )

        builder.addField(getEmbedMessage("announcement", "wizard.field.publish", settings.locale), "${announcement.publish}", true)
        builder.addField(getEmbedMessage("announcement", "wizard.field.enabled", settings.locale), "${announcement.enabled}", true)
        builder.addField(getEmbedMessage("announcement", "wizard.field.calendar", settings.locale), "${announcement.calendarNumber}", true)

        // Build up any warnings
        val warningsBuilder = StringBuilder()
        if ((announcement.type == Announcement.Type.SPECIFIC || announcement.type == Announcement.Type.RECUR) && announcement.eventId.isNullOrBlank())
            warningsBuilder.appendLine(getEmbedMessage("announcement", "warning.wizard.eventId", settings.locale)).appendLine()
        if (announcement.type == Announcement.Type.COLOR && announcement.eventColor == EventColor.NONE)
            warningsBuilder.appendLine(getEmbedMessage("announcement", "warning.wizard.color", settings.locale)).appendLine()
        if (announcement.getCalculatedTime() < Duration.ofMinutes(5))
            warningsBuilder.appendLine(getEmbedMessage("announcement", "warning.wizard.time", settings.locale)).appendLine()
        if (announcement.calendarNumber > settings.maxCalendars)
            warningsBuilder.appendLine(getEmbedMessage("announcement", "warning.wizard.calNum", settings.locale))



        if (warningsBuilder.isNotBlank()) {
            builder.addField(getEmbedMessage("announcement", "wizard.field.warnings", settings.locale), warningsBuilder.toString(), false)
        }

        return builder.build()
    }
}
