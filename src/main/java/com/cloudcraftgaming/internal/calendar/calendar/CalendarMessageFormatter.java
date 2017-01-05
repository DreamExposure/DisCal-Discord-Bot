package com.cloudcraftgaming.internal.calendar.calendar;

import com.google.api.services.calendar.model.Calendar;

/**
 * Created by Nova Fox on 1/4/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class CalendarMessageFormatter {
    private static String lineBreak = System.getProperty("line.separator");

    public static String getFormatEventMessage(Calendar calendar) {
        return "~-~-~- Calendar Info ~-~-~-" + lineBreak
                + "Calendar ID: " + calendar.getId() + lineBreak + lineBreak
                + "Name/Summery: " + calendar.getSummary() + lineBreak + lineBreak
                + "Description: " + calendar.getDescription() + lineBreak + lineBreak
                + "TimeZone: " + calendar.getTimeZone();
    }

    public static String getFormatEventMessage(PreCalendar calendar) {
        return "~-~-~- Calendar Info ~-~-~-" + lineBreak
                + "Calendar ID: null until creation completed" + lineBreak + lineBreak
                + "Name/Summery: " + calendar.getSummery() + lineBreak + lineBreak
                + "Description: " + calendar.getDescription() + lineBreak + lineBreak
                + "TimeZone: " + calendar.getTimezone();
    }
}