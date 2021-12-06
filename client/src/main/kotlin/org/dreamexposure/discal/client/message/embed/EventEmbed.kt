package org.dreamexposure.discal.client.message.embed

import discord4j.core.`object`.entity.Guild
import discord4j.core.spec.EmbedCreateSpec
import org.dreamexposure.discal.core.`object`.GuildSettings
import org.dreamexposure.discal.core.`object`.event.PreEvent
import org.dreamexposure.discal.core.entities.Event
import org.dreamexposure.discal.core.enums.time.DiscordTimestampFormat.LONG_DATETIME
import org.dreamexposure.discal.core.extensions.*
import org.dreamexposure.discal.core.utils.getCommonMsg

object EventEmbed : EmbedMaker {
    fun getFull(guild: Guild, settings: GuildSettings, event: Event): EmbedCreateSpec {
        val builder = defaultBuilder(guild, settings)
                .footer(getMessage("event", "full.footer", settings, event.eventId), null)
                .color(event.color.asColor())

        if (event.name.isNotBlank())
            builder.title(event.name.toMarkdown().embedTitleSafe())
        if (event.description.isNotBlank())
            builder.description(event.description.toMarkdown().embedDescriptionSafe())

        builder.addField(
                getMessage("event", "full.field.start", settings),
                event.start.asDiscordTimestamp(LONG_DATETIME),
                true)
        builder.addField(
                getMessage("event", "full.field.end", settings),
                event.end.asDiscordTimestamp(LONG_DATETIME),
                true
        )

        if (event.location.isNotBlank()) builder.addField(
                getMessage("event", "full.field.location", settings),
                event.location.toMarkdown().embedFieldSafe(),
                false
        )

        builder.addField(getMessage("event", "full.field.cal", settings), "${event.calendar.calendarNumber}", false)

        if (event.image.isNotEmpty())
            builder.image(event.image)

        return builder.build()
    }

    fun getCondensed(guild: Guild, settings: GuildSettings, event: Event): EmbedCreateSpec {
        val builder = defaultBuilder(guild, settings)
                .footer(getMessage("event", "con.footer", settings, event.eventId), null)
                .color(event.color.asColor())

        if (event.name.isNotBlank())
            builder.title(event.name.toMarkdown().embedTitleSafe())

        builder.addField(
                getMessage("event", "con.field.start", settings),
                event.start.asDiscordTimestamp(LONG_DATETIME),
                true
        )

        if (event.location.isNotBlank()) builder.addField(
                getMessage("event", "con.field.location", settings),
                event.location.toMarkdown().embedFieldSafe(),
                false
        )

        if (event.image.isNotBlank())
            builder.thumbnail(event.image)

        return builder.build()
    }

    fun pre(guild: Guild, settings: GuildSettings, event: PreEvent): EmbedCreateSpec {
        val builder = defaultBuilder(guild, settings)
                .title(getMessage("event", "wizard.title", settings))
                .footer(getMessage("event", "wizard.footer", settings), null)
                .color(event.color.asColor())

        if (!event.name.isNullOrBlank()) builder.addField(
                getMessage("event", "wizard.field.name", settings),
                event.name!!.toMarkdown().embedFieldSafe(),
                false
        ) else builder.addField(
                getMessage("event", "wizard.field.name", settings),
                getCommonMsg("embed.unset", settings),
                false
        )

        if (!event.description.isNullOrBlank()) builder.addField(
                getMessage("event", "wizard.field.desc", settings),
                event.description!!.toMarkdown().embedFieldSafe(),
                false
        ) else builder.addField(
                getMessage("event", "wizard.field.desc", settings),
                getCommonMsg("embed.unset", settings),
                false
        )

        if (!event.location.isNullOrBlank()) builder.addField(
                getMessage("event", "wizard.field.location", settings),
                event.location!!.toMarkdown().embedFieldSafe(),
                false
        ) else builder.addField(
                getMessage("event", "wizard.field.location", settings),
                getCommonMsg("embed.unset", settings),
                false
        )

        if (event.start != null) builder.addField(
                getMessage("event", "wizard.field.start", settings),
                event.start!!.humanReadableFull(event.timezone, settings.timeFormat),
                true
        ) else builder.addField(
                getMessage("event", "wizard.field.start", settings),
                getCommonMsg("embed.unset", settings),
                true
        )

        if (event.end != null) builder.addField(
                getMessage("event", "wizard.field.end", settings),
                event.end!!.humanReadableFull(event.timezone, settings.timeFormat),
                true
        ) else builder.addField(
                getMessage("event", "wizard.field.end", settings),
                getCommonMsg("embed.unset", settings),
                true
        )

        if (event.recurrence != null) builder.addField(
                getMessage("event", "wizard.field.recurrence", settings),
                event.recurrence!!.toHumanReadable(),
                true
        ) else builder.addField(
                getMessage("event", "wizard.field.recurrence", settings),
                getCommonMsg("embed.unset", settings),
                true
        )

        builder.addField(getMessage("event", "wizard.field.timezone", settings), event.timezone.id, false)

        if (event.editing)
            builder.addField(getMessage("event", "wizard.field.id", settings), event.eventId!!, true)
        builder.addField(getMessage("event", "wizard.field.calendar", settings), event.calNumber.toString(), true)

        if (event.image != null)
            builder.image(event.image!!)

        val warnings = event.generateWarnings(settings)
        if (warnings.isNotEmpty()) {
            val warnText = "```fix\n${warnings.joinToString("\n")}\n```"
            builder.addField(getMessage("event", "wizard.field.warnings", settings), warnText, false)
        }

        return builder.build()
    }
}
