package org.dreamexposure.discal.core.entities

import discord4j.common.util.Snowflake
import org.dreamexposure.discal.core.`object`.announcement.Announcement
import org.dreamexposure.discal.core.`object`.event.Recurrence
import org.dreamexposure.discal.core.`object`.event.RsvpData
import org.dreamexposure.discal.core.enums.event.EventColor
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant

interface Event {
    val eventId: String
    val guildId: Snowflake

    //summary is being converted to "name" to make more user-friendly.
    var name: String
    var description: String
    var location: String

    var color: EventColor

    var start: Instant
    var end: Instant

    var recur: Boolean
    var recurrence: Recurrence

    var image: String

    //Reactive
    fun getTimezone(): Mono<String>

    fun getLinkedAnnouncements(): Flux<Announcement>

    fun getRsvp(): Mono<RsvpData>
    //Should I add #save #update #delete here? nah, probably should be on the calendar entity
}
