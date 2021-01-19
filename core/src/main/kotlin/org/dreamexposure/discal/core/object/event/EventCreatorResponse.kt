package org.dreamexposure.discal.core.`object`.event

import com.google.api.services.calendar.model.Event
import discord4j.core.`object`.entity.Message

data class EventCreatorResponse(
        val successful: Boolean,
        val event: Event?,
        val creatorMessage: Message?,
        val edited: Boolean,
)
