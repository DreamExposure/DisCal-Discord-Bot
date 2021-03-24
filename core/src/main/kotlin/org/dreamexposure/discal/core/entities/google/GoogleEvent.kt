package org.dreamexposure.discal.core.entities.google

import discord4j.common.util.Snowflake
import org.dreamexposure.discal.core.`object`.announcement.Announcement
import org.dreamexposure.discal.core.`object`.event.Recurrence
import org.dreamexposure.discal.core.`object`.event.RsvpData
import org.dreamexposure.discal.core.entities.Event
import org.dreamexposure.discal.core.enums.event.EventColor
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant

class GoogleEvent(
        override val eventId: String,
        override val guildId: Snowflake
) : Event {

    override var name: String = ""
    override var description: String = ""
    override var location: String = ""

    override var color: EventColor = EventColor.NONE

    override lateinit var start: Instant
    override lateinit var end: Instant

    override var recur: Boolean = false
    override var recurrence: Recurrence = Recurrence()

    override var image: String = ""

    override fun getTimezone(): Mono<String> {
        TODO("Not yet implemented")
    }

    override fun getLinkedAnnouncements(): Flux<Announcement> {
        TODO("Not yet implemented")
    }

    override fun getRsvp(): Mono<RsvpData> {
        TODO("Not yet implemented")
    }
}
