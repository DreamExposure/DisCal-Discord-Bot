package org.dreamexposure.discal.core.extensions.discord4j

import discord4j.core.`object`.entity.Guild
import discord4j.rest.entity.RestGuild
import org.dreamexposure.discal.core.entities.Calendar
import org.dreamexposure.discal.core.entities.spec.create.CreateCalendarSpec
import reactor.core.publisher.Mono

//Calendars


fun Guild.determineNextCalendarNumber(): Mono<Int> = getRestGuild().determineNextCalendarNumber()

/**
 * Attempts to retrieve this [Guild]'s [Calendar] with the supplied index.
 * If an error occurs, it is emitted through the [Mono]
 *
 * @param calNumber The number of the [Calendar]. one-indexed
 * @return A [Mono] containing the [Calendar] with the supplied index, if it does not exist, [empty][Mono.empty] is
 * returned.
 */
fun Guild.getCalendar(calNumber: Int): Mono<Calendar> = getRestGuild().getCalendar(calNumber)

/**
 * Attempts to create a [Calendar] with the supplied information on a 3rd party host.
 * If an error occurs, it is emitted through the [Mono].
 *
 * @param spec The instructions for creating the [Calendar]
 * @return A [Mono] containing the newly created [Calendar]
 */
fun Guild.createCalendar(spec: CreateCalendarSpec): Mono<Calendar> = getRestGuild().createCalendar(spec)

fun Guild.getRestGuild(): RestGuild {
    return client.rest().restGuild(data)
}
