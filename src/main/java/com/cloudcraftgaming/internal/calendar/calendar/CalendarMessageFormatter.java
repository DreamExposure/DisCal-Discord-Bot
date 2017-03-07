package com.cloudcraftgaming.internal.calendar.calendar;

import com.cloudcraftgaming.database.DatabaseManager;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;

import java.net.URI;

/**
 * Created by Nova Fox on 1/4/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class CalendarMessageFormatter {
    private static String lineBreak = System.getProperty("line.separator");

    public static String getCalendarLink(MessageReceivedEvent e) {
        String calId = DatabaseManager.getManager().getData(e.getMessage().getGuild().getID()).getCalendarAddress();
        URI callURI = URI.create(calId);
        return "https://calendar.google.com/calendar/embed?src=" + callURI;
    }

    public static String getFormatEventMessage(PreCalendar calendar) {
        return "~-~-~- Calendar Info ~-~-~-" + lineBreak
                + "Calendar ID: null until creation completed" + lineBreak + lineBreak
                + "Name/Summary: " + calendar.getSummary() + lineBreak + lineBreak
                + "Description: " + calendar.getDescription() + lineBreak + lineBreak
                + "TimeZone: " + calendar.getTimezone() + lineBreak + lineBreak
                + "Link: Unknown until confirmed.";
    }
}