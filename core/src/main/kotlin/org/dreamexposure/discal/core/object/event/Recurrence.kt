package org.dreamexposure.discal.core.`object`.event

import kotlinx.serialization.Serializable
import org.dreamexposure.discal.core.enums.event.EventFrequency
import org.dreamexposure.discal.core.utils.GlobalConst

@Serializable
data class Recurrence(
        val frequency: EventFrequency = EventFrequency.DAILY,
        val interval: Int = 1,
        val count: Int = -1
) {
    companion object {
        fun fromRRule(rrule: String): Recurrence {
            val contents = rrule.replace("RRULE:", "").split(";")

            var recur = Recurrence()

            for (c in contents) {
                when {
                    c.contains("FREQ=") -> {
                        val freq = c.replace("FREQ=", "")
                        if (EventFrequency.isValid(freq)) recur = recur.copy(frequency = EventFrequency.fromValue(freq))
                    }
                    c.contains("INTERVAL=") -> {
                        val inter = c.replace("INTERVAL=", "")
                        try {
                            recur = recur.copy(interval = inter.toInt())
                        } catch (ignore: NumberFormatException) {
                        }
                    }
                    c.contains("COUNT=") -> {
                        val con = c.replaceAfter("COUNT=", "")
                        try {
                            recur = recur.copy(count = con.toInt())
                        } catch (ignore: NumberFormatException) {
                        }
                    }
                }
            }

            return recur
        }
    }

    fun toRRule(): String {
        val rrule = "RRULE:FREQ=${this.frequency.name};INTERVAL=${this.interval}"

        return if (this.count < 1) rrule
        else "${rrule};COUNT=${this.count}"
    }

    fun toHumanReadable(): String {
        val read = "Frequency: ${this.frequency.name}${GlobalConst.lineBreak}Interval: ${this.interval}"

        return if (this.count < 1) "$read${GlobalConst.lineBreak} Amount: Infinite"
        else "$read${GlobalConst.lineBreak} Amount: ${this.count}"
    }
}
