package org.dreamexposure.discal.core.utils;

import org.dreamexposure.discal.core.enums.BadTimezone;
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
            return tz != null && !isBadTz(value);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static boolean isBadTz(String value) {
        try {
            BadTimezone.valueOf(value.replaceAll("/", "_"));
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}