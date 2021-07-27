package org.dreamexposure.discal.core.utils;

import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.logger.LogFeed;
import org.dreamexposure.discal.core.logger.object.LogObject;
import org.dreamexposure.discal.core.object.calendar.CalendarData;
import org.dreamexposure.discal.core.wrapper.google.CalendarWrapper;
import reactor.core.publisher.Mono;

/**
 * Created by Nova Fox on 11/10/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
public class CalendarUtils {
    @Deprecated
    public static Mono<Boolean> deleteCalendar(final CalendarData data) {
        return CalendarWrapper.INSTANCE.deleteCalendar(data).then(
            Mono.when(
                DatabaseManager.INSTANCE.deleteCalendar(data),
                DatabaseManager.INSTANCE.deleteAllEventData(data.getGuildId()),
                DatabaseManager.INSTANCE.deleteAllRsvpData(data.getGuildId()),
                DatabaseManager.INSTANCE.deleteAllAnnouncementData(data.getGuildId())
            )).thenReturn(true)
            .doOnError(e -> LogFeed.log(LogObject.forException("Failed to delete calendar", e, CalendarUtils.class)))
            .onErrorReturn(false);
    }
}
