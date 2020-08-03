package org.dreamexposure.discal.core.utils;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;

import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.logger.LogFeed;
import org.dreamexposure.discal.core.logger.object.LogObject;
import org.dreamexposure.discal.core.object.GuildSettings;
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
    public static Mono<Boolean> deleteCalendar(final CalendarData data, final GuildSettings settings) {
        return CalendarWrapper.deleteCalendar(data, settings)
            .then(Mono.just(settings)
                .doOnNext(s -> s.setUseExternalCalendar(false))
                .doOnNext(s -> s.setEncryptedAccessToken("N/a"))
                .doOnNext(s -> s.setEncryptedRefreshToken("N/a"))
                .flatMap(s -> Mono.when(
                    DatabaseManager.updateSettings(s),
                    DatabaseManager.deleteCalendar(data),
                    DatabaseManager.deleteAllEventData(s.getGuildID()),
                    DatabaseManager.deleteAllRSVPData(s.getGuildID()),
                    DatabaseManager.deleteAllAnnouncementData(settings.getGuildID())
                ))
            ).thenReturn(true)
            .doOnError(e -> LogFeed.log(LogObject.forException("Failed to delete calendar", e, CalendarUtils.class)))
            .onErrorReturn(false);
    }

    //TODO: Make sure this supports multi calendar support!!
    public static Mono<Boolean> calendarExists(final CalendarData data, final GuildSettings settings) {
        return CalendarWrapper.getCalendar(data, settings)
            .hasElement()
            .onErrorResume(GoogleJsonResponseException.class, ge -> {
                if (ge.getStatusCode() == GlobalConst.STATUS_GONE || ge.getStatusCode() == GlobalConst.STATUS_NOT_FOUND) {
                    //Calendar does not exist... remove from db...
                    settings.setUseExternalCalendar(false);
                    settings.setEncryptedRefreshToken("N/a");
                    settings.setEncryptedAccessToken("N/a");

                    return Mono.when(
                        DatabaseManager.updateSettings(settings),
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