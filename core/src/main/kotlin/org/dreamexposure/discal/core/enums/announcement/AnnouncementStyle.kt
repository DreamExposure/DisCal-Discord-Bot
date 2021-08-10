package org.dreamexposure.discal.core.enums.announcement

enum class AnnouncementStyle(val value: Int = 1) {
    FULL(1), SIMPLE(2), EVENT(3);

    companion object {
        fun isValid(i: Int): Boolean {
            return i in 1..3
        }

        fun fromValue(i: Int): AnnouncementStyle {
            return when (i) {
                1 -> FULL
                2 -> SIMPLE
                3 -> EVENT
                else -> FULL
            }
        }
    }
}
