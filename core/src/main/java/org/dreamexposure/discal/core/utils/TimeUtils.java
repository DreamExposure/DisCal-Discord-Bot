package org.dreamexposure.discal.core.utils;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.calendar.CalendarData;
import org.dreamexposure.discal.core.object.event.PreEvent;
import org.dreamexposure.discal.core.wrapper.google.EventWrapper;
import reactor.core.publisher.Mono;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.TimeZone;

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
        return DatabaseManager.INSTANCE.getMainCalendar(settings.getGuildID()).flatMap(data ->
            EventWrapper.getEvent(data, eventId)
                .map(TimeUtils::isInPast)
        );
    }

    public static Mono<Boolean> isInPast(final String eventId, final int calNumber, final GuildSettings settings) {
        return DatabaseManager.INSTANCE.getCalendar(settings.getGuildID(), calNumber).flatMap(data ->
            EventWrapper.getEvent(data, eventId)
                .map(TimeUtils::isInPast)
        );
    }

    public static Mono<Boolean> isInPast(final String eventId, final CalendarData data) {
        return EventWrapper.getEvent(data, eventId).map(TimeUtils::isInPast);
    }


    public static boolean hasEndBeforeStart(final String endRaw, final TimeZone timezone, final PreEvent event) {
        if (event.getStartDateTime() != null) {
            try {
                final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss");
                sdf.setTimeZone(timezone);
                final Date endDate = sdf.parse(endRaw);

                final Date startDate;
                if (event.getStartDateTime().getDateTime() != null)
                    startDate = new Date(event.getStartDateTime().getDateTime().getValue());
                else
                    startDate = new Date(event.getStartDateTime().getDate().getValue());

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

                Date endDate;
                if (event.getEndDateTime().getDateTime() != null)
                    endDate = new Date(event.getEndDateTime().getDateTime().getValue());
                else
                    endDate = new Date(event.getEndDateTime().getDate().getValue());


                return startDate.after(endDate);
            } catch (final ParseException e) {
                return true;
            }
        }
        return false;
    }

    public static DateTime doTimeShiftBullshit(DateTime original, ZoneId tz) {
        return new DateTime(Instant.ofEpochMilli(original.getValue())
            .plus(1, ChronoUnit.DAYS)
            .atZone(tz)
            .truncatedTo(ChronoUnit.DAYS)
            .toLocalDate()
            .atStartOfDay()
            .atZone(tz)
            .toInstant()
            .toEpochMilli()
        );
    }

    public static Instant convertToInstant(EventDateTime edt, ZoneId tz) {
        if (edt.getDateTime() != null) {
            return Instant.ofEpochMilli(edt.getDateTime().getValue());
        } else {
            return Instant.ofEpochMilli(edt.getDate().getValue())
                .plus(1, ChronoUnit.DAYS)
                .atZone(tz)
                .truncatedTo(ChronoUnit.DAYS)
                .toLocalDate()
                .atStartOfDay()
                .atZone(tz)
                .toInstant();
        }
    }

    public static String getHumanReadableUptime() {
        final RuntimeMXBean mxBean = ManagementFactory.getRuntimeMXBean();

        long rawDuration = System.currentTimeMillis() - mxBean.getStartTime();
        Duration duration = Duration.ofMillis(rawDuration);

        return String.format("%d days, %d hours, %d minutes, %d seconds%n",
            duration.toDays(), duration.toHoursPart(), duration.toMinutesPart(), duration.toSecondsPart());
    }
}
