package org.dreamexposure.discal.client.message.embed

import discord4j.core.`object`.entity.Guild
import discord4j.core.spec.EmbedCreateSpec
import org.dreamexposure.discal.core.entities.Calendar
import org.dreamexposure.discal.core.extensions.embedDescriptionSafe
import org.dreamexposure.discal.core.extensions.embedFieldSafe
import org.dreamexposure.discal.core.extensions.embedTitleSafe
import org.dreamexposure.discal.core.extensions.toMarkdown
import org.dreamexposure.discal.core.`object`.calendar.PreCalendar
import org.dreamexposure.discal.core.`object`.new.GuildSettings
import org.dreamexposure.discal.core.utils.GlobalVal.discalColor
import org.dreamexposure.discal.core.utils.getCommonMsg

object CalendarEmbed : EmbedMaker {

    @Deprecated("Use replacement in EmbedService")
    fun link(guild: Guild, settings: GuildSettings, calendar: Calendar): EmbedCreateSpec {
        val builder = defaultBuilder(guild, settings)
        //Handle optional fields
        if (calendar.name.isNotBlank())
            builder.title(calendar.name.toMarkdown().embedTitleSafe())
        if (calendar.description.isNotBlank())
            builder.description(calendar.description.toMarkdown().embedDescriptionSafe())

        return builder.addField(getMessage("calendar", "link.field.timezone", settings), calendar.zoneName, false)
            .addField(getMessage("calendar", "link.field.host", settings), calendar.calendarData.host.name, true)
            .addField(getMessage("calendar", "link.field.number", settings), "${calendar.calendarNumber}", true)
            .addField(getMessage("calendar", "link.field.id", settings), calendar.calendarId, false)
            .url(calendar.link)
            .footer(getMessage("calendar", "link.footer.default", settings), null)
            .color(discalColor)
            .build()
    }

    @Deprecated("Prefer to use replacement in EmbedService")
    fun pre(guild: Guild, settings: GuildSettings, preCal: PreCalendar): EmbedCreateSpec {
        val builder = defaultBuilder(guild, settings)
            .title(getMessage("calendar", "wizard.title", settings))
            .addField(getMessage(
                "calendar", "wizard.field.name", settings),
                preCal.name.toMarkdown().embedFieldSafe(),
                false
            ).addField(
                getMessage("calendar", "wizard.field.description", settings),
                preCal.description?.ifEmpty { getCommonMsg("embed.unset", settings.locale) }?.toMarkdown()?.embedFieldSafe()
                    ?: getCommonMsg("embed.unset", settings.locale),
                false
            ).addField(getMessage("calendar", "wizard.field.timezone", settings),
                preCal.timezone?.id ?: getCommonMsg("embed.unset", settings.locale),
                true
            ).addField(getMessage("calendar", "wizard.field.host", settings), preCal.host.name, true)
            .footer(getMessage("calendar", "wizard.footer", settings), null)

        if (preCal.editing)
            builder.addField(getMessage("calendar", "wizard.field.id", settings), preCal.calendar!!.calendarId, false)

        return builder.build()
    }
}
