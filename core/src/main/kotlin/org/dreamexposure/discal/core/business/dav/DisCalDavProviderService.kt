package org.dreamexposure.discal.core.business.dav

import discord4j.common.util.Snowflake
import org.dreamexposure.discal.core.business.CalendarProvider
import org.dreamexposure.discal.core.`object`.new.Calendar
import org.dreamexposure.discal.core.`object`.new.CalendarMetadata
import org.dreamexposure.discal.core.`object`.new.Event
import java.time.Instant

class DisCalDavProviderService(
    
) : CalendarProvider {
    override val host = CalendarMetadata.Host.DISCAL_DAV

    override suspend fun getCalendar(metadata: CalendarMetadata): Calendar? {
        TODO("Not yet implemented")
    }

    override suspend fun createCalendar(guildId: Snowflake, spec: Calendar.CreateSpec): Calendar {
        TODO("Not yet implemented")
    }

    override suspend fun updateCalendar(guildId: Snowflake, metadata: CalendarMetadata, spec: Calendar.UpdateSpec): Calendar {
        TODO("Not yet implemented")
    }

    override suspend fun deleteCalendar(guildId: Snowflake, metadata: CalendarMetadata) {
        TODO("Not yet implemented")
    }

    override suspend fun getEvent(calendar: Calendar, id: String): Event? {
        TODO("Not yet implemented")
    }

    override suspend fun getUpcomingEvents(calendar: Calendar, amount: Int, maxDays: Int?): List<Event> {
        TODO("Not yet implemented")
    }

    override suspend fun getOngoingEvents(calendar: Calendar): List<Event> {
        TODO("Not yet implemented")
    }

    override suspend fun getEventsInTimeRange(calendar: Calendar, start: Instant, end: Instant): List<Event> {
        TODO("Not yet implemented")
    }

    override suspend fun createEvent(calendar: Calendar, spec: Event.CreateSpec): Event {
        TODO("Not yet implemented")
    }

    override suspend fun updateEvent(calendar: Calendar, spec: Event.UpdateSpec): Event {
        TODO("Not yet implemented")
    }

    override suspend fun deleteEvent(calendar: Calendar, id: String) {
        TODO("Not yet implemented")
    }
}