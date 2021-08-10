package org.dreamexposure.discal.core.utils;

import org.dreamexposure.discal.core.enums.time.BadTimezone;

import java.time.ZoneId;

/**
 * Created by Nova Fox on 4/7/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class TimeZoneUtils {
    public static boolean isValid(final String value) {
        try {
            return ZoneId.getAvailableZoneIds().contains(value) && !isBadTz(value);
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isBadTz(final String value) {
        try {
            BadTimezone.valueOf(value.replaceAll("/", "_"));
            return true;
        } catch (final IllegalArgumentException e) {
            return false;
        }
    }
}
