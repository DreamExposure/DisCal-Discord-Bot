package org.dreamexposure.discal.core.`object`.event

import discord4j.common.util.Snowflake
import discord4j.core.`object`.entity.Guild
import org.dreamexposure.discal.core.entities.Calendar
import org.dreamexposure.discal.core.entities.Event
import org.dreamexposure.discal.core.entities.spec.create.CreateEventSpec
import org.dreamexposure.discal.core.entities.spec.update.UpdateEventSpec
import org.dreamexposure.discal.core.enums.event.EventColor
import org.dreamexposure.discal.core.extensions.discord4j.getCalendar
import reactor.core.publisher.Mono
import java.time.Instant
import java.time.ZoneId

@Suppress("DataClassPrivateConstructor")
data class PreEvent private constructor(
        val guildId: Snowflake,
        val eventId: String? = null,
        val calNumber: Int,
        val timezone: ZoneId,
        val editing: Boolean = false,
) {
    var name: String? = null

    var description: String? = null

    var start: Instant? = null
    var end: Instant? = null

    var color: EventColor = EventColor.NONE

    var location: String? = null

    var image: String? = null

    var recurrence: Recurrence? = null

    var event: Event? = null

    var lastEdit: Instant = Instant.now()


    fun hasRequiredValues(): Boolean {
        return this.start != null
                && this.end != null
    }

    fun createSpec(): CreateEventSpec {
        return CreateEventSpec(
                name = name,
                description = description,
                start = start!!,
                end = end!!,
                color = color,
                location = location,
                image = image,
                recur = recurrence != null,
                recurrence = recurrence,
        )
    }

    fun updateSpec(): UpdateEventSpec {
        return UpdateEventSpec(
                name = name,
                description = description,
                start = start,
                end = end,
                color = color,
                location = location,
                image = image,
                recur = recurrence != null,
                recurrence = recurrence,
        )
    }

    companion object {
        fun new(guildId: Snowflake, calendar: Calendar): PreEvent {
            return PreEvent(
                    guildId = guildId,
                    calNumber = calendar.calendarNumber,
                    timezone = calendar.timezone,
                    editing = false,
            )
        }

        fun edit(event: Event): PreEvent {
            val pre = PreEvent(
                    guildId = event.guildId,
                    eventId = event.eventId,
                    calNumber = event.calendar.calendarNumber,
                    timezone = event.timezone,
                    editing = true
            )

            pre.name = event.name
            pre.description = event.description
            pre.start = event.start
            pre.end = event.end
            pre.color = event.color
            pre.location = event.location
            pre.image = event.image
            pre.recurrence = event.recurrence
            pre.event = event

            return pre
        }

        fun copy(guild: Guild, event: Event, calNum: Int = event.calendar.calendarNumber): Mono<PreEvent> {
            val calMono: Mono<Calendar> =
                    if (calNum != event.calendar.calendarNumber) {
                        guild.getCalendar(calNum).defaultIfEmpty(event.calendar)
                    } else {
                        Mono.just(event.calendar)
                    }

            return calMono.map { targetCal ->
                val pre = PreEvent(
                        guildId = event.guildId,
                        calNumber = targetCal.calendarNumber,
                        timezone = targetCal.timezone,
                        editing = true
                )

                pre.name = event.name
                pre.description = event.description
                pre.start = event.start
                pre.end = event.end
                pre.color = event.color
                pre.location = event.location
                pre.image = event.image
                pre.recurrence = event.recurrence

                pre
            }
        }
    }
}
