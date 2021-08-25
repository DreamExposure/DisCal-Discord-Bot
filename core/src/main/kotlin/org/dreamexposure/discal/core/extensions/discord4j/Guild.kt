package org.dreamexposure.discal.core.extensions.discord4j

import discord4j.common.util.Snowflake
import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.entity.Role
import discord4j.rest.entity.RestGuild
import discord4j.rest.http.client.ClientException
import io.netty.handler.codec.http.HttpResponseStatus
import org.dreamexposure.discal.core.`object`.GuildSettings
import org.dreamexposure.discal.core.`object`.announcement.Announcement
import org.dreamexposure.discal.core.database.DatabaseManager
import org.dreamexposure.discal.core.entities.Calendar
import org.dreamexposure.discal.core.entities.spec.create.CreateCalendarSpec
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

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

//Announcements
/**
 * Requests to check if an announcement with the supplied ID exists.
 * If an error occurs, it is emitted through the Mono.
 *
 * @param id The ID of the announcement to check for
 * @return A Mono, where upon successful completion, returns a boolean as to if the announcement exists or not
 */
fun Guild.announcementExists(id: UUID): Mono<Boolean> = getRestGuild().announcementExists(id)

/**
 * Attempts to retrieve an [Announcement] with the supplied [ID][UUID].
 * If an error occurs, it is emitted through the [Mono]
 *
 * @param id The ID of the [Announcement]
 * @return A [Mono] of the [Announcement] with the supplied ID, otherwise [empty][Mono.empty] is returned.
 */
fun Guild.getAnnouncement(id: UUID): Mono<Announcement> = getRestGuild().getAnnouncement(id)

/**
 * Attempts to retrieve all [announcements][Announcement] belonging to this [Guild].
 * If an error occurs, it is emitted through the [Flux]
 *
 * @return A Flux of all [announcements][Announcement] belonging to this [Guild]
 */
fun Guild.getAllAnnouncements(): Flux<Announcement> = getRestGuild().getAllAnnouncements()

/**
 * Attempts to retrieve all [announcements][Announcement] belonging to this [Guild] that are enabled.
 * If an error occurs, it is emitted through the [Flux]
 *
 * @return A [Flux] of all [announcements][Announcement] belonging to this [Guild] that are enabled.
 */
fun Guild.getEnabledAnnouncements(): Flux<Announcement> = getRestGuild().getEnabledAnnouncements()

fun Guild.createAnnouncement(ann: Announcement): Mono<Boolean> = getRestGuild().createAnnouncement(ann)

fun Guild.updateAnnouncement(ann: Announcement): Mono<Boolean> = getRestGuild().updateAnnouncement(ann)

fun Guild.deleteAnnouncement(id: UUID): Mono<Boolean> = getRestGuild().deleteAnnouncement(id)

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

fun Guild.getMemberFromStringId(id: String): Mono<Member> {
    return Mono.just(id)
          .filter(String::isNotEmpty)
          .filter { it.matches(Regex("[0-9]+")) }
          .map(Snowflake::of)
          .flatMap(this::getMemberById)
          .onErrorResume { Mono.empty() }
}

fun Guild.getMembersFromId(ids: List<String>): Flux<Member> {
    return Flux.fromIterable(ids)
          .flatMap(this::getMemberFromStringId)
}

fun Guild.getRestGuild(): RestGuild {
    return client.rest().restGuild(data)
}
