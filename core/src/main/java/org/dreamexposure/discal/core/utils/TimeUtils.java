package org.dreamexposure.discal.core.utils;

import com.google.api.client.util.DateTime;
import org.dreamexposure.discal.core.object.event.PreEvent;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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
@Deprecated
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
}
