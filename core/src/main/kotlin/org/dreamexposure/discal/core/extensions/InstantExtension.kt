package org.dreamexposure.discal.core.extensions

import org.dreamexposure.discal.core.enums.time.DiscordTimestampFormat
import org.dreamexposure.discal.core.enums.time.TimeFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun Instant.asDiscordTimestamp(fmt: DiscordTimestampFormat) = "<t:${this.toEpochMilli() / 1000}:${fmt.value}>"

fun Instant.humanReadableFull(timezone: ZoneId, format: TimeFormat): String {
    return DateTimeFormatter.ofPattern(format.full).withZone(timezone).format(this)
}

fun Instant.humanReadableFullSimple(timezone: ZoneId, format: TimeFormat): String {
    return DateTimeFormatter.ofPattern(format.fullSimple).withZone(timezone).format(this)
}

fun Instant.humanReadableDate(timezone: ZoneId, format: TimeFormat, long: Boolean = true, longDay: Boolean = false): String {
    return if (long && longDay) DateTimeFormatter.ofPattern(format.dayOfWeek).withZone(timezone).format(this)
    else if (long) DateTimeFormatter.ofPattern(format.longDate).withZone(timezone).format(this)
    else DateTimeFormatter.ofPattern(format.date).withZone(timezone).format(this)
}

fun Instant.humanReadableTime(timezone: ZoneId, format: TimeFormat): String {
    return DateTimeFormatter.ofPattern(format.time).withZone(timezone).format(this)
}

fun Instant.isExpiredTtl(): Boolean = Instant.now().isAfter(this)
