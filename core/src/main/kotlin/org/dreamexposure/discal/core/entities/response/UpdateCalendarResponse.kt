package org.dreamexposure.discal.core.entities.response

import org.dreamexposure.discal.core.entities.Calendar

data class UpdateCalendarResponse(
        val success: Boolean,
        val old: Calendar? = null,
        val new: Calendar? = null,
)
