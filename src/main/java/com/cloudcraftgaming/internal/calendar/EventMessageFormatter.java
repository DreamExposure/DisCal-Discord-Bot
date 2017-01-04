package com.cloudcraftgaming.internal.calendar;

import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

/**
 * Created by Nova Fox on 1/3/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class EventMessageFormatter {
    private static String lineBreak = System.getProperty("line.separator");

    public static String getFormatEventMessage(Event event) {
        return "~-~-~- Event Info ~-~-~-" + lineBreak
                + "Event ID: " + event.getId() + lineBreak + lineBreak
                + "Summery: " + event.getSummary() + lineBreak + lineBreak
                + "Description: " + event.getDescription() + lineBreak + lineBreak
                + "Date (yyyy/MM/dd): " + getHumanReadableDate(event) + lineBreak
                + "Time (HH:mm): " + getHumanReadableTime(event) + lineBreak
                + "TimeZone: U.S. Central.";
    }

    public static String getHumanReadableDate(Event event) {
        String[] dateArray = event.getStart().getDateTime().toStringRfc3339().split("-");
        String year = dateArray[0];
        String month = dateArray[1];
        String day = dateArray[2].substring(0, 2);

        return year + "/" + month + "/" + day;
    }

    public static String getHumanReadableDate(EventDateTime eventDateTime) {
        if (eventDateTime.getDateTime() != null) {
            String[] dateArray = eventDateTime.getDateTime().toStringRfc3339().split("-");
            String year = dateArray[0];
            String month = dateArray[1];
            String day = dateArray[2].substring(0, 2);

            return year + "/" + month + "/" + day;
        } else {
            String[] dateArray = eventDateTime.getDate().toStringRfc3339().split("-");
            String year = dateArray[0];
            String month = dateArray[1];
            String day = dateArray[2].substring(0, 2);

            return year + "/" + month + "/" + day;
        }
    }


    public static String getHumanReadableTime(Event event) {
        String[] timeArray = event.getStart().getDateTime().toStringRfc3339().split(":");
        String hour = timeArray[0].substring(11, 13);
        String minute = timeArray[1];

        return hour + ":" + minute;
    }

    public static String getHumanReadableTime(EventDateTime eventDateTime) {
        if (eventDateTime.getDateTime() != null) {
            String[] timeArray = eventDateTime.getDateTime().toStringRfc3339().split(":");
            String hour = timeArray[0].substring(11, 13);
            String minute = timeArray[1];

            return hour + ":" + minute;
        } else {
            String[] timeArray = eventDateTime.getDate().toStringRfc3339().split(":");
            String hour = timeArray[0].substring(11, 13);
            String minute = timeArray[1];

            return hour + ":" + minute;
        }
    }
}
