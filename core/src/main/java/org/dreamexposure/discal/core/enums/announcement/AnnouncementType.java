package org.dreamexposure.discal.core.enums.announcement;

/**
 * Created by Nova Fox on 11/10/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
public enum AnnouncementType {
    UNIVERSAL, SPECIFIC, COLOR, RECUR;

    /**
     * Checks if the specified value is a valid AnnouncementType.
     *
     * @param value The value to check.
     * @return {@code true} if value, otherwise {@code false}.
     */
    public static boolean isValid(final String value) {
        return "UNIVERSAL".equalsIgnoreCase(value)
            || "SPECIFIC".equalsIgnoreCase(value)
            || "COLOR".equalsIgnoreCase(value)
            || "COLOUR".equalsIgnoreCase(value)
            || "RECUR".equalsIgnoreCase(value);
    }

    /**
     * Gets the AnnouncementType from the value.
     *
     * @param value The value to check.
     * @return The AnnouncementType.
     */
    public static AnnouncementType fromValue(final String value) {
        switch (value.toUpperCase()) {
            case "SPECIFIC":
                return SPECIFIC;
            case "COLOR":
            case "COLOUR":
                return COLOR;
            case "RECUR":
                return RECUR;
            default:
                return UNIVERSAL;
        }
    }
}