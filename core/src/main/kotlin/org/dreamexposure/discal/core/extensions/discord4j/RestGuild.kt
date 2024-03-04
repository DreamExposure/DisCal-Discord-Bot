package org.dreamexposure.discal.core.extensions.discord4j

import com.google.api.services.calendar.model.AclRule
import discord4j.core.`object`.entity.Guild
import discord4j.rest.entity.RestGuild
import org.dreamexposure.discal.core.cache.DiscalCache
import org.dreamexposure.discal.core.database.DatabaseManager
import org.dreamexposure.discal.core.entities.Calendar
import org.dreamexposure.discal.core.entities.google.GoogleCalendar
import org.dreamexposure.discal.core.entities.spec.create.CreateCalendarSpec
import org.dreamexposure.discal.core.enums.calendar.CalendarHost
import org.dreamexposure.discal.core.`object`.GuildSettings
import org.dreamexposure.discal.core.`object`.calendar.CalendarData
import org.dreamexposure.discal.core.wrapper.google.AclRuleWrapper
import org.dreamexposure.discal.core.wrapper.google.CalendarWrapper
import org.dreamexposure.discal.core.wrapper.google.GoogleAuthWrapper
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration
import java.util.*
import com.google.api.services.calendar.model.Calendar as GoogleCalendarModel

//Settings
fun RestGuild.getSettings(): Mono<GuildSettings> = DatabaseManager.getSettings(this.id)

//Calendars
/**
 * Attempts to request whether this [Guild] has at least one [Calendar].
 * If an error occurs, it is emitted through the [Mono]
 *
 * @return A [Mono] containing whether this [Guild] has a [Calendar].
 */
fun RestGuild.hasCalendar(): Mono<Boolean> {
    return DatabaseManager.getAllCalendars(this.id).map(List<CalendarData>::isNotEmpty)
}

fun RestGuild.canAddCalendar(): Mono<Boolean> {
    //Always check the live database and bypass cache
    return DatabaseManager.getCalendarCount(this.id)
            .flatMap { current ->
                if (current == 0) Mono.just(true)
                else getSettings().map { current < it.maxCalendars }
            }
}

fun RestGuild.determineNextCalendarNumber(): Mono<Int> {
    return DatabaseManager.getAllCalendars(this.id)
            .map(List<CalendarData>::size)
            .map { it + 1 }
}

/**
 * Attempts to retrieve this [Guild]'s main [Calendar] (calendar 1, this guild's first/primary calendar)
 * If an error occurs, it is emitted through the [Mono]
 *
 * @return A [Mono] containing this [Guild]'s main [Calendar], if it does not exist, [empty][Mono.empty] is returned.
 */
fun RestGuild.getMainCalendar(): Mono<Calendar> = this.getCalendar(1)

/**
 * Attempts to retrieve this [Guild]'s [Calendar] with the supplied index.
 * If an error occurs, it is emitted through the [Mono]
 *
 * @param calNumber The number of the [Calendar]. one-indexed
 * @return A [Mono] containing the [Calendar] with the supplied index, if it does not exist, [empty][Mono.empty] is
 * returned.
 */
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
fun RestGuild.getAllCalendars(): Flux<Calendar> {
    //check cache first
    val cals = DiscalCache.getAllCalendars(id)
    if (cals != null) return Flux.fromIterable(cals)

    return DatabaseManager.getAllCalendars(this.id)
            .flatMapMany { Flux.fromIterable(it) }
            .flatMap(Calendar.Companion::from)
            .doOnNext(DiscalCache::putCalendar)
}

/**
 * Attempts to create a [Calendar] with the supplied information on a 3rd party host.
 * If an error occurs, it is emitted through the [Mono].
 *
 * @param spec The instructions for creating the [Calendar]
 * @return A [Mono] containing the newly created [Calendar]
 */
fun RestGuild.createCalendar(spec: CreateCalendarSpec): Mono<Calendar> {
    return Mono.defer {
        when (spec.host) {
            CalendarHost.GOOGLE -> {
                val credId = GoogleAuthWrapper.randomCredentialId()
                val googleCal = GoogleCalendarModel()

                googleCal.summary = spec.name
                spec.description?.let { googleCal.description = it }
                googleCal.timeZone = spec.timezone.id

                //Call google to create it
                CalendarWrapper.createCalendar(googleCal, credId, this.id)
                        .timeout(Duration.ofSeconds(30))
                        .flatMap { confirmed ->
                            val data = CalendarData(
                                    guildId = this.id,
                                    calendarNumber = spec.calNumber,
                                    host = CalendarHost.GOOGLE,
                                    calendarId = confirmed.id,
                                    calendarAddress = confirmed.id,
                                    credentialId = credId
                            )

                            val rule = AclRule()
                                    .setScope(AclRule.Scope().setType("default"))
                                    .setRole("reader")

                            Mono.`when`(
                                    DatabaseManager.updateCalendar(data),
                                    AclRuleWrapper.insertRule(rule, data)
                            ).thenReturn(GoogleCalendar(data, confirmed))
                                    .doOnNext(DiscalCache::putCalendar)
                        }
            }
        }
    }
}
