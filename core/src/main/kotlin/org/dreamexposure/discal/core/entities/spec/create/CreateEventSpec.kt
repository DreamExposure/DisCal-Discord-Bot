package org.dreamexposure.discal.core.entities.spec.create

import org.dreamexposure.discal.core.`object`.event.Recurrence
import org.dreamexposure.discal.core.enums.event.EventColor
import java.time.Instant

data class CreateEventSpec(
        val name: String? = null,

        val description: String? = null,

        val start: Instant,

        val end: Instant,

        val color: EventColor = EventColor.NONE,

        val location: String? = null,

        val image: String? = null,

        val recur: Boolean = false,

        val recurrence: Recurrence? = null,
)
