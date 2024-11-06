package org.dreamexposure.discal.core.business

import discord4j.common.util.Snowflake
import org.dreamexposure.discal.core.`object`.new.Calendar
import org.dreamexposure.discal.core.`object`.new.CalendarMetadata
import org.dreamexposure.discal.core.`object`.new.CalendarMetadata.*

interface CalendarProvider {
    val host: Host

    suspend fun getCalendar(metadata: CalendarMetadata): Calendar?

    suspend fun createCalendar(guildId: Snowflake, spec: Calendar.CreateSpec): Calendar

    suspend fun updateCalendar(guildId: Snowflake, metadata: CalendarMetadata, spec: Calendar.UpdateSpec): Calendar

    suspend fun deleteCalendar(guildId: Snowflake, metadata: CalendarMetadata)

    // TODO: Implement the rest of required CRUD functions
}
