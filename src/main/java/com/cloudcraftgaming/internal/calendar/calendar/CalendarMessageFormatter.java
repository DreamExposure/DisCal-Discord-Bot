package com.cloudcraftgaming.internal.calendar.calendar;

import com.cloudcraftgaming.database.DatabaseManager;
import com.google.api.services.calendar.model.Calendar;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;

import java.net.URI;

/**
 * Created by Nova Fox on 1/4/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class CalendarMessageFormatter {
    private static String lineBreak = System.getProperty("line.separator");

    public static String getFormatEventMessage(MessageReceivedEvent event, Calendar calendar) {
        String calId = DatabaseManager.getManager().getData(event.getMessage().getGuild().getID()).getCalendarAddress();
        URI callURI = URI.create(calId);
        String link = "https://calendar.google.com/calendar/embed?src=" + callURI;
        return "~-~-~- Calendar Info ~-~-~-" + lineBreak
                + "Calendar ID: " + calendar.getId() + lineBreak + lineBreak
                + "Name/Summery: " + calendar.getSummary() + lineBreak + lineBreak
                + "Description: " + calendar.getDescription() + lineBreak + lineBreak
                + "TimeZone: " + calendar.getTimeZone() + lineBreak + lineBreak
                + "Link: " + link;
    }

    public static String getFormatEventMessage(PreCalendar calendar) {
        return "~-~-~- Calendar Info ~-~-~-" + lineBreak
                + "Calendar ID: null until creation completed" + lineBreak + lineBreak
                + "Name/Summery: " + calendar.getSummery() + lineBreak + lineBreak
                + "Description: " + calendar.getDescription() + lineBreak + lineBreak
                + "TimeZone: " + calendar.getTimezone() + lineBreak + lineBreak
                + "Link: Unknown until confirmed.";
    }
}