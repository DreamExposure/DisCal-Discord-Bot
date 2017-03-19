package com.cloudcraftgaming.discal.internal.calendar.event;

/**
 * Created by Nova Fox on 3/10/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public enum EventFrequency {
    DAILY, MONTHLY, YEARLY;

    /**
     * Checks if the value is a valid enum value.
     * @param value The value to check.
     * @return <code>true</code> if valid, else <code>false</code>.
     */
    public static boolean isValid(String value) {
        return value.equalsIgnoreCase("DAILY") || value.equalsIgnoreCase("MONTHLY") || value.equalsIgnoreCase("YEARLY");
    }

    /**
     * Gets the enum value for the specified string value.
     * @param value The value to get from.
     * @return The enum value.
     */
    public static EventFrequency fromValue(String value) {
        if (value.equalsIgnoreCase("DAILY")) {
            return DAILY;
        } else if (value.equalsIgnoreCase("MONTHLY")) {
            return MONTHLY;
        } else {
            return YEARLY;
        }
    }
}