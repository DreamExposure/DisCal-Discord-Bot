package com.cloudcraftgaming.internal.calendar.calendar;

import com.cloudcraftgaming.database.DatabaseManager;
import com.cloudcraftgaming.internal.calendar.CalendarAuth;
import com.cloudcraftgaming.internal.data.BotData;
import com.cloudcraftgaming.internal.email.EmailSender;
import com.google.api.services.calendar.Calendar;

import java.io.IOException;

/**
 * Created by Nova Fox on 1/4/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class CalendarUtils {
    public static Boolean deleteCalendar(BotData data) {
        try {
            Calendar service = CalendarAuth.getCalendarService();
            service.calendars().delete(data.getCalendarAddress()).execute();

            //Remove from database
            data.setCalendarId("primary");
            data.setCalendarAddress("primary");
            DatabaseManager.getManager().updateData(data);
            
            return true;
        } catch (IOException e) {
            //Fail silently.
            EmailSender.getSender().sendExceptionEmail(e);
        }
        return false;
    }
}