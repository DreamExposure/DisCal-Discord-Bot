package org.dreamexposure.discal.core.enums.announcement

enum class AnnouncementModifier {
    BEFORE, DURING, END;

    companion object {
        fun isValid(value: String): Boolean {
            return value.equals("BEFORE", true)
                    || value.equals("B4", true)
                    || value.equals("DURING", true)
                    || value.equals("END", true)
        }

        fun fromValue(value: String): AnnouncementModifier {
            return when (value.uppercase()) {
                "DURING" -> DURING
                "END" -> END
                else -> BEFORE
            }
        }
    }
}
