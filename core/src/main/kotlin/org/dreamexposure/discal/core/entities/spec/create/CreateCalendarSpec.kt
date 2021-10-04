package org.dreamexposure.discal.core.entities.spec.create

import org.dreamexposure.discal.core.enums.calendar.CalendarHost
import java.time.ZoneId

data class CreateCalendarSpec(
        val host: CalendarHost,

        val calNumber: Int,

        val name: String,

        val description: String? = null,

        val timezone: ZoneId,
)
