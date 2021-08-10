package org.dreamexposure.discal.core.enums.announcement

enum class AnnouncementType {
    UNIVERSAL, SPECIFIC, COLOR, RECUR;

    companion object {
        fun isValid(value: String): Boolean {
            return value.equals("UNIVERSAL", true)
                    || value.equals("SPECIFIC", true)
                    || value.equals("COLOR", true)
                    || value.equals("COLOUR", true)
                    || value.equals("RECUR", true)
        }

        fun fromValue(value: String): AnnouncementType {
            return when (value.uppercase()) {
                "SPECIFIC" -> SPECIFIC
                "COLOR" -> COLOR
                "COLOUR" -> COLOR
                "RECUR" -> RECUR
                else -> UNIVERSAL
            }
        }
    }
}
