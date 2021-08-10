package org.dreamexposure.discal.core.enums.time

enum class TimeFormat(val value: Int = 1) {
    TWENTY_FOUR_HOUR(1),
    TWELVE_HOUR(2);

    companion object {
        fun isValid(i: Int): Boolean {
            return i == 1 || i == 2
        }

        fun fromValue(i: Int): TimeFormat {
            return when (i) {
                1 -> TWENTY_FOUR_HOUR
                2 -> TWELVE_HOUR
                else -> TWENTY_FOUR_HOUR
            }
        }
    }
}
