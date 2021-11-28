package org.dreamexposure.discal.core.entities.google

import com.google.api.client.util.DateTime
import com.google.api.services.calendar.model.EventDateTime
import org.dreamexposure.discal.core.`object`.event.EventData
import org.dreamexposure.discal.core.`object`.event.Recurrence
import org.dreamexposure.discal.core.database.DatabaseManager
import org.dreamexposure.discal.core.entities.Calendar
import org.dreamexposure.discal.core.entities.Event
import org.dreamexposure.discal.core.entities.response.UpdateEventResponse
import org.dreamexposure.discal.core.entities.spec.update.UpdateEventSpec
import org.dreamexposure.discal.core.enums.event.EventColor
import org.dreamexposure.discal.core.wrapper.google.EventWrapper
import reactor.core.publisher.Mono
import java.time.Instant
import java.time.temporal.ChronoUnit
import com.google.api.services.calendar.model.Event as GoogleEventModel

class GoogleEvent internal constructor(
        override val calendar: Calendar,
        override val eventData: EventData,
        private val baseEvent: GoogleEventModel,
) : Event {
    override val eventId: String
        get() = baseEvent.id

    override val name: String
        get() = baseEvent.summary.orEmpty()

    override val description: String
        get() = baseEvent.description.orEmpty()

    override val location: String
        get() = baseEvent.location.orEmpty()

    override val link: String
        get() = baseEvent.htmlLink.orEmpty()

    override val color: EventColor
        get() {
            return if (baseEvent.colorId != null && baseEvent.colorId.isNotEmpty())
                EventColor.fromNameOrHexOrId(baseEvent.colorId)
            else
                EventColor.NONE
        }

    override val start: Instant
        get() {
            return if (baseEvent.start.dateTime != null) {
                Instant.ofEpochMilli(baseEvent.start.dateTime.value)
            } else {
                Instant.ofEpochMilli(baseEvent.start.date.value)
                        .plus(1, ChronoUnit.DAYS)
                        .atZone(timezone)
                        .truncatedTo(ChronoUnit.DAYS)
                        .toLocalDate()
                        .atStartOfDay()
                        .atZone(timezone)
                        .toInstant()
            }
        }

    override val end: Instant
        get() {
            return if (baseEvent.end.dateTime != null) {
                Instant.ofEpochMilli(baseEvent.end.dateTime.value)
            } else {
                Instant.ofEpochMilli(baseEvent.end.date.value)
                        .plus(1, ChronoUnit.DAYS)
                        .atZone(timezone)
                        .truncatedTo(ChronoUnit.DAYS)
                        .toLocalDate()
                        .atStartOfDay()
                        .atZone(timezone)
                        .toInstant()
            }
        }

    override val recur: Boolean
        get() = baseEvent.recurrence != null && baseEvent.recurrence.isNotEmpty()

    override val recurrence: Recurrence
        get() {
            return if (recur)
                Recurrence.fromRRule(baseEvent.recurrence[0])
            else
                Recurrence()
        }

    override fun update(spec: UpdateEventSpec): Mono<UpdateEventResponse> {
        val event = GoogleEventModel()
        event.id = this.eventId

        spec.name?.let { event.summary = it }
        spec.description?.let { event.description = it }
        spec.location?.let { event.location = it }

        //Always update start/end so that we can safely handle all day events without DateTime by overwriting it
        if (spec.start != null) {
            event.start = EventDateTime()
                    .setDateTime(DateTime(spec.start.toEpochMilli()))
                    .setTimeZone(this.timezone.id)
        } else {
            event.start = EventDateTime()
                    .setDateTime(DateTime(this.start.toEpochMilli()))
                    .setTimeZone(this.timezone.id)
        }
        if (spec.end != null) {
            event.end = EventDateTime()
                    .setDateTime(DateTime(spec.end.toEpochMilli()))
                    .setTimeZone(this.timezone.id)
        } else {
            event.end = EventDateTime()
                    .setDateTime(DateTime(this.end.toEpochMilli()))
                    .setTimeZone(this.timezone.id)
        }

        spec.color?.let {
            if (it == EventColor.NONE)
                event.colorId = null
            else
                event.colorId = it.id.toString()
        }

        //Special recurrence handling
        if (spec.recur != null) {
            if (spec.recur) {
                //event now recurs, add the RRUle.
                spec.recurrence?.let { event.recurrence = listOf(it.toRRule()) }
            }
        } else {
            //Recur equals null, so it's not changing whether its recurring, so handle if RRule changes only
            spec.recurrence?.let { event.recurrence = listOf(it.toRRule()) }
        }

        //Okay, all values are set, lets patch this event now...
        return EventWrapper.patchEvent(this.calendar.calendarData, event).flatMap { confirmed ->
            val data = EventData(
                    this.guildId,
                    confirmed.id,
                    calendar.calendarNumber,
                    confirmed.end.dateTime.value,
                    spec.image ?: this.image
            )

            return@flatMap DatabaseManager.updateEventData(data)
                    .thenReturn(UpdateEventResponse(true, old = this, GoogleEvent(this.calendar, data, confirmed)))
        }.defaultIfEmpty(UpdateEventResponse(false, old = this))
    }

    override fun delete(): Mono<Boolean> {
        return EventWrapper.deleteEvent(calendar.calendarData, eventId)
                .flatMap { success ->
                    if (success) {
                        Mono.`when`(
                                DatabaseManager.deleteAnnouncementsForEvent(guildId, eventId),
                                DatabaseManager.deleteEventData(eventId),
                        ).thenReturn(true)
                    } else {
                        Mono.just(false)
                    }
                }.defaultIfEmpty(false)
    }

    internal companion object {
        /**
         * Requests to retrieve the event with the provided ID from the provided [Calendar].
         * If an error occurs, it is emitted through the [Mono]
         *
         * @param calendar The [Calendar] this event exists on.
         * @param id The ID of the event
         * @return A [Mono] containing the event, if it does not exist, the mono is empty.
         */
        fun get(calendar: Calendar, id: String): Mono<Event> {
            return EventWrapper.getEvent(calendar.calendarData, id)
                    .flatMap { event ->
                        DatabaseManager.getEventData(calendar.guildId, id)
                                .map {
                                    GoogleEvent(calendar, it, event)
                                }
                    }
        }
    }
}
