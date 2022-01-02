package org.dreamexposure.discal.core.`object`.event

import discord4j.common.util.Snowflake
import discord4j.core.`object`.entity.Guild
import org.dreamexposure.discal.core.`object`.GuildSettings
import org.dreamexposure.discal.core.`object`.Pre
import org.dreamexposure.discal.core.entities.Calendar
import org.dreamexposure.discal.core.entities.Event
import org.dreamexposure.discal.core.entities.spec.create.CreateEventSpec
import org.dreamexposure.discal.core.entities.spec.update.UpdateEventSpec
import org.dreamexposure.discal.core.enums.event.EventColor
import org.dreamexposure.discal.core.extensions.discord4j.getCalendar
import org.dreamexposure.discal.core.utils.getEmbedMessage
import reactor.core.publisher.Mono
import java.time.Duration
import java.time.Instant
import java.time.ZoneId

@Suppress("DataClassPrivateConstructor")
data class PreEvent private constructor(
        override val guildId: Snowflake,
        val eventId: String? = null,
        val calNumber: Int,
        val timezone: ZoneId,
        override val editing: Boolean = false,
): Pre(guildId, editing) {
    var name: String? = null

    var description: String? = null

    var start: Instant? = null
    var end: Instant? = null

    var color: EventColor = EventColor.NONE

    var location: String? = null

    var image: String? = null

    var recurrence: Recurrence? = null

    var event: Event? = null


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

    override fun generateWarnings(settings: GuildSettings): List<String> {
        val warnings = mutableListOf<String>()

        if (name.isNullOrBlank()) {
            warnings.add(getEmbedMessage("event", "warning.wizard.noName", settings))
        }

        // Checking end time is not needed
        if (start != null && start!!.isBefore(Instant.now())) {
            warnings.add(getEmbedMessage("event", "warning.wizard.past", settings))
        }

        if (this.start != null && this.end != null) {
            if (Duration.between(start!!, end!!).toDays() > 30) {
                warnings.add(getEmbedMessage("event", "warning.wizard.veryLong", settings))
            }

        }

        return warnings
    }

    companion object {
        fun new(calendar: Calendar): PreEvent {
            return PreEvent(
                    guildId = calendar.guildId,
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
            if (event.recur)
                pre.recurrence = event.recurrence
            pre.event = event

            return pre
        }

        fun copy(guild: Guild, event: Event, targetCalNum: Int): Mono<PreEvent> {
            val calMono: Mono<Calendar> =
                    if (targetCalNum != event.calendar.calendarNumber) {
                        guild.getCalendar(targetCalNum).defaultIfEmpty(event.calendar)
                    } else {
                        Mono.just(event.calendar)
                    }

            return calMono.map { targetCal ->
                val pre = PreEvent(
                        guildId = event.guildId,
                        calNumber = targetCal.calendarNumber,
                        timezone = targetCal.timezone,
                )

                pre.name = event.name
                pre.description = event.description
                pre.start = event.start
                pre.end = event.end
                pre.color = event.color
                pre.location = event.location
                pre.image = event.image
                if (event.recur)
                    pre.recurrence = event.recurrence

                pre
            }
        }
    }
}
