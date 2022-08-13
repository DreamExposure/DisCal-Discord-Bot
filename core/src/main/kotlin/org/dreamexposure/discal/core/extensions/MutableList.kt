package org.dreamexposure.discal.core.extensions

import org.dreamexposure.discal.core.entities.Event
import org.dreamexposure.discal.core.logger.LOGGER
import java.time.Instant
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.stream.Collectors


fun MutableList<String>.asStringList(): String {
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

// TODO: This could use some optimization, but we'll leave it for now
fun MutableList<Event>.groupByDateMultiOld(): Map<ZonedDateTime, List<Event>> {
    // Each of the days events start on, their ending is ignored
    val dates = this.map {
        ZonedDateTime.ofInstant(it.start, it.timezone).truncatedTo(ChronoUnit.DAYS)
            .with(TemporalAdjusters.ofDateAdjuster { identity -> identity })
    }.distinct().sorted()

    val multi: MutableMap<ZonedDateTime, List<Event>> = mutableMapOf()

    dates.forEach {
        LOGGER.debug("Date: $it")

        val range = LongRange(it.toEpochSecond(), it.plusHours(23).plusMinutes(59).toEpochSecond())
        LOGGER.debug("Range: ${Instant.ofEpochSecond(range.first)} - ${Instant.ofEpochSecond(range.last)}")

        val events: MutableList<Event> = mutableListOf()

        this.forEach { event ->
            // When we check event end, we bump it back a second in order to prevent weirdness.
            if (range.contains(event.start.epochSecond) || range.contains(event.end.epochSecond - 1)) {
                LOGGER.debug("Event in range? Start: ${event.start} | End: ${event.end} | Name: ${event.name}")
                events.add(event)
            } else if (event.start.epochSecond < range.first && event.end.epochSecond > range.last) {
                // This is a multi-day event that starts before today and ends after today
                LOGGER.debug("Event extends beyond range? Start: ${event.start} | End: ${event.end} | Name: ${event.name}")
                events.add(event)
            }
        }
        LOGGER.debug("events in this range: ${events.size}")
        multi[it] = events
    }

    return multi.toSortedMap()
}

fun MutableList<Event>.groupByDateMulti(): Map<ZonedDateTime, List<Event>> {
    // First get a list of distinct dates each event starts on
    val rawDates = this.map {
        ZonedDateTime.ofInstant(it.start, it.timezone).truncatedTo(ChronoUnit.DAYS)
            .with(TemporalAdjusters.ofDateAdjuster { identity -> identity })
    }.toMutableList()

    // Add multi-day end dates if not already present
    rawDates += this.asSequence().filter {
        it.isMultiDay()
    }.map {
        ZonedDateTime.ofInstant(it.end, it.timezone).truncatedTo(ChronoUnit.DAYS)
            .with(TemporalAdjusters.ofDateAdjuster { identity -> identity })
    }

    // Sort dates
    val sortedDates = rawDates.distinct().sorted().toList()

    // Group events
    val multi = mutableMapOf<ZonedDateTime, List<Event>>()

    sortedDates.forEach {
        val range = LongRange(it.toEpochSecond(), it.plusHours(23).plusMinutes(59).toEpochSecond())
        val events = mutableListOf<Event>()

        this.forEach { event ->
            // When we check event end, we bump it back a second in order to prevent weirdness.
            if (range.contains(event.start.epochSecond) || range.contains(event.end.epochSecond - 1)) {
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
