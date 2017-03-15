package com.cloudcraftgaming.internal.calendar.event;

import com.cloudcraftgaming.database.DatabaseManager;
import com.cloudcraftgaming.internal.calendar.CalendarAuth;
import com.cloudcraftgaming.internal.email.EmailSender;
import com.google.api.services.calendar.Calendar;

import java.io.IOException;

/**
 * Created by Nova Fox on 1/4/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class EventUtils {
    public static Boolean deleteEvent(String guildId, String eventId) {
        String calendarId = DatabaseManager.getManager().getData(guildId).getCalendarAddress();
        try {
            Calendar service = CalendarAuth.getCalendarService();
            service.events().delete(calendarId, eventId).execute();
            return true;
        } catch (IOException e) {
            System.out.println("Something weird happened when deleting an event!");
            EmailSender.getSender().sendExceptionEmail(e, EventUtils.class);
            e.printStackTrace();
        }
        return false;
    }
}