package org.dreamexposure.discal.core.entities.google

import com.google.api.client.util.DateTime
import com.google.api.services.calendar.model.AclRule
import com.google.api.services.calendar.model.EventDateTime
import org.dreamexposure.discal.core.`object`.calendar.CalendarData
import org.dreamexposure.discal.core.`object`.event.EventData
import org.dreamexposure.discal.core.cache.DiscalCache
import org.dreamexposure.discal.core.crypto.KeyGenerator
import org.dreamexposure.discal.core.database.DatabaseManager
import org.dreamexposure.discal.core.entities.Calendar
import org.dreamexposure.discal.core.entities.Event
import org.dreamexposure.discal.core.entities.response.UpdateCalendarResponse
import org.dreamexposure.discal.core.entities.spec.create.CreateEventSpec
import org.dreamexposure.discal.core.entities.spec.update.UpdateCalendarSpec
import org.dreamexposure.discal.core.enums.event.EventColor
import org.dreamexposure.discal.core.extensions.google.asInstant
import org.dreamexposure.discal.core.wrapper.google.AclRuleWrapper
import org.dreamexposure.discal.core.wrapper.google.CalendarWrapper
import org.dreamexposure.discal.core.wrapper.google.EventWrapper
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import com.google.api.services.calendar.model.Calendar as GoogleCalendarModel
import com.google.api.services.calendar.model.Event as GoogleEventModel

class GoogleCalendar internal constructor(
        override val calendarData: CalendarData,
        private val baseCalendar: GoogleCalendarModel
) : Calendar {

    override val name: String
        get() = baseCalendar.summary.orEmpty()

    override val description: String
        get() = baseCalendar.description.orEmpty()

    override val timezone: ZoneId
        get() = ZoneId.of(baseCalendar.timeZone)

    override val hostLink: String
        get() = "https://calendar.google.com/calendar/embed?src=$calendarId"

    override fun delete(): Mono<Boolean> {
        //Delete self from cache
        DiscalCache.handleCalendarDelete(guildId)

        return CalendarWrapper.deleteCalendar(calendarData)
                .then(DatabaseManager.deleteCalendarAndRelatedData(calendarData))
                .thenReturn(true)
    }

    override fun update(spec: UpdateCalendarSpec): Mono<UpdateCalendarResponse> {
        val content = GoogleCalendarModel()
        content.id = this.calendarId

        spec.name?.let { content.summary = it }
        spec.description?.let { content.description = it }
        spec.timezone?.let { content.timeZone = it.id }

        return CalendarWrapper.patchCalendar(content, this.calendarData)
                .timeout(Duration.ofSeconds(30))
                .flatMap { confirmed ->
                    val rule = AclRule()
                            .setScope(AclRule.Scope().setType("default"))
                            .setRole("reader")

                    val new = GoogleCalendar(this.calendarData, confirmed)
                    //Update cache
                    DiscalCache.putCalendar(new)

                    return@flatMap AclRuleWrapper.insertRule(rule, this.calendarData)
                            .thenReturn(UpdateCalendarResponse(
                                    old = this,
                                    new = new,
                                    success = true)
                            )
                }.defaultIfEmpty(UpdateCalendarResponse(old = this, success = false))
    }

    override fun getEvent(eventId: String): Mono<Event> {
        return GoogleEvent.get(this, eventId)
    }

    override fun getUpcomingEvents(amount: Int): Flux<Event> {
        return EventWrapper.getEvents(calendarData, amount, System.currentTimeMillis())
                .flatMapMany(this::loadEvents)
    }

    override fun getOngoingEvents(): Flux<Event> {
        val start = System.currentTimeMillis() - Duration.ofDays(14).toMillis() // 2 weeks ago
        val end = System.currentTimeMillis() + Duration.ofDays(1).toMillis() // One day from now

        return EventWrapper.getEvents(calendarData, start, end)
                .flatMapMany { Flux.fromIterable(it) }
                .filter { it.start.asInstant(timezone).isBefore(Instant.now()) }
                .filter { it.end.asInstant(timezone).isAfter(Instant.now()) }
                .collectList()
                .flatMapMany(this::loadEvents)
    }

    override fun getEventsInTimeRange(start: Instant, end: Instant): Flux<Event> {
        return EventWrapper.getEvents(calendarData, start.toEpochMilli(), end.toEpochMilli())
                .flatMapMany(this::loadEvents)
    }

    override fun createEvent(spec: CreateEventSpec): Mono<Event> {
        val event = GoogleEventModel()
        event.id = KeyGenerator.generateEventId()
        event.visibility = "public"

        spec.name?.let { event.summary = it }
        spec.description?.let { event.description = it }
        spec.location?.let { event.location = it }


        event.start = EventDateTime()
                .setDateTime(DateTime(spec.start.toEpochMilli()))
                .setTimeZone(this.timezone.id)
        event.end = EventDateTime()
                .setDateTime(DateTime(spec.end.toEpochMilli()))
                .setTimeZone(this.timezone.id)

        if (spec.color != EventColor.NONE)
            event.colorId = spec.color.id.toString()

        if (spec.recur)
            spec.recurrence?.let { event.recurrence = listOf(it.toRRule()) }

        //Okay, all values are set, lets create the event now...
        return EventWrapper.createEvent(this.calendarData, event).flatMap { confirmed ->
            val data = EventData(
                    this.guildId,
                    confirmed.id,
                    calendarNumber,
                    spec.end.toEpochMilli(),
                    spec.image.orEmpty()
            )

            return@flatMap DatabaseManager.updateEventData(data)
                    .thenReturn(GoogleEvent(this, data, confirmed))
        }
    }

    private fun loadEvents(events: List<GoogleEventModel>): Flux<GoogleEvent> {
        return DatabaseManager.getEventsData(guildId, events.map { it.id }).flatMapMany { data ->
            Flux.fromIterable(events).concatMap {
                if (data.containsKey(it.id)) Mono.just(GoogleEvent(this, data[it.id]!!, it))
                else Mono.just(GoogleEvent(this, EventData(guildId, eventId = it.id), it))
            }
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
