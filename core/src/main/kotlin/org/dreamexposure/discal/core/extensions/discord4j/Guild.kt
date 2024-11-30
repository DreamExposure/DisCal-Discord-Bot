package org.dreamexposure.discal.core.extensions.discord4j

import discord4j.core.`object`.entity.Guild
import discord4j.rest.entity.RestGuild
import org.dreamexposure.discal.core.entities.Calendar
import reactor.core.publisher.Mono

//Calendars

/**
 * Attempts to retrieve this [Guild]'s [Calendar] with the supplied index.
 * If an error occurs, it is emitted through the [Mono]
 *
 * @param calNumber The number of the [Calendar]. one-indexed
 * @return A [Mono] containing the [Calendar] with the supplied index, if it does not exist, [empty][Mono.empty] is
 * returned.
 */
@Deprecated("Prefer to use new CalendarService")
fun Guild.getCalendar(calNumber: Int): Mono<Calendar> = getRestGuild().getCalendar(calNumber)

fun Guild.getRestGuild(): RestGuild {
    return client.rest().restGuild(data)
}
