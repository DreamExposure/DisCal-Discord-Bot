package org.dreamexposure.discal.core.utils;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;

import org.dreamexposure.discal.core.calendar.CalendarAuth;
import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.enums.event.EventColor;
import org.dreamexposure.discal.core.logger.LogFeed;
import org.dreamexposure.discal.core.logger.object.LogObject;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.calendar.CalendarData;
import org.dreamexposure.discal.core.object.event.PreEvent;

import discord4j.rest.util.Snowflake;
import reactor.core.publisher.Mono;

/**
 * Created by Nova Fox on 11/10/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
public class EventUtils {
    /**
     * Deletes an event from the calendar.
     *
     * @param settings Guild settings
     * @param eventId  The ID of the event to delete.
     * @return <code>true</code> if successfully deleted, otherwise <code>false</code>.
     */
    public static Boolean deleteEvent(GuildSettings settings, String eventId) {
        //TODO: Support multiple calendars...
        CalendarData data = DatabaseManager.getMainCalendar(settings.getGuildID()).block();
        if (data == null)
            return false;

        try {
            Calendar service = CalendarAuth.getCalendarService(settings);
            try {
                service.events().delete(data.getCalendarAddress(), eventId).execute();
            } catch (Exception e) {
                //Failed to delete event...
                return false;
            }

            Mono.zip(
                    DatabaseManager.deleteAnnouncementsForEvent(settings.getGuildID(), eventId),
                    DatabaseManager.deleteEventData(eventId)
            ).subscribe();

            return true;
        } catch (Exception e) {
            LogFeed.log(LogObject.forException("Failed to delete event", e, EventUtils.class));
            e.printStackTrace();
        }
        return false;
    }

    @Deprecated
    public static boolean eventExists(GuildSettings settings, String eventId) {
        CalendarData data = DatabaseManager.getMainCalendar(settings.getGuildID()).block();
        if (data == null)
            return false;
        try {
            Calendar service = CalendarAuth.getCalendarService(settings);

            return service.events().get(data.getCalendarAddress(), eventId).execute() != null;
        } catch (Exception e) {
            //Failed to check event, probably doesn't exist, safely ignore.
        }
        return false;
    }

    public static boolean eventExists(GuildSettings settings, int calNumber, String eventId) {
        CalendarData data = DatabaseManager.getCalendar(settings.getGuildID(), calNumber).block();
        if (data == null)
            return false;
        try {
            Calendar service = CalendarAuth.getCalendarService(settings);

            return service.events().get(data.getCalendarAddress(), eventId).execute() != null;
        } catch (Exception e) {
            //Failed to check event, probably doesn't exist, safely ignore.
        }
        return false;
    }

    public static PreEvent copyEvent(Snowflake guildId, Event event) {
        PreEvent pe = new PreEvent(guildId);
        pe.setSummary(event.getSummary());
        pe.setDescription(event.getDescription());
        pe.setLocation(event.getLocation());
        if (event.getColorId() != null)
            pe.setColor(EventColor.fromNameOrHexOrID(event.getColorId()));
        else
            pe.setColor(EventColor.RED);

        pe.setEventData(DatabaseManager.getEventData(guildId, event.getId()).block());

        return pe;
    }
}