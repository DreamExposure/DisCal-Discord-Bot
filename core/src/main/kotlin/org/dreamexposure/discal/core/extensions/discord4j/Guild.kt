package org.dreamexposure.discal.core.extensions.discord4j

import discord4j.common.util.Snowflake
import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.Role
import discord4j.rest.entity.RestGuild
import discord4j.rest.http.client.ClientException
import io.netty.handler.codec.http.HttpResponseStatus
import org.dreamexposure.discal.core.database.DatabaseManager
import org.dreamexposure.discal.core.entities.Calendar
import org.dreamexposure.discal.core.entities.spec.create.CreateCalendarSpec
import org.dreamexposure.discal.core.`object`.GuildSettings
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

//Settings
fun Guild.getSettings(): Mono<GuildSettings> = getRestGuild().getSettings()

//Calendars
/**
 * Attempts to request whether this [Guild] has at least one [Calendar].
 * If an error occurs, it is emitted through the [Mono]
 *
 * @return A [Mono] containing whether this [Guild] has a [Calendar].
 */
fun Guild.hasCalendar(): Mono<Boolean> = getRestGuild().hasCalendar()

fun Guild.canAddCalendar(): Mono<Boolean> = getRestGuild().canAddCalendar()

fun Guild.determineNextCalendarNumber(): Mono<Int> = getRestGuild().determineNextCalendarNumber()

/**
 * Attempts to retrieve this [Guild]'s main [Calendar] (calendar 1, this guild's first/primary calendar)
 * If an error occurs, it is emitted through the [Mono]
 *
 * @return A [Mono] containing this [Guild]'s main [Calendar], if it does not exist, [empty][Mono.empty] is returned.
 */
fun Guild.getMainCalendar(): Mono<Calendar> = getRestGuild().getMainCalendar()

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
 * Attempts to retrieve all [calendars][Calendar] belonging to this [Guild].
 * If an error occurs, it is emitted through the [Flux]
 *
 * @return A [Flux] containing all the [calendars][Calendar] belonging to this [Guild].
 */
fun Guild.getAllCalendars(): Flux<Calendar> = getRestGuild().getAllCalendars()

/**
 * Attempts to create a [Calendar] with the supplied information on a 3rd party host.
 * If an error occurs, it is emitted through the [Mono].
 *
 * @param spec The instructions for creating the [Calendar]
 * @return A [Mono] containing the newly created [Calendar]
 */
fun Guild.createCalendar(spec: CreateCalendarSpec): Mono<Calendar> = getRestGuild().createCalendar(spec)

fun Guild.getControlRole(): Mono<Role> {
    return getSettings().flatMap { settings ->
        if (settings.controlRole.equals("everyone", true))
            return@flatMap this.everyoneRole
        else {
            return@flatMap getRoleById(Snowflake.of(settings.controlRole))
                .onErrorResume(ClientException::class.java) {
                    //If control role is deleted/not found, we reset it to everyone
                    if (it.status == HttpResponseStatus.NOT_FOUND) {
                        settings.controlRole = "everyone"
                        DatabaseManager.updateSettings(settings).then(everyoneRole)
                    } else
                        everyoneRole
                }
        }
    }
}

fun Guild.getRestGuild(): RestGuild {
    return client.rest().restGuild(data)
}
