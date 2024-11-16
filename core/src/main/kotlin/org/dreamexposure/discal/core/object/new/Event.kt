package org.dreamexposure.discal.core.`object`.new

import discord4j.common.util.Snowflake
import org.dreamexposure.discal.core.enums.event.EventColor
import org.dreamexposure.discal.core.`object`.event.Recurrence
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

data class Event(
    val id: String,
    val guildId: Snowflake,
    val calendarNumber: Int,
    val name: String,
    val description: String,
    val location: String,
    val link: String,
    val color: EventColor,
    val start: Instant,
    val end: Instant,
    val recur: Boolean,
    val recurrence: Recurrence,
    val image: String,
    val timezone: ZoneId,
) {
    // Some helpful functions
    fun isOngoing(): Boolean = start.isBefore(Instant.now()) && end.isAfter(Instant.now())

    fun isOver(): Boolean = end.isBefore(Instant.now())

    fun isStarted() = start.isBefore(Instant.now())

    fun is24Hours() = Duration.between(start, end).toHours() == 24L

    fun isAllDay(): Boolean {
        val start = this.start.atZone(timezone)

        return start.hour == 0 && is24Hours()
    }

    fun isMultiDay(): Boolean {
        if (isAllDay()) return false // All day events should not count as multi-day events

        val start = this.start.atZone(timezone).truncatedTo(ChronoUnit.DAYS)
        val end = this.end.atZone(timezone).truncatedTo(ChronoUnit.DAYS)

        return when {
            start.year != end.year -> true
            start.month != end.month -> true
            start.dayOfYear != end.dayOfYear -> true
            else -> false
        }
    }
}

