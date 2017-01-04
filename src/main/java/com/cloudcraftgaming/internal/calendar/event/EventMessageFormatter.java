package com.cloudcraftgaming.internal.calendar.event;

import com.cloudcraftgaming.internal.calendar.event.PreEvent;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

import javax.annotation.Nullable;

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
                + "Start Date (yyyy/MM/dd): " + getHumanReadableDate(event) + lineBreak
                + "Start Time (HH:mm): " + getHumanReadableTime(event) + lineBreak
                + "End Date (yyyy/MM/dd): " + getHumanReadableDate(event) + lineBreak
                + "End Time (HH:mm): " + getHumanReadableTime(event) + lineBreak
                + "TimeZone: U.S. Central.";
    }

    public static String getFormatEventMessage(PreEvent event) {
        return "~-~-~- Event Info ~-~-~-" + lineBreak
                + "Event ID: null until creation completed" + lineBreak + lineBreak
                + "Summery: " + event.getSummery() + lineBreak + lineBreak
                + "Description: " + event.getDescription() + lineBreak + lineBreak
                + "[REQ] Start Date (yyyy/MM/dd): " + getHumanReadableDate(event.getStartDateTime()) + lineBreak
                + "[REQ] Start Time (HH:mm): " + getHumanReadableTime(event.getStartDateTime()) + lineBreak
                + "[REQ] End Date (yyyy/MM/dd): " + getHumanReadableDate(event.getEndDateTime()) + lineBreak
                + "[REQ] End Time (HH:mm): " + getHumanReadableTime(event.getEndDateTime()) + lineBreak
                + "TimeZone: U.S. Central.";
    }

    private static String getHumanReadableDate(Event event) {
        String[] dateArray = event.getStart().getDateTime().toStringRfc3339().split("-");
        String year = dateArray[0];
        String month = dateArray[1];
        String day = dateArray[2].substring(0, 2);

        return year + "/" + month + "/" + day;
    }

    public static String getHumanReadableDate(@Nullable EventDateTime eventDateTime) {
        if (eventDateTime == null) {
            return "Not Set";
        } else {
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
    }


    private static String getHumanReadableTime(Event event) {
        String[] timeArray = event.getStart().getDateTime().toStringRfc3339().split(":");
        String hour = timeArray[0].substring(11, 13);
        String minute = timeArray[1];

        return hour + ":" + minute;
    }

    public static String getHumanReadableTime(@Nullable EventDateTime eventDateTime) {
        if (eventDateTime == null) {
            return "Not Set";
        } else {
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
}
