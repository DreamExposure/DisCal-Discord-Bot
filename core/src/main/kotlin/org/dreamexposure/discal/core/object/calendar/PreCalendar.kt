package org.dreamexposure.discal.core.`object`.calendar

import discord4j.common.util.Snowflake
import discord4j.core.`object`.entity.Guild
import org.dreamexposure.discal.core.entities.Calendar
import org.dreamexposure.discal.core.entities.spec.create.CreateCalendarSpec
import org.dreamexposure.discal.core.entities.spec.update.UpdateCalendarSpec
import org.dreamexposure.discal.core.enums.calendar.CalendarHost
import org.dreamexposure.discal.core.extensions.discord4j.determineNextCalendarNumber
import reactor.core.publisher.Mono
import java.time.Instant
import java.time.ZoneId

@Suppress("DataClassPrivateConstructor")
data class PreCalendar private constructor(
        val guildId: Snowflake,
        val host: CalendarHost,
        var name: String,
        val editing: Boolean = false
) {
    var description: String? = null

    var timezone: ZoneId? = null

    var calendar: Calendar? = null

    var lastEdit: Instant = Instant.now()


    fun hasRequiredValues(): Boolean {
        return this.name.isNotEmpty() && this.timezone != null
    }

    fun createSpec(guild: Guild): Mono<CreateCalendarSpec> {
        return guild.determineNextCalendarNumber().map { CreateCalendarSpec(host, it, name, description, timezone!!) }
    }

    fun updateSpec() = UpdateCalendarSpec(name, description, timezone)

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
