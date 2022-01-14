package org.dreamexposure.discal.client.message.embed

import discord4j.core.`object`.entity.Guild
import discord4j.core.spec.EmbedCreateSpec
import org.dreamexposure.discal.core.`object`.GuildSettings
import org.dreamexposure.discal.core.`object`.announcement.Announcement
import org.dreamexposure.discal.core.entities.Event
import org.dreamexposure.discal.core.enums.announcement.AnnouncementStyle
import org.dreamexposure.discal.core.enums.announcement.AnnouncementType
import org.dreamexposure.discal.core.enums.event.EventColor
import org.dreamexposure.discal.core.enums.time.DiscordTimestampFormat.LONG_DATETIME
import org.dreamexposure.discal.core.extensions.asDiscordTimestamp
import org.dreamexposure.discal.core.extensions.discord4j.getSettings
import org.dreamexposure.discal.core.extensions.embedFieldSafe
import org.dreamexposure.discal.core.extensions.toMarkdown
import org.dreamexposure.discal.core.utils.GlobalVal
import org.dreamexposure.discal.core.utils.getCommonMsg
import reactor.core.publisher.Mono

object AnnouncementEmbed : EmbedMaker {
    fun determine(ann: Announcement, event: Event, guild: Guild): Mono<EmbedCreateSpec> {
        return guild.getSettings().map { settings ->
            when (settings.announcementStyle) {
                AnnouncementStyle.FULL -> full(ann, event, guild, settings)
                AnnouncementStyle.SIMPLE -> simple(ann, event, guild, settings)
                AnnouncementStyle.EVENT -> event(ann, event, guild, settings)
            }
        }
    }

    private fun full(ann: Announcement, event: Event, guild: Guild, settings: GuildSettings): EmbedCreateSpec {
        val builder = defaultBuilder(guild, settings)
            .color(event.color.asColor())
            .title(getMessage("announcement", "full.title", settings))

        if (event.name.isNotBlank()) builder.addField(
            getMessage("announcement", "full.field.name", settings),
            event.name.toMarkdown().embedFieldSafe(),
            false
        )
        if (event.description.isNotBlank()) builder.addField(
            getMessage("announcement", "full.field.desc", settings),
            event.description.toMarkdown().embedFieldSafe(),
            false
        )

        builder.addField(
            getMessage("announcement", "full.field.start", settings),
            event.start.asDiscordTimestamp(LONG_DATETIME),
            true
        )
        builder.addField(
            getMessage("announcement", "full.field.end", settings),
            event.end.asDiscordTimestamp(LONG_DATETIME),
            true
        )

        if (event.location.isNotBlank()) builder.addField(
            getMessage("announcement", "full.field.location", settings),
            event.location.toMarkdown().embedFieldSafe(),
            false
        )

        if (ann.info.isNotBlank() && !ann.info.equals("None", true)) builder.addField(
            getMessage("announcement", "full.field.info", settings),
            ann.info.toMarkdown().embedFieldSafe(),
            false
        )

        builder.addField(
            getMessage("announcement", "full.field.calendar", settings),
            "${event.calendar.calendarNumber}",
            true
        )
        builder.addField(getMessage("announcement", "full.field.event", settings), event.eventId, true)

        if (event.image.isNotBlank())
            builder.image(event.image)

        builder.footer(getMessage("announcement", "full.footer", settings, ann.id), null)

        return builder.build()
    }

    private fun simple(ann: Announcement, event: Event, guild: Guild, settings: GuildSettings): EmbedCreateSpec {
        val builder = defaultBuilder(guild, settings)
            .color(event.color.asColor())
            .title(getMessage("announcement", "simple.title", settings))

        if (event.name.isNotBlank()) builder.addField(
            getMessage("announcement", "simple.field.name", settings),
            event.name.toMarkdown().embedFieldSafe(),
            false
        )
        if (event.description.isNotBlank()) builder.addField(
            getMessage("announcement", "simple.field.desc", settings),
            event.description.toMarkdown().embedFieldSafe(),
            false
        )

        builder.addField(
            getMessage("announcement", "simple.field.start", settings),
            event.start.asDiscordTimestamp(LONG_DATETIME),
            true
        )

        if (event.location.isNotBlank()) builder.addField(
            getMessage("announcement", "simple.field.location", settings),
            event.location.toMarkdown().embedFieldSafe(),
            false
        )

        if (ann.info.isNotBlank() && !ann.info.equals("None", true)) builder.addField(
            getMessage("announcement", "simple.field.info", settings),
            ann.info.toMarkdown().embedFieldSafe(),
            false
        )

        if (event.image.isNotEmpty())
            builder.image(event.image)

        builder.footer(getMessage("announcement", "simple.footer", settings, ann.id), null)

        return builder.build()
    }

