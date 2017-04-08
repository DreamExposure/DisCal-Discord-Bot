package com.cloudcraftgaming.discal.internal.calendar.event;

import com.cloudcraftgaming.discal.database.DatabaseManager;
import com.cloudcraftgaming.discal.internal.calendar.CalendarAuth;
import com.cloudcraftgaming.discal.internal.email.EmailSender;
import com.cloudcraftgaming.discal.utils.EventColor;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;

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
            try {
                service.events().delete(calendarId, eventId).execute();
            } catch (Exception e) {
                //Failed to delete event...
                return false;
            }
            DatabaseManager.getManager().deleteAnnouncementsForEvent(guildId, eventId);
            return true;
        } catch (IOException e) {
            System.out.println("Something weird happened when deleting an event!");
            EmailSender.getSender().sendExceptionEmail(e, EventUtils.class);
            e.printStackTrace();
        }
        return false;
    }

    public static boolean eventExists(String guildId, String eventId) {
        //TODO: Support multiple calendars...
        String calendarId = DatabaseManager.getManager().getMainCalendar(guildId).getCalendarAddress();
        try {
            Calendar service = CalendarAuth.getCalendarService();
            return service.events().get(calendarId, eventId).execute() != null;
        } catch (IOException e) {
            //Failed to check event, probably doesn't exist, safely ignore.
        }
        return false;
    }

    static PreEvent copyEvent(String guildId, Event event) {
        PreEvent pe = new PreEvent(guildId);
        pe.setSummary(event.getSummary());
        pe.setDescription(event.getDescription());
        if (event.getColorId() != null) {
            pe.setColor(EventColor.fromNameOrHexOrID(event.getColorId()));
        } else {
            pe.setColor(EventColor.RED);
        }

        return pe;
    }
}