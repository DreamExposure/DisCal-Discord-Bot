package org.dreamexposure.discal.core.`object`.calendar

import com.google.api.services.calendar.model.Calendar
import discord4j.core.`object`.entity.Message

data class CalendarCreatorResponse(
        val successful: Boolean,
        val edited: Boolean,
        val creatorMessage: Message?,
        val calendar: Calendar
)
