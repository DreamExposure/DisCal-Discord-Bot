package org.dreamexposure.discal.core.enums.event;

/**
 * Created by Nova Fox on 11/10/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
public enum EventFrequency {
    HOURLY, DAILY, WEEKLY, MONTHLY, YEARLY;

    /**
     * Checks if the value is a valid enum value.
     *
     * @param value The value to check.
     * @return {@code true} if valid, else {@code false}.
     */
    public static boolean isValid(final String value) {
        return "DAILY".equalsIgnoreCase(value) || "WEEKLY".equalsIgnoreCase(value) || "MONTHLY".equalsIgnoreCase(value)
            || "YEARLY".equalsIgnoreCase(value);
    }

    /**
     * Gets the enum value for the specified string value.
     *
     * @param value The value to get from.
     * @return The enum value.
     */
    public static EventFrequency fromValue(final String value) {
        switch (value.toUpperCase()) {
            case "WEEKLY":
                return WEEKLY;
            case "MONTHLY":
                return MONTHLY;
            case "YEARLY":
                return YEARLY;
            default:
                return DAILY;
        }
    }

    public String getName() {
        return this.name();
    }
}