package org.dreamexposure.discal.core.extensions

import org.dreamexposure.discal.core.entities.Event
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


fun MutableList<Event>.groupByDate(): Map<ZonedDateTime, List<Event>> {
    return this.stream()
            .collect(Collectors.groupingBy {
                ZonedDateTime.ofInstant(it.start, it.timezone).truncatedTo(ChronoUnit.DAYS)
                        .with(TemporalAdjusters.ofDateAdjuster { identity -> identity })
            }).toSortedMap()

}
