package org.dreamexposure.discal.client.message.embed

import discord4j.core.`object`.entity.Guild
import discord4j.core.spec.EmbedCreateSpec
import org.dreamexposure.discal.core.`object`.GuildSettings
import org.dreamexposure.discal.core.extensions.discord4j.getCalendar
import org.dreamexposure.discal.core.utils.GlobalVal.discalColor
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object CalendarEmbed : EmbedMaker {
    fun getLinkCalEmbed(guild: Guild, settings: GuildSettings, calNumber: Int): Mono<EmbedCreateSpec> {
        return guild.getCalendar(calNumber).map { cal ->
            val builder = defaultBuilder(guild, settings)

            builder.title(getMessage("calendar", "link.title", settings))
            builder.addField(getMessage("calendar", "link.field.name", settings), cal.name, false)
            builder.addField(getMessage("calendar", "link.field.description", settings), cal.description, false)
            builder.addField(getMessage("calendar", "link.field.timezone", settings), cal.zoneName, false)

            builder.addField(getMessage("calendar", "link.field.host", settings), cal.calendarData.host.name, true)
            builder.addField(getMessage("calendar", "link.field.number", settings), "${cal.calendarNumber}", true)
            builder.addField(getMessage("calendar", "link.field.id", settings), cal.calendarId, false)
            builder.url(cal.link)
            builder.footer(getMessage("calendar", "link.footer", settings), null)
            builder.color(discalColor)

            builder.build()
        }
    }

    fun getTimeEmbed(guild: Guild, settings: GuildSettings, calNumber: Int): Mono<EmbedCreateSpec> {
        return guild.getCalendar(calNumber).map { cal ->
            val ldt = LocalDateTime.now(cal.timezone)

            val fmt: DateTimeFormatter =
                  if (settings.twelveHour)
                      DateTimeFormatter.ofPattern("yyyy/MM/dd hh:mm:ss a")
                  else
                      DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")


            val correctTime = fmt.format(ldt)
            val builder = defaultBuilder(guild, settings)

            builder.title(getMessage("time", "embed.title", settings))
            builder.addField(getMessage("time", "embed.field.current", settings), correctTime, false)
            builder.addField(getMessage("time", "embed.field.timezone", settings), cal.zoneName, false)
            builder.footer(getMessage("time", "embed.footer", settings), null)
            builder.url(cal.link)

            builder.color(discalColor)

            builder.build()
        }
    }
}
