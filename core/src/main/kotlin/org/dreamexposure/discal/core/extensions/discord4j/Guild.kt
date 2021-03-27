package org.dreamexposure.discal.core.extensions.discord4j

import discord4j.core.`object`.entity.Guild
import org.dreamexposure.discal.core.`object`.announcement.Announcement
import org.dreamexposure.discal.core.`object`.calendar.CalendarData
import org.dreamexposure.discal.core.database.DatabaseManager
import org.dreamexposure.discal.core.entities.Calendar
import org.dreamexposure.discal.core.entities.google.GoogleCalendar
import org.dreamexposure.discal.core.enums.calendar.CalendarHost
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

//TODO: Settings and some other objects


//Calendars
/**
 * Attempts to request whether or not this [Guild] has at least one [Calendar].
 * If an error occurs, it is emitted through the [Mono]
 *
 * @return A [Mono] containing whether or not this [Guild] has a [Calendar].
 */
fun Guild.hasCalendar(): Mono<Boolean> {
    return DatabaseManager.getAllCalendars(this.id).map(MutableList<CalendarData>::isNotEmpty)
}

/**
 * Attempts to retrieve this [Guild]'s main [Calendar] (calendar 1, this guild's first/primary calendar)
 * If an error occurs, it is emitted through the [Mono]
 *
 * @return A [Mono] containing this [Guild]'s main [Calendar], if it does not exist, [empty][Mono.empty] is returned.
 */
fun Guild.getMainCalendar(): Mono<Calendar> = this.getCalendar(1)

/**
 * Attempts to retrieve this [Guild]'s [Calendar] with the supplied index.
 * If an error occurs, it is emitted through the [Mono]
 *
 * @param calNumber The number of the [Calendar]. one-indexed
 * @return A [Mono] containing the [Calendar] with the supplied index, if it does not exist, [empty][Mono.empty] is
 * returned.
 */
fun Guild.getCalendar(calNumber: Int): Mono<Calendar> {
    return DatabaseManager.getCalendar(this.id, calNumber).flatMap {
        when (it.host) {
            CalendarHost.GOOGLE -> {
                return@flatMap GoogleCalendar.get(it)
            }
        }
    }
}

/**
 * Attempts to retrieve all [calendars][Calendar] belonging to this [Guild].
 * If an error occurs, it is emitted through the [Flux]
 *
 * @return A [Flux] containing all the [calendars][Calendar] belonging to this [Guild].
 */
fun Guild.getAllCalendars(): Flux<Calendar> {
    return DatabaseManager.getAllCalendars(this.id)
            .flatMapMany { Flux.fromIterable(it) }
            .flatMap {
                when (it.host) {
                    CalendarHost.GOOGLE -> {
                        return@flatMap GoogleCalendar.get(it)
                    }
                }
            }
}

//TODO: Create/update/delete calendars

//Announcements
/**
 * Attempts to retrieve an [Announcement] with the supplied [ID][UUID].
 * If an error occurs, it is emitted through the [Mono]
 *
 * @param id The ID of the [Announcement]
 * @return A [Mono] of the [Announcement] with the supplied ID, otherwise [empty][Mono.empty] is returned.
 */
fun Guild.getAnnouncement(id: UUID): Mono<Announcement> = DatabaseManager.getAnnouncement(id, this.id)

/**
 * Attempts to retrieve all [announcements][Announcement] belonging to this [Guild].
 * If an error occurs, it is emitted through the [Flux]
 *
 * @return A Flux of all [announcements][Announcement] belonging to this [Guild]
 */
fun Guild.getAllAnnouncements(): Flux<Announcement> {
    return DatabaseManager.getAnnouncements(this.id)
            .flatMapMany { Flux.fromIterable(it) }
}

/**
 * Attempts to retrieve all [announcements][Announcement] belonging to this [Guild] that are enabled.
 * If an error occurs, it is emitted through the [Flux]
 *
 * @return A [Flux] of all [announcements][Announcement] belonging to this [Guild] that are enabled.
 */
fun Guild.getEnabledAnnouncements(): Flux<Announcement> {
    return DatabaseManager.getEnabledAnnouncements(this.id)
            .flatMapMany { Flux.fromIterable(it) }
}

//TODO: create/update/delete announcements
