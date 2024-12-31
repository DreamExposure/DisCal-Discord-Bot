package org.dreamexposure.discal.core.extensions

import org.dreamexposure.discal.core.`object`.new.Event
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters


fun List<String>.asStringList(): String {
    val builder = StringBuilder()

    for ((i, str) in this.withIndex()) {
        if (str.isNotBlank()) {
            if (i == 0) builder.append(str)
            else builder.append(",").append(str)
        }
    }

    return builder.toString()
}

fun List<Event>.groupByDate(): Map<ZonedDateTime, List<Event>> {
    if (this.isEmpty()) return emptyMap()

    // Get a list of all distinct days events take place on (the first start date and the last end date)
    val startRange = this.map { it.start }.minOf { it }
    val endRange = this.map { it.end }.maxOf { it }
    val startDate = ZonedDateTime.ofInstant(startRange, this.first().timezone).truncatedTo(ChronoUnit.DAYS)
        .with(TemporalAdjusters.ofDateAdjuster { identity -> identity })
    val endDate = ZonedDateTime.ofInstant(endRange, this.first().timezone).truncatedTo(ChronoUnit.DAYS)
        .with(TemporalAdjusters.ofDateAdjuster { identity -> identity })

    // Get a list of all days between the start and end dates
    val days = mutableListOf<ZonedDateTime>(startDate, endDate)
    var current = startDate
    while (current.isBefore(endDate)) {
        current = current.plusDays(1)
        days.add(current)
    }

    // Sort dates
    val sortedDates = days.distinct().sorted().toList()

    // Group events
    val multi = mutableMapOf<ZonedDateTime, List<Event>>()

    sortedDates.forEach {
        val range = LongRange(it.toEpochSecond(), it.plusHours(23).plusMinutes(59).plusSeconds(59).toEpochSecond())
        val events = mutableListOf<Event>()

        this.forEach { event ->
            // When we check event end, we bump it back a couple seconds in order to prevent weirdness.
            if (range.contains(event.start.epochSecond) || range.contains(event.end.epochSecond - 2)) {
                // Event in range, add to list
                events.add(event)
            } else if (event.start.epochSecond < range.first && event.end.epochSecond > range.last) {
                // This is a multi-day event that starts before today and ends after today
                events.add(event)
            }
        }
        multi[it] = events.sortedBy(Event::start)
    }
    return multi.filter { it.value.isNotEmpty() }.toSortedMap()
}
