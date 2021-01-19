package org.dreamexposure.discal.core.`object`.event

import com.google.api.services.calendar.model.Calendar
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventDateTime
import discord4j.common.util.Snowflake
import discord4j.core.`object`.entity.Message
import org.dreamexposure.discal.core.database.DatabaseManager
import org.dreamexposure.discal.core.enums.event.EventColor
import org.dreamexposure.discal.core.logger.LogFeed
import org.dreamexposure.discal.core.logger.`object`.LogObject
import org.dreamexposure.discal.core.wrapper.google.CalendarWrapper

@Suppress("DataClassPrivateConstructor")
data class PreEvent private constructor(
        val guildId: Snowflake,
        val eventId: String,
) {
    //fields
    var summary: String? = null
    var description: String? = null
    var startDateTime: EventDateTime? = null
    var endDateTime: EventDateTime? = null

    var timezone = "Unknown"

    var color = EventColor.NONE

    var location: String? = null

    var recur = false
    var recurrence = Recurrence()

    var eventData = EventData(this.guildId)

    //Wizards
    var editing = false
    var creatorMessage: Message? = null
    var lastEdit = System.currentTimeMillis()

    //Constructors
    constructor(guildId: Snowflake) : this(guildId, "N/a")

    constructor(guildId: Snowflake, e: Event) : this(guildId, e.id) {
        this.color = EventColor.fromNameOrHexOrId(e.colorId)

        if (e.recurrence != null && e.recurrence.isNotEmpty()) {
            this.recur = true
            this.recurrence = Recurrence.fromRRule(e.recurrence[0])
        }

        if (e.summary != null) this.summary = e.summary
        if (e.description != null) this.description = e.description
        if (e.location != null) this.location = e.location

        this.startDateTime = e.start
        this.endDateTime = e.end

        //Here is where I need to fix the display times
        //TODO: Get rid of the blocking
        val settings = DatabaseManager.getSettings(this.guildId).block()!!
        //TODO: Support multi-cal
        val data = DatabaseManager.getMainCalendar(this.guildId).block()!!

        var cal: Calendar? = null
        try {
            cal = CalendarWrapper.getCalendar(data, settings).block()
        } catch (ex: Exception) {
            LogFeed.log(LogObject.forException("Failed to get proper date/time for event!", ex, this.javaClass))
        }

        if (cal != null) this.timezone = cal.timeZone
        else this.timezone = "ERROR/Unknown"

        this.eventData = DatabaseManager.getEventData(this.guildId, e.id).block()!!
    }

    //Functions
    fun hasRequiredValues(): Boolean = this.startDateTime != null && this.endDateTime != null
}
