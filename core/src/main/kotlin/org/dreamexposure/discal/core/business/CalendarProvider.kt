package org.dreamexposure.discal.core.business

import discord4j.common.util.Snowflake
import org.dreamexposure.discal.core.`object`.new.Calendar
import org.dreamexposure.discal.core.`object`.new.CalendarMetadata
import org.dreamexposure.discal.core.`object`.new.CalendarMetadata.Host
import org.dreamexposure.discal.core.`object`.new.Event
import java.time.Instant

interface CalendarProvider {
    val host: Host

    /////////
    /// Calendar functions
    /////////
    suspend fun getCalendar(metadata: CalendarMetadata): Calendar?

    suspend fun createCalendar(guildId: Snowflake, spec: Calendar.CreateSpec): Calendar

    suspend fun updateCalendar(guildId: Snowflake, metadata: CalendarMetadata, spec: Calendar.UpdateSpec): Calendar

    suspend fun deleteCalendar(guildId: Snowflake, metadata: CalendarMetadata)


    /////////
    /// Event functions
    /////////
    suspend fun getEvent(calendar: Calendar, id: String): Event?

    suspend fun getUpcomingEvents(calendar: Calendar, amount: Int, maxDays: Int? = null): List<Event>

    suspend fun getOngoingEvents(calendar: Calendar): List<Event>

    suspend fun getEventsInTimeRange(calendar: Calendar, start: Instant, end: Instant): List<Event>

    suspend fun createEvent(calendar: Calendar, spec: Event.CreateSpec): Event

    suspend fun updateEvent(calendar: Calendar, spec: Event.UpdateSpec): Event

    suspend fun deleteEvent(calendar: Calendar, id: String)
}
