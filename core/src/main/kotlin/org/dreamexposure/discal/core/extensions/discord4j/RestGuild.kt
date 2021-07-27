package org.dreamexposure.discal.core.extensions.discord4j

import com.google.api.services.calendar.model.AclRule
import discord4j.core.`object`.entity.Guild
import discord4j.rest.entity.RestGuild
import org.dreamexposure.discal.core.`object`.GuildSettings
import org.dreamexposure.discal.core.`object`.announcement.Announcement
import org.dreamexposure.discal.core.`object`.calendar.CalendarData
import org.dreamexposure.discal.core.database.DatabaseManager
import org.dreamexposure.discal.core.entities.Calendar
import org.dreamexposure.discal.core.entities.google.GoogleCalendar
import org.dreamexposure.discal.core.entities.spec.create.CreateCalendarSpec
import org.dreamexposure.discal.core.enums.calendar.CalendarHost
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
 * Attempts to request whether or not this [Guild] has at least one [Calendar].
 * If an error occurs, it is emitted through the [Mono]
 *
 * @return A [Mono] containing whether or not this [Guild] has a [Calendar].
 */
fun RestGuild.hasCalendar(): Mono<Boolean> {
    return DatabaseManager.getAllCalendars(this.id).map(List<CalendarData>::isNotEmpty)
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
fun RestGuild.getAllCalendars(): Flux<Calendar> {
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

/**
 * Attempts to create a [Calendar] with the supplied information on a 3rd party host.
 * If an error occurs, it is emitted through the [Mono].
 *
 * @param spec The instructions for creating the [Calendar]
 * @return A [Mono] containing the newly created [Calendar]
 */
fun RestGuild.createCalendar(spec: CreateCalendarSpec): Mono<Calendar> {
    when (spec.host) {
        CalendarHost.GOOGLE -> {
            return GoogleAuthWrapper.randomCredentialId().flatMap { credId ->
                val googleCal = GoogleCalendarModel()

                googleCal.summary = spec.name
                spec.description?.let { googleCal.description = it }
                googleCal.timeZone = spec.timezone

                //Call google to create it
                CalendarWrapper.createCalendar(googleCal, credId, this.id)
                        .timeout(Duration.ofSeconds(30))
                        .flatMap { confirmed ->
                            val data = CalendarData(
                                    this.id,
                                    spec.calNumber,
                                    CalendarHost.GOOGLE,
                                    confirmed.id,
                                    confirmed.id,
                                    credId
                            )

                            val rule = AclRule()
                                    .setScope(AclRule.Scope().setType("default"))
                                    .setRole("reader")

                            Mono.`when`(
                                    DatabaseManager.updateCalendar(data),
                                    AclRuleWrapper.insertRule(rule, data)
                            ).thenReturn(GoogleCalendar(data, confirmed))
                        }
            }
        }
    }
}

//Announcements
/**
 * Requests to check if an announcement with the supplied ID exists.
 * If an error occurs, it is emitted through the Mono.
 *
 * @param id The ID of the announcement to check for
 * @return A Mono, where upon successful completion, returns a boolean as to if the announcement exists or not
 */
fun RestGuild.announcementExists(id: UUID): Mono<Boolean> = this.getAnnouncement(id).hasElement()

/**
 * Attempts to retrieve an [Announcement] with the supplied [ID][UUID].
 * If an error occurs, it is emitted through the [Mono]
 *
 * @param id The ID of the [Announcement]
 * @return A [Mono] of the [Announcement] with the supplied ID, otherwise [empty][Mono.empty] is returned.
 */
fun RestGuild.getAnnouncement(id: UUID): Mono<Announcement> = DatabaseManager.getAnnouncement(id, this.id)

/**
 * Attempts to retrieve all [announcements][Announcement] belonging to this [Guild].
 * If an error occurs, it is emitted through the [Flux]
 *
 * @return A Flux of all [announcements][Announcement] belonging to this [Guild]
 */
fun RestGuild.getAllAnnouncements(): Flux<Announcement> {
    return DatabaseManager.getAnnouncements(this.id)
            .flatMapMany { Flux.fromIterable(it) }
}

/**
 * Attempts to retrieve all [announcements][Announcement] belonging to this [Guild] that are enabled.
 * If an error occurs, it is emitted through the [Flux]
 *
 * @return A [Flux] of all [announcements][Announcement] belonging to this [Guild] that are enabled.
 */
fun RestGuild.getEnabledAnnouncements(): Flux<Announcement> {
    return DatabaseManager.getEnabledAnnouncements(this.id)
            .flatMapMany { Flux.fromIterable(it) }
}

fun RestGuild.createAnnouncement(ann: Announcement): Mono<Boolean> = DatabaseManager.updateAnnouncement(ann)

fun RestGuild.updateAnnouncement(ann: Announcement): Mono<Boolean> = DatabaseManager.updateAnnouncement(ann)

fun RestGuild.deleteAnnouncement(id: UUID): Mono<Boolean> = DatabaseManager.deleteAnnouncement(id.toString())
