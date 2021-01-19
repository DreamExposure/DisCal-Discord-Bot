package org.dreamexposure.discal.core.enums.event

enum class EventFrequency {
    DAILY, WEEKLY, MONTHLY, YEARLY;

    companion object {
        fun isValid(value: String): Boolean {
            return value.equals("DAILY", true)
                    || value.equals("WEEKLY", true)
                    || value.equals("MONTHLY", true)
                    || value.equals("YEARLY", true)
        }

        fun fromValue(value: String): EventFrequency {
            return when (value.toUpperCase()) {
                "WEEKLY" -> WEEKLY
                "MONTHLY" -> MONTHLY
                "YEARLY" -> YEARLY
                else -> DAILY
            }
        }
    }
}
