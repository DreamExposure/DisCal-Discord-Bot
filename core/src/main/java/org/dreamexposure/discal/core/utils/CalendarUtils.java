package org.dreamexposure.discal.core.utils;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;

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
    //TODO: Make sure this supports multi calendar support
    public static Mono<Boolean> deleteCalendar(final CalendarData data) {
        return CalendarWrapper.deleteCalendar(data).then(
            Mono.when(
                DatabaseManager.deleteCalendar(data),
                DatabaseManager.deleteAllEventData(data.getGuildId()),
                DatabaseManager.deleteAllRSVPData(data.getGuildId()),
                DatabaseManager.deleteAllAnnouncementData(data.getGuildId())
            )).thenReturn(true)
            .doOnError(e -> LogFeed.log(LogObject.forException("Failed to delete calendar", e, CalendarUtils.class)))
            .onErrorReturn(false);
    }

    //TODO: Make sure this supports multi calendar support!!
    public static Mono<Boolean> calendarExists(final CalendarData data) {
        return CalendarWrapper.getCalendar(data)
            .hasElement()
            .onErrorResume(GoogleJsonResponseException.class, ge -> {
                if (ge.getStatusCode() == GlobalConst.STATUS_GONE || ge.getStatusCode() == GlobalConst.STATUS_NOT_FOUND) {
                    //Calendar does not exist... remove from db...
                    return Mono.when(
                        DatabaseManager.deleteCalendar(data),
                        DatabaseManager.deleteAllEventData(data.getGuildId()),
                        DatabaseManager.deleteAllRSVPData(data.getGuildId()),
                        DatabaseManager.deleteAllAnnouncementData(data.getGuildId())
                    ).thenReturn(false);
                } else {
                    LogFeed.log(LogObject
                        .forException("Unknown google error when checking for calendar exist", ge,
                            CalendarUtils.class));
                    return Mono.just(false);
                }
            });
    }
}
