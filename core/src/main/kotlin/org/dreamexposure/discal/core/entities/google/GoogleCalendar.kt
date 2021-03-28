package org.dreamexposure.discal.core.entities.google

import org.dreamexposure.discal.core.`object`.calendar.CalendarData
import org.dreamexposure.discal.core.database.DatabaseManager
import org.dreamexposure.discal.core.entities.Calendar
import org.dreamexposure.discal.core.entities.Event
import org.dreamexposure.discal.core.entities.response.UpdateCalendarResponse
import org.dreamexposure.discal.core.entities.spec.update.UpdateCalendarSpec
import org.dreamexposure.discal.core.utils.GlobalConst
import org.dreamexposure.discal.core.utils.TimeUtils
import org.dreamexposure.discal.core.wrapper.google.CalendarWrapper
import org.dreamexposure.discal.core.wrapper.google.EventWrapper
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import java.time.ZoneId

class GoogleCalendar internal constructor(
        override val calendarData: CalendarData,
        private val baseCalendar: com.google.api.services.calendar.model.Calendar
) : Calendar {

    override val name: String
        get() = baseCalendar.summary

    override val description: String
        get() = baseCalendar.description

    override val timezone: ZoneId
        get() = ZoneId.of(baseCalendar.timeZone)

    override fun delete(): Mono<Boolean> {
        return CalendarWrapper.deleteCalendar(calendarData).then(
                Mono.`when`(
                        DatabaseManager.deleteCalendar(calendarData),
                        DatabaseManager.deleteAllEventData(guildId),
                        DatabaseManager.deleteAllRSVPData(guildId),
                        DatabaseManager.deleteAllAnnouncementData(guildId)
                )).thenReturn(true)
                .defaultIfEmpty(false)
    }

    override fun update(spec: UpdateCalendarSpec): Mono<UpdateCalendarResponse> {
        TODO("Not yet implemented")
    }

    override fun getEvent(eventId: String): Mono<Event> {
        return GoogleEvent.get(this, eventId)
    }

    override fun getOngoingEvents(): Flux<Event> {
        val start = System.currentTimeMillis() - (GlobalConst.oneDayMs * 14) // 2 weeks ago
        val end = System.currentTimeMillis() + GlobalConst.oneDayMs // One day from now

        return EventWrapper.getEvents(calendarData, start, end)
                .flatMapMany { Flux.fromIterable(it) }
                .filter { TimeUtils.convertToInstant(it.start, timezone).toEpochMilli() < System.currentTimeMillis() }
                .filter { TimeUtils.convertToInstant(it.end, timezone).toEpochMilli() > System.currentTimeMillis() }
                .flatMap { event ->
                    DatabaseManager.getEventData(guildId, event.id)
                            .map { GoogleEvent(this, it, event) }
                }
    }

    override fun getEventsInTimeRange(start: Instant, end: Instant): Flux<Event> {
        return EventWrapper.getEvents(calendarData, start.toEpochMilli(), end.toEpochMilli())
                .flatMapMany { Flux.fromIterable(it) }
                .flatMap { event ->
                    DatabaseManager.getEventData(guildId, event.id)
                            .map { GoogleEvent(this, it, event) }
                }
    }


    internal companion object {
        /**
         * Requests to retrieve the [Calendar] from the provided [CalendarData]
         * If an error occurs, it is emitted through the [Mono]
         *
         * @param calData The data object for the Calendar to be built with
         * @return A [Mono] containing the [Calendar], if it does not exist, [empty][Mono.empty] is returned.
         */
        fun get(calData: CalendarData): Mono<Calendar> {
            return CalendarWrapper.getCalendar(calData)
                    .map { GoogleCalendar(calData, it) }
        }
    }
}
