package org.dreamexposure.discal.core.`object`.event

import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventDateTime
import discord4j.common.util.Snowflake
import discord4j.core.`object`.entity.Message
import org.dreamexposure.discal.core.`object`.calendar.CalendarData
import org.dreamexposure.discal.core.database.DatabaseManager
import org.dreamexposure.discal.core.enums.event.EventColor

@Suppress("DataClassPrivateConstructor")
data class PreEvent private constructor(
        val guildId: Snowflake,
        val eventId: String,
        val calNumber: Int,
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
    constructor(guildId: Snowflake, calNumber: Int) : this(guildId, "N/a", calNumber)

    constructor(guildId: Snowflake, e: Event, calData: CalendarData) : this(guildId, e.id, calData.calendarNumber) {
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

        if (e.start.timeZone != null) this.timezone = e.start.timeZone

        this.eventData = DatabaseManager.getEventData(this.guildId, e.id).block()!!
    }

    //Functions
    fun hasRequiredValues(): Boolean = this.startDateTime != null && this.endDateTime != null
}
