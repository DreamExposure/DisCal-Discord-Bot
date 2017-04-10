package com.cloudcraftgaming.discal.utils;

import org.joda.time.DateTimeZone;

/**
 * Created by Nova Fox on 4/7/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class TimeZoneUtils {
    public static boolean isValid(String value) {
        try {
            DateTimeZone tz = DateTimeZone.forID(value);
            return tz != null;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}