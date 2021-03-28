package org.dreamexposure.discal.core.entities.spec.create

import org.dreamexposure.discal.core.enums.calendar.CalendarHost

data class CreateCalendarSpec(
        val host: CalendarHost,

        val name: String,

        val description: String? = null,

        val timezone: String,
)
