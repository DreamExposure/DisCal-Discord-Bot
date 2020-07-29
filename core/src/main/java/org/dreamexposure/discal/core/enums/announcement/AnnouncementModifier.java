package org.dreamexposure.discal.core.enums.announcement;

public enum AnnouncementModifier {
    BEFORE, DURING, END;

    public static Boolean isValid(String _value) {
        return _value.equalsIgnoreCase("BEFORE") || _value.equalsIgnoreCase("B4") || _value.equalsIgnoreCase("DURING")
            || _value.equalsIgnoreCase("END");
    }

    public static AnnouncementModifier fromValue(String value) {
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
