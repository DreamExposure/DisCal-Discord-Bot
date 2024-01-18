package org.dreamexposure.discal.core.extensions

import org.dreamexposure.discal.core.entities.Event
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.stream.Collectors


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

fun MutableList<String>.setFromString(strList: String) {
    this += strList.split(",").filter(String::isNotBlank)
}


fun MutableList<Event>.groupByDate(): Map<ZonedDateTime, List<Event>> {
    return this.stream()
        .collect(Collectors.groupingBy {
            ZonedDateTime.ofInstant(it.start, it.timezone).truncatedTo(ChronoUnit.DAYS)
                .with(TemporalAdjusters.ofDateAdjuster { identity -> identity })
        }).toSortedMap()

}

fun MutableList<Event>.groupByDateMulti(): Map<ZonedDateTime, List<Event>> {
    // First get a list of distinct dates each event starts on
    var rawDates = this.map {
        ZonedDateTime.ofInstant(it.start, it.timezone).truncatedTo(ChronoUnit.DAYS)
            .with(TemporalAdjusters.ofDateAdjuster { identity -> identity })
    }.toList()

    // Add days for multi-day events including end dates
    rawDates = rawDates.plus(this.asSequence().filter {
        it.isMultiDay()
    }.map {
        val start = ZonedDateTime.ofInstant(it.start, it.timezone).truncatedTo(ChronoUnit.DAYS)
            .with(TemporalAdjusters.ofDateAdjuster { identity -> identity })
        val end = ZonedDateTime.ofInstant(it.end, it.timezone).truncatedTo(ChronoUnit.DAYS)
            .with(TemporalAdjusters.ofDateAdjuster { identity -> identity })

        val days = listOf<ZonedDateTime>()
        var current = start
        while (current.isBefore(end)) {
            current = current.plusDays(1)
            days.plus(current)
        }

        days
    }.flatten())

    // Sort dates
    val sortedDates = rawDates.distinct().sorted().toList()

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
    return multi.toSortedMap()
}
