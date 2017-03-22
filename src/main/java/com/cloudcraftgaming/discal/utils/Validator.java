package com.cloudcraftgaming.discal.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Nova Fox on 3/22/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class Validator {
    /**
     * Checks whether or not a date has already past (IE: March 3, 1990).
     * @param dateRaw The date to check in format (yyyy/MM/dd-HH:mm:ss).
     * @return <code>true</code> if the date is in the past, otherwise <code>false</code>.
     */
    public static Boolean inPast(String dateRaw, TimeZone timezone) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss");
            sdf.setTimeZone(timezone);
            Date dateObj = sdf.parse(dateRaw);
            Date now = new Date(System.currentTimeMillis());

            return dateObj.before(now);

        } catch (ParseException e) {
            return true;
        }
    }
}