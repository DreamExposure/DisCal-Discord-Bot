package com.cloudcraftgaming.discal.module.announcement;

/**
 * Created by Nova Fox on 3/4/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public enum AnnouncementType {
    UNIVERSAL, SPECIFIC;

    public static Boolean isValid(String _value) {
        return _value.equalsIgnoreCase("UNIVERSAL") || _value.equalsIgnoreCase("SPECIFIC");
    }

    public static AnnouncementType fromValue(String _value) {
        if (_value.equalsIgnoreCase("UNIVERSAL")) {
            return UNIVERSAL;
        } else {
            return SPECIFIC;
        }
    }
}