    fun event(ann: Announcement, event: Event, guild: Guild, settings: GuildSettings): EmbedCreateSpec {
        val builder = defaultBuilder(guild, settings)
            .color(event.color.asColor())
            .title(getMessage("announcement", "event.title", settings))

        if (event.name.isNotBlank()) builder.addField(
            getMessage("announcement", "event.field.name", settings),
            event.name.toMarkdown().embedFieldSafe(),
            false
        )
        if (event.description.isNotBlank()) builder.addField(
            getMessage("announcement", "event.field.desc", settings),
            event.description.toMarkdown().embedFieldSafe(),
            false
        )

        builder.addField(
            getMessage("announcement", "event.field.start", settings),
            event.start.asDiscordTimestamp(LONG_DATETIME),
            true
        )
        builder.addField(
            getMessage("announcement", "event.field.end", settings),
            event.end.asDiscordTimestamp(LONG_DATETIME),
            true
        )

        if (event.location.isNotBlank()) builder.addField(
            getMessage("announcement", "event.field.location", settings),
            event.location.toMarkdown().embedFieldSafe(),
            false
        )

        builder.addField(
            getMessage("announcement", "event.field.calendar", settings),
            "${event.calendar.calendarNumber}",
            true
        )
        builder.addField(getMessage("announcement", "event.field.event", settings), event.eventId, true)

        if (ann.info.isNotBlank() && !ann.info.equals("None", true)) builder.addField(
            getMessage("announcement", "event.field.info", settings),
            ann.info.toMarkdown().embedFieldSafe(),
            false
        )

        if (event.image.isNotBlank())
            builder.image(event.image)

        builder.footer(getMessage("announcement", "event.footer", settings, ann.id), null)

        return builder.build()
    }

    fun condensed(ann: Announcement, guild: Guild, settings: GuildSettings): EmbedCreateSpec {
        val builder = defaultBuilder(guild, settings)
            .title(getMessage("announcement", "con.title", settings))
            .addField(getMessage("announcement", "con.field.id", settings), ann.id, false)
            .addField(getMessage("announcement", "con.field.time", settings), condensedTime(ann), true)
            .addField(getMessage("announcement", "con.field.enabled", settings), "${ann.enabled}", true)
            .footer(getMessage("announcement", "con.footer", settings, ann.type.name, ann.modifier.name), null)

        if (ann.type == AnnouncementType.COLOR)
            builder.color(ann.eventColor.asColor())
        else
            builder.color(GlobalVal.discalColor)

        return builder.build()
    }

    fun view(ann: Announcement, guild: Guild, settings: GuildSettings): EmbedCreateSpec {
        val builder = defaultBuilder(guild, settings)
            .title(getMessage("announcement", "view.title", settings))
            .addField(getMessage("announcement", "view.field.type", settings), ann.type.name, true)
            .addField(getMessage("announcement", "view.field.modifier", settings), ann.modifier.name, true)
            .addField(getMessage("announcement", "view.field.channel", settings), "<${ann.announcementChannelId}>", false)
            .addField(getMessage("announcement", "view.field.hours", settings), "${ann.hoursBefore}", true)
            .addField(getMessage("announcement", "view.field.minutes", settings), "${ann.minutesBefore}", true)

        if (ann.info.isNotBlank() && !ann.info.equals("None", true)) {
            builder.addField(getMessage("announcement", "view.field.info", settings), ann.info.toMarkdown().embedFieldSafe(), false)
        }

        builder.addField(getMessage("announcement", "view.field.calendar", settings), "${ann.calendarNumber}", true)
        if (ann.type == AnnouncementType.RECUR || ann.type == AnnouncementType.SPECIFIC)
            builder.addField(getMessage("announcement", "view.field.event", settings), ann.eventId, true)

        if (ann.type == AnnouncementType.COLOR) {
            builder.color(ann.eventColor.asColor())
            builder.addField(getMessage("announcement", "view.field.color", settings), ann.eventColor.name, true)
        } else
            builder.color(GlobalVal.discalColor)

        return builder.addField(getMessage("announcement", "view.field.id", settings), ann.id, false)
            .addField(getMessage("announcement", "view.field.enabled", settings), "${ann.enabled}", true)
            .addField(getMessage("announcement", "view.field.publish", settings), "${ann.publish}", true)
            .build()
    }

