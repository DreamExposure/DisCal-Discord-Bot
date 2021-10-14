package org.dreamexposure.discal.core.`object`.event

import discord4j.common.util.Snowflake
import org.dreamexposure.discal.core.entities.Event
import org.dreamexposure.discal.core.entities.spec.create.CreateEventSpec
import org.dreamexposure.discal.core.entities.spec.update.UpdateEventSpec
import org.dreamexposure.discal.core.enums.event.EventColor
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
        //TODO: new function

        //TODO: edit function
    }
}
