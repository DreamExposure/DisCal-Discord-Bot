package org.dreamexposure.discal.core.business

import org.dreamexposure.discal.core.`object`.new.Calendar
import org.dreamexposure.discal.core.`object`.new.CalendarMetadata
import org.dreamexposure.discal.core.`object`.new.CalendarMetadata.*

interface CalendarProvider {
    val host: Host

    suspend fun getCalendar(metadata: CalendarMetadata): Calendar?

    // TODO: Implement the rest of required CRUD functions
}