package org.dreamexposure.discal.core.`object`.new

import java.time.DayOfWeek
import java.time.Month

data class EventRecurrence(
    val frequency: Frequency = Frequency.DAILY,
    val interval: Int = 1,
    val count: Int? = null,
    val bySetPos: SetPos? = null,
    val byDay: List<Day> = emptyList(),
    val byMonthDay: Int? = null,
    val byMonth: Month? = null,
) {
    // Companion object to translate from rrule string
    companion object {
        fun fromRRule(rrule: String): EventRecurrence {
            var frequency = Frequency.DAILY
            var interval = 1
            var count: Int? = null
            var bySetPos: SetPos? = null
            var byDay = emptyList<Day>()
            var byMonthDay: Int? = null
            var byMonth: Month? = null

            rrule.replace("RRULE:", "").split(";").forEach {
                when {
                    it.contains("FREQ=") -> frequency = Frequency.valueOf(it.replace("FREQ=", ""))
                    it.contains("INTERVAL=") -> try {
                        interval = it.replace("INTERVAL=", "").toInt()
                    } catch (_: NumberFormatException) {}
                    it.contains("COUNT=") -> try {
                        count = it.replace("COUNT=", "").toInt()
                    } catch (_: NumberFormatException) {}
                    it.contains("BYSETPOS=") -> try {
                        bySetPos = SetPos.entries.firstOrNull { v -> v.value == it.replace("BYSETPOS=", "").toInt() }
                    } catch (_: NumberFormatException) {}
                    it.contains("BYDAY=") -> byDay = it.replace("BYDAY=", "").split(",").map { dv -> Day.valueOf(dv) }
                    it.contains("BYMONTHDAY=") -> try {
                        byMonthDay = it.replace("BYMONTHDAY=", "").toInt()
                    } catch (_: NumberFormatException) {}
                    it.contains("BYMONTH=") -> try {
                        byMonth = Month.of(it.replace("BYMONTH=", "").toInt())
                    } catch (_: NumberFormatException) {}
                }
            }

            return EventRecurrence(frequency, interval, count, bySetPos, byDay, byMonthDay, byMonth)
        }
    }

    // Some helpful functions
    fun asRRule(): String {
        val rrule = StringBuilder()
            .append("RRULE:")
            .append("FREQ=${frequency.name};")
            .append("INTERVAL=${interval};")

        if (count != null) rrule.append("COUNT=${count};")
        if (bySetPos != null) rrule.append("BYSETPOS=${bySetPos};")
        if (byDay.isNotEmpty()) rrule.append("BYDAY=${byDay.joinToString(",") { it.value }};")
        if (byMonthDay != null) rrule.append("BYMONTHDAY=${byMonthDay};")
        if (byMonth != null) rrule.append("BYMONTH=${byMonth.value};")

        return rrule.toString()
    }

    fun asHumanReadable(): String {
        val builder = StringBuilder()
            .append("Repeat ${frequency.name} every $interval ")

        when (frequency) {
            Frequency.DAILY -> builder.append("day(s) ")
            Frequency.WEEKLY -> builder.append("week(s) ")
            Frequency.MONTHLY -> builder.append("month(s) ")
            Frequency.YEARLY -> builder.append("year(s) ")
        }

        if (byMonth != null && byMonthDay != null) builder.append("on ${byMonth.name} $byMonthDay ")
        else if (byMonthDay != null) builder.append("$byMonthDay ")

        if (byMonth != null && bySetPos != null && byDay.isNotEmpty()) builder.append("on the ${bySetPos.name} ${byDay.joinToString(",")} of ${byMonth.name} ")
        else if (bySetPos != null && byDay.isNotEmpty()) builder.append("on the ${bySetPos.name} ${byDay.joinToString(",")} ")
        else if (byDay.isNotEmpty()) builder.append("on ${byDay.joinToString(",")} ")

        if (count != null) builder.append("End after $count occurrence(s)")

        return builder.toString()
    }

    ////////////////////////////
    ////// Nested classes //////
    ////////////////////////////
    enum class Frequency {
        DAILY,
        WEEKLY,
        MONTHLY,
        YEARLY,
    }

    enum class SetPos(val value: Int) {
        FIRST(1),
        SECOND(2),
        THIRD(3),
        FOURTH(4),
        LAST(-1),
    }

    enum class Day(val value: String, val dayOfWeek: DayOfWeek) {
        SUNDAY("SU", DayOfWeek.SUNDAY),
        MONDAY("MO", DayOfWeek.MONDAY),
        TUESDAY("TU", DayOfWeek.TUESDAY),
        WEDNESDAY("WE", DayOfWeek.WEDNESDAY),
        THURSDAY("TH", DayOfWeek.THURSDAY),
        FRIDAY("FR", DayOfWeek.FRIDAY),
        SATURDAY("SA", DayOfWeek.SATURDAY),
    }
}