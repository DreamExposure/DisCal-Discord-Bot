package org.dreamexposure.discal.core.entities.spec.update

import org.dreamexposure.discal.core.`object`.event.Recurrence
import org.dreamexposure.discal.core.enums.event.EventColor
import java.time.Instant

data class UpdateEventSpec(
        val name: String? = null,

        val description: String? = null,

        val start: Instant? = null,

        val end: Instant? = null,

        val color: EventColor? = null,

        val location: String? = null,

        val image: String? = null,

        val recur: Boolean? = null,

        val recurrence: Recurrence? = null
)
