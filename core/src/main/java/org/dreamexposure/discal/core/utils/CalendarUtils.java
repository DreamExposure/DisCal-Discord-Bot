package org.dreamexposure.discal.core.utils;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.calendar.Calendar;

import org.dreamexposure.discal.core.calendar.CalendarAuth;
import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.logger.LogFeed;
import org.dreamexposure.discal.core.logger.object.LogObject;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.calendar.CalendarData;

import reactor.core.publisher.Mono;

/**
 * Created by Nova Fox on 11/10/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
@SuppressWarnings("Duplicates")
public class CalendarUtils {
    /**
     * Deletes a calendar from Google Calendar and the Db
     *
     * @param data The BotData of the Guild whose deleting their calendar.
     * @return <code>true</code> if successful, else <code>false</code>.
     */
    public static boolean deleteCalendar(CalendarData data, GuildSettings settings) {
        try {
            //Only delete if the calendar is stored on DisCal's account.
            if (!data.getCalendarAddress().equalsIgnoreCase("primary") && !settings.useExternalCalendar()) {
                Calendar service = CalendarAuth.getCalendarService(settings);
                service.calendars().delete(data.getCalendarAddress()).execute();
            }
        } catch (Exception e) {
            //Fail silently.
            LogFeed.log(LogObject
                    .forException("Failed to delete calendar", e, CalendarUtils.class));
            return false;
        }
        if (settings.useExternalCalendar()) {
            //Update settings.
            settings.setUseExternalCalendar(false);
            settings.setEncryptedAccessToken("N/a");
            settings.setEncryptedRefreshToken("N/a");
            DatabaseManager.updateSettings(settings).subscribe();
        }

        //Delete everything that is specific to the calendar...
        Mono.when(
                DatabaseManager.deleteCalendar(data),
                DatabaseManager.deleteAllEventData(data.getGuildId()),
                DatabaseManager.deleteAllRSVPData(data.getGuildId()),
                DatabaseManager.deleteAllAnnouncementData(data.getGuildId())
        ).subscribe();

        return true;
    }

    //TODO: Make sure this supports multi calendar support!!
    public static boolean calendarExists(CalendarData data, GuildSettings settings) {
        try {
            return CalendarAuth.getCalendarService(settings).calendars().get(data.getCalendarAddress()).execute() != null;
        } catch (GoogleJsonResponseException ge) {
            if (ge.getStatusCode() == 410 || ge.getStatusCode() == 404) {
                //Calendar does not exist... remove from db...
                settings.setUseExternalCalendar(false);
                settings.setEncryptedRefreshToken("N/a");
                settings.setEncryptedAccessToken("N/a");

                Mono.when(
                        DatabaseManager.updateSettings(settings),
                        DatabaseManager.deleteCalendar(data),
                        DatabaseManager.deleteAllEventData(data.getGuildId()),
                        DatabaseManager.deleteAllRSVPData(data.getGuildId()),
                        DatabaseManager.deleteAllAnnouncementData(data.getGuildId())
                ).subscribe();

                return false;
            } else {
                LogFeed.log(LogObject
                        .forException("Unknown google error when checking for calendar exist", ge,
                                CalendarUtils.class));
                return true;
            }
        } catch (Exception e) {
            LogFeed.log(LogObject.forException("Unknown error when checking for calendar exist", e,
                    CalendarUtils.class));
            return true;
        }
    }
}