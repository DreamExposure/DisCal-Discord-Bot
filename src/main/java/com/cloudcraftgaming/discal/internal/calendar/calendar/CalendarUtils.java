package com.cloudcraftgaming.discal.internal.calendar.calendar;

import com.cloudcraftgaming.discal.database.DatabaseManager;
import com.cloudcraftgaming.discal.internal.calendar.CalendarAuth;
import com.cloudcraftgaming.discal.internal.data.CalendarData;
import com.cloudcraftgaming.discal.utils.ExceptionHandler;
import com.google.api.services.calendar.Calendar;

import java.io.IOException;

/**
 * Created by Nova Fox on 1/4/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class CalendarUtils {
    /**
     * Deletes a calendar from Google Calendar and the Db
     * @param data The BotData of the Guild whose deleting their calendar.
     * @return <code>true</code> if successful, else <code>false</code>.
     */
    public static Boolean deleteCalendar(CalendarData data) {
        try {
            Calendar service = CalendarAuth.getCalendarService();
            service.calendars().delete(data.getCalendarAddress()).execute();

            //Remove from database
            data.setCalendarId("primary");
            data.setCalendarAddress("primary");

            //TODO: Remove??
            DatabaseManager.getManager().updateCalendar(data);

            return true;
        } catch (IOException e) {
            //Fail silently.
            ExceptionHandler.sendException(null, "Failed to delete calendar", e, CalendarUtils.class);
        }
        return false;
    }
}