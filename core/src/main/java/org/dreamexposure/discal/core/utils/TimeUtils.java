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
    public static boolean isInPast(final String dateRaw, final TimeZone timezone) {
        try {
            final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss");
            sdf.setTimeZone(timezone);
            final Date dateObj = sdf.parse(dateRaw);
            final Date now = new Date(System.currentTimeMillis());

            return dateObj.before(now);
        } catch (final ParseException e) {
            return true;
        }
    }

    private static boolean isInPast(final Event event) {
        if (event.getStart().getDateTime() != null)
            return event.getStart().getDateTime().getValue() <= System.currentTimeMillis();
        else
            return event.getStart().getDate().getValue() <= System.currentTimeMillis();
    }

    @Deprecated
    public static Mono<Boolean> isInPast(final String eventId, final GuildSettings settings) {
        return DatabaseManager.getMainCalendar(settings.getGuildID()).flatMap(data ->
            EventWrapper.getEvent(data, settings, eventId)
                .map(TimeUtils::isInPast)
        );
    }

    public static Mono<Boolean> isInPast(final String eventId, final int calNumber, final GuildSettings settings) {
        return DatabaseManager.getCalendar(settings.getGuildID(), calNumber).flatMap(data ->
            EventWrapper.getEvent(data, settings, eventId)
                .map(TimeUtils::isInPast)
        );
    }

    public static Mono<Boolean> isInPast(final String eventId, final CalendarData data, final GuildSettings settings) {
        return EventWrapper.getEvent(data, settings, eventId).map(TimeUtils::isInPast);
    }


    public static boolean hasEndBeforeStart(final String endRaw, final TimeZone timezone, final PreEvent event) {
        if (event.getStartDateTime() != null) {
            try {
                final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss");
                sdf.setTimeZone(timezone);
                final Date endDate = sdf.parse(endRaw);
                final Date startDate = new Date(event.getStartDateTime().getDateTime().getValue());

                return endDate.before(startDate);
            } catch (final ParseException e) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasStartAfterEnd(final String startRaw, final TimeZone timezone, final PreEvent event) {
        if (event.getEndDateTime() != null) {
            try {
                final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss");
                sdf.setTimeZone(timezone);
                final Date startDate = sdf.parse(startRaw);
                final Date endDate = new Date(event.getEndDateTime().getDateTime().getValue());

                return startDate.after(endDate);
            } catch (final ParseException e) {
                return true;
            }
        }
        return false;
    }

    public static long applyTimeZoneOffset(final long epochTime, final String timezone) {
        final long timeZoneOffset = TimeZone.getTimeZone(ZoneId.of(timezone)).getRawOffset();
        final long chicagoOffset = TimeZone.getTimeZone(ZoneId.of("UTC")).getRawOffset();

        final long toAdd = timeZoneOffset - chicagoOffset;

        return epochTime + toAdd;
    }
}