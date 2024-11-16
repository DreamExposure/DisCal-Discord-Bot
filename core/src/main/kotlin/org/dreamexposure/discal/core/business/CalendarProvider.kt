package org.dreamexposure.discal.core.business

import discord4j.common.util.Snowflake
import org.dreamexposure.discal.core.`object`.new.Calendar
import org.dreamexposure.discal.core.`object`.new.CalendarMetadata
import org.dreamexposure.discal.core.`object`.new.CalendarMetadata.Host
import org.dreamexposure.discal.core.`object`.new.Event
import java.time.Instant

interface CalendarProvider {
    val host: Host

    suspend fun getCalendar(metadata: CalendarMetadata): Calendar?

    suspend fun createCalendar(guildId: Snowflake, spec: Calendar.CreateSpec): Calendar

    suspend fun updateCalendar(guildId: Snowflake, metadata: CalendarMetadata, spec: Calendar.UpdateSpec): Calendar

    suspend fun deleteCalendar(guildId: Snowflake, metadata: CalendarMetadata)



    suspend fun getEvent(guildId: Snowflake, calendar: Calendar, id: String): Event?

    suspend fun getUpcomingEvents(guildId: Snowflake, calendar: Calendar, amount: Int): List<Event>

    suspend fun getOngoingEvents(guildId: Snowflake, calendar: Calendar): List<Event>

    suspend fun getEventsInTimeRange(guildId: Snowflake, calendar: Calendar, start: Instant, end: Instant): List<Event>
    // TODO: Implement the rest of required CRUD functions
}
