package org.dreamexposure.discal.core.extensions.discord4j

import discord4j.core.`object`.entity.Guild
import discord4j.rest.entity.RestGuild
import org.dreamexposure.discal.core.cache.DiscalCache
import org.dreamexposure.discal.core.database.DatabaseManager
import org.dreamexposure.discal.core.entities.Calendar
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

//Calendars

/**
 * Attempts to retrieve this [Guild]'s main [Calendar] (calendar 1, this guild's first/primary calendar)
 * If an error occurs, it is emitted through the [Mono]
 *
 * @return A [Mono] containing this [Guild]'s main [Calendar], if it does not exist, [empty][Mono.empty] is returned.
 */
@Deprecated("Prefer to use new CalendarService")
fun RestGuild.getMainCalendar(): Mono<Calendar> = this.getCalendar(1)

/**
 * Attempts to retrieve this [Guild]'s [Calendar] with the supplied index.
 * If an error occurs, it is emitted through the [Mono]
 *
 * @param calNumber The number of the [Calendar]. one-indexed
 * @return A [Mono] containing the [Calendar] with the supplied index, if it does not exist, [empty][Mono.empty] is
 * returned.
 */
@Deprecated("Prefer to use new CalendarService")
fun RestGuild.getCalendar(calNumber: Int): Mono<Calendar> {
    //Check cache first
    val cal = DiscalCache.getCalendar(id, calNumber)
    if (cal != null) return Mono.just(cal)

    return DatabaseManager.getCalendar(this.id, calNumber)
            .flatMap(Calendar.Companion::from)
            .doOnNext(DiscalCache::putCalendar)
}

/**
 * Attempts to retrieve all [calendars][Calendar] belonging to this [Guild].
 * If an error occurs, it is emitted through the [Flux]
 *
 * @return A [Flux] containing all the [calendars][Calendar] belonging to this [Guild].
 */
@Deprecated("Prefer to use new CalendarService")
fun RestGuild.getAllCalendars(): Flux<Calendar> {
    //check cache first
    val cals = DiscalCache.getAllCalendars(id)
    if (cals != null) return Flux.fromIterable(cals)

    return DatabaseManager.getAllCalendars(this.id)
            .flatMapMany { Flux.fromIterable(it) }
            .flatMap(Calendar.Companion::from)
            .doOnNext(DiscalCache::putCalendar)
}
