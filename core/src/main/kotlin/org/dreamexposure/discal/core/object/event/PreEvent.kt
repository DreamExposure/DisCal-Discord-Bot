package org.dreamexposure.discal.core.`object`.event

import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventDateTime
import discord4j.common.util.Snowflake
import discord4j.core.`object`.entity.Message
import org.dreamexposure.discal.core.`object`.calendar.CalendarData
import org.dreamexposure.discal.core.database.DatabaseManager
import org.dreamexposure.discal.core.enums.event.EventColor
import org.dreamexposure.discal.core.utils.TimeUtils
import org.dreamexposure.discal.core.wrapper.google.CalendarWrapper
import reactor.core.publisher.Mono
import java.time.ZoneId

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

    private constructor(guildId: Snowflake, e: Event, calData: CalendarData) : this(guildId, e.id, calData
            .calendarNumber) {
        try {
            this.color = EventColor.fromNameOrHexOrId(e.colorId)
        } catch (ignore: NullPointerException) {
            this.color = EventColor.NONE
        }

        if (e.recurrence != null && e.recurrence.isNotEmpty()) {
            this.recur = true
            this.recurrence = Recurrence.fromRRule(e.recurrence[0])
        }

        if (e.summary != null) this.summary = e.summary
        if (e.description != null) this.description = e.description
        if (e.location != null) this.location = e.location

        if (e.start.date == null)
            this.startDateTime = e.start

        if (e.end.date == null)
            this.endDateTime = e.end

        if (e.start.timeZone != null) this.timezone = e.start.timeZone
    }

    companion object {
        @JvmStatic
        fun copy(guildId: Snowflake, e: Event, calData: CalendarData): Mono<PreEvent> {
            return CalendarWrapper.getCalendar(calData)
                    .map { ZoneId.of(it.timeZone) }
                    .map { tz ->
                        val event = PreEvent(guildId, e, calData)
                        event.timezone = tz.id

                        if (e.start.date != null) {
                            event.startDateTime = EventDateTime()
                            event.startDateTime!!.dateTime = TimeUtils.doTimeShiftBullshit(e.start.date, tz)
                        }
                        if (e.end.date != null) {
                            event.endDateTime = EventDateTime()
                            event.endDateTime!!.dateTime = TimeUtils.doTimeShiftBullshit(e.end.date, tz)
                        }

                        return@map event
                    }
                    .flatMap { event ->
                        DatabaseManager.getEventData(guildId, event.eventId)
                                .switchIfEmpty(Mono.just(EventData(guildId, event.eventId)))
                                .doOnNext {
                                    event.eventData = it
                                }
                                .thenReturn(event)
                    }
        }
    }

    //Functions
    fun hasRequiredValues(): Boolean = this.startDateTime != null && this.endDateTime != null
}
