package org.dreamexposure.discal.core.entities.google

import org.dreamexposure.discal.core.`object`.announcement.Announcement
import org.dreamexposure.discal.core.`object`.event.EventData
import org.dreamexposure.discal.core.`object`.event.Recurrence
import org.dreamexposure.discal.core.database.DatabaseManager
import org.dreamexposure.discal.core.entities.Calendar
import org.dreamexposure.discal.core.entities.Event
import org.dreamexposure.discal.core.enums.event.EventColor
import org.dreamexposure.discal.core.wrapper.google.EventWrapper
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import java.time.temporal.ChronoUnit

class GoogleEvent internal constructor(
        override val calendar: Calendar,
        override val eventData: EventData,
        private val baseEvent: com.google.api.services.calendar.model.Event,
) : Event {
    override val eventId: String
        get() = baseEvent.id

    override val name: String
        get() = baseEvent.summary

    override val description: String
        get() = baseEvent.description

    override val location: String
        get() = baseEvent.location

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

    override fun getLinkedAnnouncements(): Flux<Announcement> {
        TODO("Not yet implemented")
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
