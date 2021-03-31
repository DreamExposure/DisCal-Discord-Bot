package org.dreamexposure.discal.core.entities.response

import org.dreamexposure.discal.core.entities.Event

data class UpdateEventResponse(
        val success: Boolean,
        val old: Event? = null,
        val new: Event? = null,
)
