package org.dreamexposure.discal.client.message.embed

import discord4j.core.`object`.entity.Guild
import discord4j.core.spec.EmbedCreateSpec
import discord4j.rest.util.Image
import org.dreamexposure.discal.client.message.Messages
import org.dreamexposure.discal.core.`object`.BotSettings
import org.dreamexposure.discal.core.`object`.GuildSettings
import org.dreamexposure.discal.core.extensions.discord4j.getCalendar
import org.dreamexposure.discal.core.utils.GlobalVal.discalColor
import org.dreamexposure.discal.core.utils.GlobalVal.iconUrl
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object CalendarEmbed {
    fun getLinkCalEmbed(guild: Guild, settings: GuildSettings, calNumber: Int): Mono<EmbedCreateSpec> {
        return guild.getCalendar(calNumber).map { cal ->
            val builder = EmbedCreateSpec.builder()

            if (settings.branded)
                builder.author(guild.name, BotSettings.BASE_URL.get(), guild.getIconUrl(Image.Format.PNG).orElse(iconUrl))
            else
                builder.author("DisCal", BotSettings.BASE_URL.get(), iconUrl)

            builder.title(Messages.getMessage("Embed.Calendar.Link.Title", settings))
            builder.addField(Messages.getMessage("Embed.Calendar.Link.Summary", settings), cal.name, true)
            builder.addField(Messages.getMessage("Embed.Calendar.Link.Description", settings), cal.description, true)

            builder.addField(Messages.getMessage("Embed.Calendar.Link.TimeZone", settings), cal.zoneName, false)
            builder.url(cal.link)
            builder.footer(Messages.getMessage("Embed.Calendar.Link.CalendarId", "%id%", cal.calendarId, settings), null)
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

            val builder = EmbedCreateSpec.builder()

            if (settings.branded)
                builder.author(guild.name, BotSettings.BASE_URL.get(), guild.getIconUrl(Image.Format.PNG).orElse(iconUrl))
            else
                builder.author("DisCal", BotSettings.BASE_URL.get(), iconUrl)

            builder.title(Messages.getMessage("Embed.Time.Title", settings))
            builder.addField(Messages.getMessage("Embed.Time.Time", settings), correctTime, false)
            builder.addField(Messages.getMessage("Embed.Time.TimeZone", settings), cal.zoneName, false)
            builder.footer(Messages.getMessage("Embed.Time.Footer", settings), null)
            builder.url(cal.link)

            builder.color(discalColor)

            builder.build()
        }
    }
}
