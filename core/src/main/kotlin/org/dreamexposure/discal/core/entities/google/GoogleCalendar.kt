package org.dreamexposure.discal.core.entities.google

import com.google.api.client.util.DateTime
import com.google.api.services.calendar.model.AclRule
import com.google.api.services.calendar.model.EventDateTime
import org.dreamexposure.discal.core.`object`.calendar.CalendarData
import org.dreamexposure.discal.core.`object`.event.EventData
import org.dreamexposure.discal.core.crypto.KeyGenerator
import org.dreamexposure.discal.core.database.DatabaseManager
import org.dreamexposure.discal.core.entities.Calendar
import org.dreamexposure.discal.core.entities.Event
import org.dreamexposure.discal.core.entities.response.UpdateCalendarResponse
import org.dreamexposure.discal.core.entities.spec.create.CreateEventSpec
import org.dreamexposure.discal.core.entities.spec.update.UpdateCalendarSpec
import org.dreamexposure.discal.core.enums.event.EventColor
import org.dreamexposure.discal.core.utils.GlobalConst
import org.dreamexposure.discal.core.utils.TimeUtils
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
        val content = GoogleCalendarModel()

        spec.name?.let { content.summary = it }
        spec.description?.let { content.description = it }
        spec.timezone?.let { content.timeZone = it }

        return CalendarWrapper.patchCalendar(content, this.calendarData)
                .timeout(Duration.ofSeconds(30))
                .flatMap { confirmed ->
                    val rule = AclRule()
                            .setScope(AclRule.Scope().setType("default"))
                            .setRole("reader")

                    return@flatMap AclRuleWrapper.insertRule(rule, this.calendarData)
                            .thenReturn(UpdateCalendarResponse(
                                    old = this,
                                    new = GoogleCalendar(this.calendarData, confirmed),
                                    success = true)
                            )
                }.defaultIfEmpty(UpdateCalendarResponse(old = this, success = false))
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
                            .map {
                                GoogleEvent(this, it, event)
                            }
                }
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
                    spec.end.toEpochMilli(),
                    spec.image ?: ""
            )

            return@flatMap DatabaseManager.updateEventData(data)
                    .thenReturn(GoogleEvent(this, data, confirmed))
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
