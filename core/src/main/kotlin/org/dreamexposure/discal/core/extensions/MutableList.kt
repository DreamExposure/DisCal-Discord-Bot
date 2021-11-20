package org.dreamexposure.discal.core.extensions

import org.dreamexposure.discal.core.entities.Event
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import java.util.stream.Collectors


fun MutableList<String>.asStringList(): String {
    val builder = StringBuilder()

    for ((i, str) in this.withIndex()) {
        if (str.isNotBlank()) {
            if (i == 0) builder.append(str)
            else builder.append(",").append(builder)
        }
    }

    return builder.toString()
}


fun MutableList<Event>.groupByDate(): Map<LocalDate, List<Event>> {
    return this.stream()
            .collect(Collectors.groupingBy {
                LocalDate.ofInstant(it.start, it.timezone)
                        .with(TemporalAdjusters.ofDateAdjuster { identity -> identity })
            })

}
