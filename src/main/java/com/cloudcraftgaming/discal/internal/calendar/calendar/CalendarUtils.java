package com.cloudcraftgaming.discal.internal.calendar.calendar;

import com.cloudcraftgaming.discal.database.DatabaseManager;
import com.cloudcraftgaming.discal.internal.calendar.CalendarAuth;
import com.cloudcraftgaming.discal.internal.data.CalendarData;
import com.cloudcraftgaming.discal.internal.data.GuildSettings;
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
    public static Boolean deleteCalendar(CalendarData data, GuildSettings settings, boolean deleteFromDb) {
        try {
        	//Only delete if the calendar is stored on DisCal's account.
        	if (!data.getCalendarAddress().equalsIgnoreCase("primary") && !settings.useExternalCalendar()) {
					Calendar service = CalendarAuth.getCalendarService();
					service.calendars().delete(data.getCalendarAddress()).execute();
			}
		} catch (IOException e) {
			//Fail silently.
			ExceptionHandler.sendException(null, "Failed to delete calendar", e, CalendarUtils.class);
		}

            //Remove from database
            data.setCalendarId("primary");
            data.setCalendarAddress("primary");
            if (settings.useExternalCalendar()) {
            	data.setExternal(false);

            	//Update settings
				settings.setUseExternalCalendar(false);
				settings.setEncryptedAccessToken("N/a");
				settings.setEncryptedRefreshToken("N/a");
				DatabaseManager.getManager().updateSettings(settings);
			}

            DatabaseManager.getManager().updateCalendar(data, deleteFromDb);
            DatabaseManager.getManager().deleteAllEventData(data.getGuildId());

        return true;
    }
}