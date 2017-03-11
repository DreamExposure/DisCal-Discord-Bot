package com.cloudcraftgaming.internal.calendar.event;

/**
 * Created by Nova Fox on 3/10/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public enum EventFrequency {
    DAILY, MONTHLY, YEARLY;

    public static boolean isValid(String value) {
        return value.equalsIgnoreCase("DAILY") || value.equalsIgnoreCase("MONTHLY") || value.equalsIgnoreCase("YEARLY");
    }

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