package org.dreamexposure.discal.core.entities

import discord4j.common.util.Snowflake
import discord4j.core.`object`.entity.Guild
import org.dreamexposure.discal.core.`object`.BotSettings
import org.dreamexposure.discal.core.`object`.announcement.Announcement
import org.dreamexposure.discal.core.`object`.calendar.CalendarData
import org.dreamexposure.discal.core.database.DatabaseManager
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.*

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
     * Whether or not the calendar is "external" meaning it is owned by a user account.
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
     * Gets the link to the calendar on the official bot website.
     *
     * @return A link to view the calendar on the official bot website.
     */
    fun getLink(): String {
        return if (BotSettings.PROFILE.get().equals("TEST", true))
            "https://dev.discalbot.com/embed/${guildId.asString()}/calendar/$calendarNumber"
        else
            "https://dev.discalbot.com/embed/${guildId.asString()}/calendar/$calendarNumber"
    }

    //Reactive - Self
    /**
     * Attempts to delete the calendar and returns the result.
     * If an error occurs, it is emitted through the [Mono].
     *
     * @return A [Mono] boolean telling whether or not the deletion was successful
     */
    fun delete(): Mono<Boolean>

    //TODO: Add update once I figure out specs

    //Reactive - Events
    /**
     * Requests to retrieve the [Event] with the ID.
     * If an error occurs, it is emitted through the [Mono]
     *
     * @return A [Mono] of the [Event], or [Empty][Mono.empty] if not found
     */
    fun getEvent(eventId: String): Mono<Event>

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
     * Requests to retrieve all [events][Event] occurring withing the next 24 hour period from the supplied [Instant]
     * (inclusive).
     * If an error occurs, it is emitted through the [Flux]
     *
     * @return A [Flux] of [events][Event] that are happening within the next 24 hour period from the start.
     */
    fun getEventsInNext24HourPeriod(start: Instant): Flux<Event> =
            getEventsInTimeRange(start, start.plus(1, ChronoUnit.DAYS))

    /**
     * Requests to retrieve all [events][Event] within the month starting at the supplied [Instant].
     * If an error occurs, it is emitted through the [Flux]
     *
     * @return A [Flux] of [events][Event] that are happening in the supplied 1 month period.
     */
    fun getEventsInMonth(start: Instant, daysInMonth: Int): Flux<Event> =
            getEventsInTimeRange(start, start.plus(daysInMonth.toLong(), ChronoUnit.DAYS))

    //TODO: Create/update/delete event

    // Reactive - Announcements
    /**
     * Requests to retrieve the [Announcement] with the supplied [ID][UUID].
     * If an error occurs, it is emitted through the [Mono]
     *
     * @return A [Mono] containing the [Announcement] with the supplied [ID][UUID], [empty][Mono.empty] if it does not
     * exist.
     */
    fun getAnnouncement(id: UUID): Mono<Announcement> = DatabaseManager.getAnnouncement(id, guildId)

    /**
     * Requests to retrieve all [announcements][Announcement] associated with the owning [Guild].
     * If an error occurs, it is emitted through the [Flux]
     *
     * @return A [Flux] of all [announcements][Announcement] associated with the owning [Guild].
     */
    fun getAllAnnouncements(): Flux<Announcement> =
            DatabaseManager.getAnnouncements(guildId).flatMapMany { Flux.fromIterable(it) }

    /**
     * Requests to retrieve all [announcements][Announcement] associated with the owning [Guild] that are enabled.
     * If an error occurs, it is emitted through the [Flux]
     *
     * @return A [Flux] of all [announcements][Announcement] associated with the owning [Guild] that are enabled.
     */
    fun getEnabledAnnouncements(): Flux<Announcement> =
            DatabaseManager.getEnabledAnnouncements(guildId).flatMapMany { Flux.fromIterable(it) }

    //TODO: Create/update/delete announcements
}
