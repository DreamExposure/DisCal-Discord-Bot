package org.dreamexposure.discal.core.`object`.calendar

import discord4j.common.util.Snowflake
import org.dreamexposure.discal.core.entities.Calendar
import org.dreamexposure.discal.core.enums.calendar.CalendarHost
import java.time.Instant
import java.time.ZoneId

@Suppress("DataClassPrivateConstructor")
data class PreCalendar private constructor(
        val guildId: Snowflake,
        val host: CalendarHost,
        var name: String,
        val editing: Boolean = false
) {
    var description: String = ""

    var timezone: ZoneId? = null

    var calendar: Calendar? = null

    var lastEdit: Instant = Instant.now()

    fun hasRequiredValues(): Boolean {
        return this.name.isNotEmpty() && this.timezone != null
    }

    companion object {
        fun new(guildId: Snowflake, host: CalendarHost, name: String) = PreCalendar(guildId, host, name)

        fun edit(calendar: Calendar): PreCalendar {
            val pre = PreCalendar(calendar.guildId, calendar.calendarData.host, calendar.name, true)

            pre.calendar = calendar
            pre.description = calendar.description
            pre.timezone = calendar.timezone

            return pre
        }
    }
}
