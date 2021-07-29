package org.dreamexposure.discal.core.utils;

import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.object.calendar.CalendarData;
import org.dreamexposure.discal.core.wrapper.google.CalendarWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

/**
 * Created by Nova Fox on 11/10/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
public class CalendarUtils {
    private final static Logger LOGGER = LoggerFactory.getLogger(CalendarUtils.class);

    @Deprecated
    public static Mono<Boolean> deleteCalendar(final CalendarData data) {
        return CalendarWrapper.INSTANCE.deleteCalendar(data).then(
            Mono.when(
                DatabaseManager.INSTANCE.deleteCalendar(data),
                DatabaseManager.INSTANCE.deleteAllEventData(data.getGuildId(), data.getCalendarNumber()),
                DatabaseManager.INSTANCE.deleteAllRsvpData(data.getGuildId(), data.getCalendarNumber()),
                DatabaseManager.INSTANCE.deleteAllAnnouncementData(data.getGuildId(), data.getCalendarNumber())
            )).thenReturn(true)
            .doOnError(e -> LOGGER.error(GlobalVal.getDEFAULT(), "Calendar delete failure", e))
            .onErrorReturn(false);
    }
}
