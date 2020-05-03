package org.dreamexposure.discal.core.utils;

import com.google.api.services.calendar.model.Event;

import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.calendar.CalendarData;
import org.dreamexposure.discal.core.object.event.PreEvent;
import org.dreamexposure.discal.core.wrapper.google.EventWrapper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Date;
import java.util.TimeZone;

import reactor.core.publisher.Mono;

/**
 * Created by Nova Fox on 11/10/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
public class TimeUtils {
    public static boolean inPast(String dateRaw, TimeZone timezone) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss");
            sdf.setTimeZone(timezone);
            Date dateObj = sdf.parse(dateRaw);
            Date now = new Date(System.currentTimeMillis());

            return dateObj.before(now);
        } catch (ParseException e) {
            return true;
        }
    }

    private static boolean inPast(Event event) {
        if (event.getStart().getDateTime() != null)
            return event.getStart().getDateTime().getValue() <= System.currentTimeMillis();
        else
            return event.getStart().getDate().getValue() <= System.currentTimeMillis();
    }

    @Deprecated
    public static Mono<Boolean> inPast(String eventId, GuildSettings settings) {
        return DatabaseManager.getMainCalendar(settings.getGuildID()).flatMap(data ->
            EventWrapper.getEvent(data, settings, eventId)
                .map(TimeUtils::inPast)
        );
    }

    public static Mono<Boolean> inPast(String eventId, int calNumber, GuildSettings settings) {
        return DatabaseManager.getCalendar(settings.getGuildID(), calNumber).flatMap(data ->
            EventWrapper.getEvent(data, settings, eventId)
                .map(TimeUtils::inPast)
        );
    }

    public static Mono<Boolean> inPast(String eventId, CalendarData data, GuildSettings settings) {
        return EventWrapper.getEvent(data, settings, eventId).map(TimeUtils::inPast);
    }


    public static boolean endBeforeStart(String endRaw, TimeZone timezone, PreEvent event) {
        if (event.getStartDateTime() != null) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss");
                sdf.setTimeZone(timezone);
                Date endDate = sdf.parse(endRaw);
                Date startDate = new Date(event.getStartDateTime().getDateTime().getValue());

                return endDate.before(startDate);
            } catch (ParseException e) {
                return true;
            }
        }
        return false;
    }

    public static boolean startAfterEnd(String startRaw, TimeZone timezone, PreEvent event) {
        if (event.getEndDateTime() != null) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss");
                sdf.setTimeZone(timezone);
                Date startDate = sdf.parse(startRaw);
                Date endDate = new Date(event.getEndDateTime().getDateTime().getValue());

                return startDate.after(endDate);
            } catch (ParseException e) {
                return true;
            }
        }
        return false;
    }

    public static long applyTimeZoneOffset(long epochTime, String timezone) {
        long timeZoneOffset = TimeZone.getTimeZone(ZoneId.of(timezone)).getRawOffset();
        long chicagoOffset = TimeZone.getTimeZone(ZoneId.of("UTC")).getRawOffset();

        long toAdd = timeZoneOffset - chicagoOffset;

        return epochTime + toAdd;
    }
}