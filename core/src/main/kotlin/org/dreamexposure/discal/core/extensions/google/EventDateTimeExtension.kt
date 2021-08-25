package org.dreamexposure.discal.core.extensions.google

import com.google.api.services.calendar.model.EventDateTime
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

fun EventDateTime.asInstant(zone: ZoneId): Instant {
    return if (this.dateTime != null) {
        Instant.ofEpochMilli(this.dateTime.value)
    } else {
        Instant.ofEpochMilli(this.date.value)
              .plus(1, ChronoUnit.DAYS)
              .atZone(zone)
              .truncatedTo(ChronoUnit.DAYS)
              .toLocalDate()
              .atStartOfDay()
              .atZone(zone)
              .toInstant()
    }
}
