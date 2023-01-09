package org.dreamexposure.discal.core.entities

import discord4j.common.util.Snowflake
import discord4j.core.`object`.entity.Guild
import org.dreamexposure.discal.core.config.Config
import org.dreamexposure.discal.core.entities.google.GoogleCalendar
import org.dreamexposure.discal.core.entities.response.UpdateCalendarResponse
import org.dreamexposure.discal.core.entities.spec.create.CreateEventSpec
import org.dreamexposure.discal.core.entities.spec.update.UpdateCalendarSpec
import org.dreamexposure.discal.core.enums.calendar.CalendarHost
import org.dreamexposure.discal.core.`object`.calendar.CalendarData
import org.dreamexposure.discal.core.`object`.web.WebCalendar
import org.json.JSONObject
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

interface Calendar {
    /**
     * The ID of the [Guild] this calendar belongs to.
     */
    val guildId: Snowflake
        get() = calendarData.guildId

    /**
     * The base object in which most of the calendar data comes from
     */
    val calendarData: CalendarData

    /**
     * The calendar's ID, usually but not always the same as the calendar's [address][calendarAddress].
     * Use this for unique identification of the calendar
     */
    val calendarId: String
        get() = calendarData.calendarId

    /**
     * The calendar's address, usually but not always the same as the calendar's [ID][calendarId].
     * This property may not be unique, use [calendarId] instead
     */
    val calendarAddress: String
        get() = calendarData.calendarAddress

    /**
     * The relative number of the calendar in order it was created in for the [Guild].
     * Calendar number `1` is the `main` calendar for the [Guild], used as the default.
     */
    val calendarNumber: Int
        get() = calendarData.calendarNumber

    /**
     * Whether the calendar is "external" meaning it is owned by a user account.
     * This does not indicate the service used to host the calendar, but whether it is owned by DisCal, or a user.
     */
    val external: Boolean
        get() = calendarData.external

    /**
     * The name of the calendar. Renamed from "summary" to be more user-friendly and clear.
     */
    val name: String

    /**
     * A longer form description of the calendar.
     * If this is not present, an empty string is returned
     */
    val description: String

    /**
     * The timezone the calendar uses. Normally in its longer name, such as `America/New_York`
     */
    val timezone: ZoneId

    /**
     * The timezone's name, derived from the ZoneId timezone
     */
    val zoneName: String
        get() = timezone.id

    /**
     * A link to view the calendar on the official discal website
     */
    val link: String
        get() = "${Config.URL_BASE.getString()}/embed/${guildId.asString()}/calendar/$calendarNumber"

    /**
     * A link to view the calendar on the host's website (e.g. google.com)
     */
    val hostLink: String

    //Reactive - Self
    /**
     * Attempts to delete the calendar and returns the result.
     * If an error occurs, it is emitted through the [Mono].
     *
     * @return A [Mono] boolean telling whether the deletion was successful
     */
    fun delete(): Mono<Boolean>

    /**
     * Attempts to update the calendar with the provided details and return the result.
     * If an error occurs, it is emitted through the [Mono].
     *
     * @param spec The details to update the calendar with
     * @return A [Mono] whereupon successful completion, returns an [UpdateCalendarResponse] with the new calendar
     */
    fun update(spec: UpdateCalendarSpec): Mono<UpdateCalendarResponse>

    //Reactive - Events
    /**
     * Requests to retrieve the [Event] with the ID.
     * If an error occurs, it is emitted through the [Mono]
     *
     * @return A [Mono] of the [Event], or [Empty][Mono.empty] if not found
     */
    fun getEvent(eventId: String): Mono<Event>

    /**
     * Requests to retrieve all upcoming [events][Event]
     * If an error occurs, it is emitted through the [Flux]
     *
     * @param amount The upper limit of how many events to retrieve
     * @return A [Flux] of [events][Event] that are upcoming
     */
    fun getUpcomingEvents(amount: Int): Flux<Event>

    /**
     * Requests to retrieve all ongoing [events][Event] (starting no more than 2 weeks ago).
     * If an error occurs, it is emitted through the [Flux]
     *
     * @return A [Flux] of [events][Event] that are currently ongoing
     */
    fun getOngoingEvents(): Flux<Event>

    /**
     * Requests to retrieve all [events][Event] within the supplied time span (inclusive).
     * If an error occurs, it is emitted through the [Flux]
     *
     * @return A [Flux] of [events][Event] that are happening within the supplied time range
     */
    fun getEventsInTimeRange(start: Instant, end: Instant): Flux<Event>

    /**
     * Requests to retrieve all [events][Event] occurring withing the next 24-hour period from the supplied [Instant]
     * (inclusive).
     * If an error occurs, it is emitted through the [Flux]
     *
     * @return A [Flux] of [events][Event] that are happening within the next 24-hour period from the start.
     */
    fun getEventsInNext24HourPeriod(start: Instant): Flux<Event> =
          getEventsInTimeRange(start, start.plus(1, ChronoUnit.DAYS))

    /**
     * Requests to retrieve all [events][Event] within the month starting at the supplied [Instant].
     * If an error occurs, it is emitted through the [Flux]
     *
     * @return A [Flux] of [events][Event] that are happening in the supplied 1-month period.
     */
    fun getEventsInMonth(start: Instant, daysInMonth: Int): Flux<Event> =
          getEventsInTimeRange(start, start.plus(daysInMonth.toLong(), ChronoUnit.DAYS))

    fun getEventsInNextNDays(days: Int): Flux<Event> =
        getEventsInTimeRange(Instant.now(), Instant.now().plus(days.toLong(), ChronoUnit.DAYS))



    /**
     * Requests to create an event with the supplied information.
     * If an error occurs, it is emitted through the [Mono]
     *
     * @param spec The information to input into the new [Event]
     * @return A [Mono] containing the newly created [Event]
     */
    fun createEvent(spec: CreateEventSpec): Mono<Event>

    //Convenience

    /**
     * Converts this entity into a [WebCalendar] object.
     *
     * @return A [WebCalendar] containing the information from this entity
     */
    fun toWebCalendar(): WebCalendar {
        return WebCalendar(
              this.calendarId,
              this.calendarAddress,
              this.calendarNumber,
              this.calendarData.host,
              this.link,
              this.hostLink,
              this.name,
              this.description,
              this.timezone.id.replace("/", "___"),
              this.external
        )
    }

    fun toJson(): JSONObject {
        return JSONObject()
              .put("guild_id", guildId.asString())
              .put("calendar_id", calendarId)
              .put("calendar_address", calendarAddress)
              .put("calendar_number", calendarNumber)
              .put("host", calendarData.host.name)
              .put("host_link", hostLink)
              .put("external", external)
              .put("name", name)
              .put("description", description)
              .put("timezone", timezone)
              .put("link", link)
    }

    companion object {
        /**
         * Requests to retrieve the [Calendar] from the provided [CalendarData]
         * If an error occurs, it is emitted through the [Mono]
         *
         * @param data The data object for the Calendar to be built with
         * @return A [Mono] containing the [Calendar], if it does not exist, [empty][Mono.empty] is returned.
         */
        fun from(data: CalendarData): Mono<Calendar> {
            when (data.host) {
                CalendarHost.GOOGLE -> {
                    return GoogleCalendar.get(data)
                }
            }
        }
    }
}