    fun pre(guild: Guild, ann: Announcement, settings: GuildSettings): EmbedCreateSpec {
        val builder = defaultBuilder(guild, settings)
            .title(getMessage("announcement", "wizard.title", settings))
            .footer(getMessage("announcement", "wizard.footer", settings), null)
            .color(ann.eventColor.asColor())
            //fields
            .addField(getMessage("announcement", "wizard.field.type", settings), ann.type.name, true)
            .addField(getMessage("announcement", "wizard.field.modifier", settings), ann.modifier.name, true)

        if (ann.type == AnnouncementType.COLOR) {
            if (ann.eventColor == EventColor.NONE) builder.addField(
                getMessage("announcement", "wizard.field.color", settings),
                getCommonMsg("embed.unset", settings),
                false
            ) else builder.addField(
                getMessage("announcement", "wizard.field.color", settings),
                ann.eventColor.name,
                false
            )
        }

        if (ann.type == AnnouncementType.SPECIFIC || ann.type == AnnouncementType.RECUR) {
            if (ann.eventId == "N/a") builder.addField(
                getMessage("announcement", "wizard.field.event", settings),
                getCommonMsg("embed.unset", settings),
                false
            ) else builder.addField(
                getMessage("announcement", "wizard.field.event", settings),
                ann.eventId,
                false
            )
        }

        if (ann.info == "None") builder.addField(
            getMessage("announcement", "wizard.field.info", settings),
            getCommonMsg("embed.unset", settings),
            false
        ) else builder.addField(
            getMessage("announcement", "wizard.field.info", settings),
            ann.info.embedFieldSafe().toMarkdown(),
            false
        )

        if (ann.announcementChannelId == "N/a") builder.addField(
            getMessage("announcement", "wizard.field.channel", settings),
            getCommonMsg("embed.unset", settings),
            false
        ) else builder.addField(
            getMessage("announcement", "wizard.field.channel", settings),
            "<#${ann.announcementChannelId}>",
            false
        )

        builder.addField(getMessage("announcement", "wizard.field.minutes", settings), "${ann.minutesBefore}", true)
        builder.addField(getMessage("announcement", "wizard.field.hours", settings), "${ann.hoursBefore}", true)

        if (ann.editing) builder.addField(getMessage("announcement", "wizard.field.id", settings), ann.id, false)
        else builder.addField(
            getMessage("announcement", "wizard.field.id", settings),
            getCommonMsg("embed.unset", settings),
            false
        )

        builder.addField(getMessage("announcement", "wizard.field.publish", settings), "${ann.publish}", true)
        builder.addField(getMessage("announcement", "wizard.field.enabled", settings), "${ann.enabled}", true)
        builder.addField(getMessage("announcement", "wizard.field.calendar", settings), "${ann.calendarNumber}", true)

        val warnings = ann.generateWarnings(settings)
        if (warnings.isNotEmpty()) {
            val warnText = "```fix\n${warnings.joinToString("\n")}\n```"
            builder.addField(getMessage("announcement", "wizard.field.warnings", settings), warnText, false)
        }

        return builder.build()
    }

    private fun condensedTime(a: Announcement): String = "${a.hoursBefore}H${a.minutesBefore}m"
}
