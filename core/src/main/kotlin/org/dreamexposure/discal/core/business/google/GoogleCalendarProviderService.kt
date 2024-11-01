package org.dreamexposure.discal.core.business.google

import org.dreamexposure.discal.core.business.CalendarProvider
import org.dreamexposure.discal.core.`object`.new.Calendar
import org.dreamexposure.discal.core.`object`.new.CalendarMetadata
import org.springframework.stereotype.Component
import java.time.ZoneId

@Component
class GoogleCalendarProviderService(
    val googleCalendarApiWrapper: GoogleCalendarApiWrapper,
) : CalendarProvider {
    override val host = CalendarMetadata.Host.GOOGLE

    override suspend fun getCalendar(metadata: CalendarMetadata): Calendar? {
        val googleCalendar = googleCalendarApiWrapper.getCalendar(metadata) ?: return null

        return Calendar(
            metadata = metadata,
            name = googleCalendar.summary.orEmpty(),
            description = googleCalendar.description.orEmpty(),
            timezone = ZoneId.of(googleCalendar.timeZone),
            hostLink = "https://calendar.google.com/calendar/embed?src=${metadata.id}"
        )
    }
}