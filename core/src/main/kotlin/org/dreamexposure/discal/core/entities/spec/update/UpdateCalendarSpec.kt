package org.dreamexposure.discal.core.entities.spec.update

import java.time.ZoneId

data class UpdateCalendarSpec(
        val name: String? = null,

        val description: String? = null,

        val timezone: ZoneId? = null,
)
