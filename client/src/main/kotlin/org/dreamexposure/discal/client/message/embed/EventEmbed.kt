package org.dreamexposure.discal.client.message.embed

import discord4j.core.`object`.entity.Guild
import discord4j.core.spec.EmbedCreateSpec
import org.dreamexposure.discal.core.`object`.GuildSettings
import org.dreamexposure.discal.core.`object`.event.PreEvent
import org.dreamexposure.discal.core.entities.Event
import org.dreamexposure.discal.core.extensions.asDiscordTimestamp
import org.dreamexposure.discal.core.extensions.humanReadableFull
import org.dreamexposure.discal.core.extensions.toMarkdown
import org.dreamexposure.discal.core.utils.getCommonMsg

object EventEmbed : EmbedMaker {
    fun getFull(guild: Guild, settings: GuildSettings, event: Event): EmbedCreateSpec {
        val builder = defaultBuilder(guild, settings)
            .title(getMessage("event", "full.title", settings))
            .footer(getMessage("event", "full.footer", settings, event.eventId), null)
            .color(event.color.asColor())

        if (event.name.isNotEmpty())
            builder.addField(getMessage("event", "full.field.name", settings), event.name.toMarkdown(), false)
        if (event.description.isNotEmpty())
            builder.addField(getMessage("event", "full.field.desc", settings), event.description.toMarkdown(), false)

        builder.addField(getMessage("event", "full.field.start", settings), event.start.asDiscordTimestamp(), true)
        builder.addField(getMessage("event", "full.field.end", settings), event.end.asDiscordTimestamp(), true)

        if (event.location.isNotEmpty())
            builder.addField(getMessage("event", "full.field.location", settings), event.location.toMarkdown(), false)

        builder.addField(getMessage("event", "full.field.cal", settings), "${event.calendar.calendarNumber}", false)

        if (event.image.isNotEmpty())
            builder.image(event.image)

        return builder.build()
    }

    fun getCondensed(guild: Guild, settings: GuildSettings, event: Event): EmbedCreateSpec {
        val builder = defaultBuilder(guild, settings)
            .title(getMessage("event", "con.title", settings))
            .footer(getMessage("event", "con.footer", settings, event.eventId), null)
            .color(event.color.asColor())

        if (event.name.isNotEmpty())
            builder.addField(getMessage("event", "con.field.name", settings), event.name.toMarkdown(), false)

        builder.addField(getMessage("event", "con.field.start", settings), event.start.asDiscordTimestamp(), true)

        if (event.location.isNotEmpty())
            builder.addField(getMessage("event", "con.field.location", settings), event.location.toMarkdown(), false)

        if (event.image.isNotEmpty())
            builder.thumbnail(event.image)

        return builder.build()
    }

    fun pre(guild: Guild, settings: GuildSettings, event: PreEvent): EmbedCreateSpec {
        val builder = defaultBuilder(guild, settings)
            .title(getMessage("event", "wizard.title", settings))
            .footer(getMessage("event", "wizard.footer", settings), null)
            .color(event.color.asColor())

        if (!event.name.isNullOrBlank())
            builder.addField(getMessage("event", "wizard.field.name", settings), event.name!!.toMarkdown(), false)
        else builder.addField(
            getMessage("event", "wizard.field.name", settings),
            getCommonMsg("embed.unset", settings),
            false
        )

        if (!event.description.isNullOrBlank()) builder.addField(
            getMessage("event", "wizard.field.desc", settings),
            event.description!!.toMarkdown(),
            false
        ) else builder.addField(
            getMessage("event", "wizard.field.desc", settings),
            getCommonMsg("embed.unset", settings),
            false
        )

        if (!event.location.isNullOrBlank()) builder.addField(
            getMessage("event", "wizard.field.location", settings),
            event.location!!.toMarkdown(),
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
            getMessage("event", "wizard.start", settings),
            getCommonMsg("embed.unset", settings),
            true
        )

        if (event.end != null) builder.addField(
            getMessage("event", "wizard.field.end", settings),
            event.end!!.humanReadableFull(event.timezone, settings.timeFormat),
            true
        )

        if (event.recurrence != null) builder.addField(
            getMessage("event", "wizard.field.recurrence", settings),
            event.recurrence!!.toHumanReadable(),
            true
        )

        builder.addField(getMessage("event", "wizard.field.timezone", settings), event.timezone.id, false)

        if (event.editing)
            builder.addField(getMessage("event", "wizard.field.id", settings), event.eventId!!, true)
        builder.addField(getMessage("event", "wizard.field.calendar", settings), event.calNumber.toString(), true)

        return builder.build()
    }
}
