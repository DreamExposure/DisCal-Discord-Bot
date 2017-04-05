package com.cloudcraftgaming.discal.module.announcement;

/**
 * Created by Nova Fox on 3/4/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public enum AnnouncementType {
    UNIVERSAL, SPECIFIC, COLOR;

    /**
     * Checks if the specified value is a valid AnnouncementType.
     * @param _value The value to check.
     * @return <code>true</code> if value, otherwise <code>false</code>.
     */
    public static Boolean isValid(String _value) {
        return _value.equalsIgnoreCase("UNIVERSAL") || _value.equalsIgnoreCase("SPECIFIC") || _value.equalsIgnoreCase("COLOR") || _value.equalsIgnoreCase("COLOUR");
    }

    /**
     * Gets the AnnouncementType from the value.
     * @param _value The value to check.
     * @return The AnnouncementType.
     */
    public static AnnouncementType fromValue(String _value) {
        switch (_value.toUpperCase()) {
            case "UNIVERSAL":
                return UNIVERSAL;
            case "SPECIFIC":
                return SPECIFIC;
            case "COLOR":
                return COLOR;
            case "COLOUR":
                return COLOR;
            default:
                return UNIVERSAL;
        }
    }
}