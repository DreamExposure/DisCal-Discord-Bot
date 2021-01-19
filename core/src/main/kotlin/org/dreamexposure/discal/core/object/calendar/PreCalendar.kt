package org.dreamexposure.discal.core.`object`.calendar

import com.google.api.services.calendar.model.Calendar
import discord4j.common.util.Snowflake
import discord4j.core.`object`.entity.Message

@Suppress("DataClassPrivateConstructor")
data class PreCalendar private constructor(
        val guildId: Snowflake,
        val editing: Boolean = false
) {
    constructor(guildId: Snowflake, summary: String) : this(guildId, false) {
        this.summary = summary
    }

    constructor(guildId: Snowflake, calendar: Calendar, editing: Boolean) : this(guildId, editing) {
        this.summary = calendar.summary

        if (calendar.description != null) this.description = calendar.description
        if (calendar.timeZone != null) this.timezone = calendar.timeZone
    }

    var summary: String = ""
    var description: String = ""
    var timezone: String? = null

    var calendarId: String? = null
    var calendarData: CalendarData? = null

    var lastEdit: Long = System.currentTimeMillis()
    var creatorMessage: Message? = null

    fun hasRequiredValues(): Boolean {
        return this.summary.isNotEmpty() && this.timezone != null
    }
}
