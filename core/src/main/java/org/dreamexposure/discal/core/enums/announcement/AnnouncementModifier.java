package org.dreamexposure.discal.core.enums.announcement;

public enum AnnouncementModifier {
    BEFORE, DURING, END;

    public static boolean isValid(final String _value) {
        return "BEFORE".equalsIgnoreCase(_value) || "B4".equalsIgnoreCase(_value) || "DURING".equalsIgnoreCase(_value)
            || "END".equalsIgnoreCase(_value);
    }

    public static AnnouncementModifier fromValue(final String value) {
        switch (value.toUpperCase()) {
            case "DURING":
                return DURING;
            case "END":
                return END;
            default:
                return BEFORE;
        }
    }
}
