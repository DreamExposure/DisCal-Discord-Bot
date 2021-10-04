package org.dreamexposure.discal.client.message.embed

import discord4j.core.`object`.entity.Guild
import discord4j.core.spec.EmbedCreateSpec
import org.dreamexposure.discal.core.`object`.GuildSettings
import org.dreamexposure.discal.core.`object`.calendar.PreCalendar
import org.dreamexposure.discal.core.enums.time.TimeFormat
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
                if (settings.timeFormat == TimeFormat.TWELVE_HOUR)
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

    fun pre(guild: Guild, settings: GuildSettings, preCal: PreCalendar): EmbedCreateSpec {
        val builder = defaultBuilder(guild, settings)
            .title(getMessage("calendar", "wizard.title", settings))
            .addField(getMessage("calendar", "wizard.field.name", settings), preCal.name, false)
            .addField(getMessage("calendar", "wizard.field.description", settings), preCal.description, false)
            .addField(getMessage("calendar", "wizard.field.timezone", settings), preCal.timezone?.id ?: "UNSET", true)
            .addField(getMessage("calendar", "wizard.field.host", settings), preCal.host.name, true)
            .footer(getMessage("calendar", "wizard.footer", settings), null)

        if (preCal.editing)
            builder.addField(getMessage("calendar", "wizard.field.id", settings), preCal.calendar!!.calendarId, false)

        return builder.build()
    }
}
