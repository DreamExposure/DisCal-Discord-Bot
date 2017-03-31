package com.cloudcraftgaming.discal.internal.calendar.event;

import com.cloudcraftgaming.discal.database.DatabaseManager;
import com.cloudcraftgaming.discal.internal.calendar.CalendarAuth;
import com.cloudcraftgaming.discal.internal.email.EmailSender;
import com.google.api.services.calendar.Calendar;

import java.io.IOException;

/**
 * Created by Nova Fox on 1/4/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class EventUtils {
    /**
     * Deletes an event from the calendar.
     * @param guildId The ID of the guild.
     * @param eventId The ID of the event to delete.
     * @return <code>true</code> if successfully deleted, otherwise <code>false</code>.
     */
    public static Boolean deleteEvent(String guildId, String eventId) {
        //TODO: Support multiple calendars...
        String calendarId = DatabaseManager.getManager().getMainCalendar(guildId).getCalendarAddress();
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