package org.dreamexposure.discal.client.message.embed

import discord4j.core.`object`.entity.Guild
import discord4j.core.spec.EmbedCreateSpec
import org.dreamexposure.discal.core.`object`.GuildSettings
import org.dreamexposure.discal.core.entities.Event
import org.dreamexposure.discal.core.extensions.asDiscordTimestamp
import org.dreamexposure.discal.core.extensions.toMarkdown

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
}
