package org.dreamexposure.discal.core.wrapper.google;

import com.google.api.services.calendar.model.Calendar;
import com.google.api.services.calendar.model.CalendarListEntry;
import discord4j.common.util.Snowflake;
import org.dreamexposure.discal.core.calendar.CalendarAuth;
import org.dreamexposure.discal.core.logger.LogFeed;
import org.dreamexposure.discal.core.logger.object.LogObject;
import org.dreamexposure.discal.core.object.calendar.CalendarData;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.util.List;

public class CalendarWrapper {
    public static Mono<Calendar> createCalendar(Calendar calendar, int credId, Snowflake guildId) {
        return CalendarAuth.getCalendarService(credId)
            .flatMap(service -> Mono.fromCallable(() ->
                service.calendars()
                    .insert(calendar)
                    .setQuotaUser(guildId.asString())
                    .execute()
            ).subscribeOn(Schedulers.boundedElastic()))
            .onErrorResume(e -> Mono.empty());
    }

    public static Mono<Calendar> updateCalendar(Calendar calendar, CalendarData calData) {
        return CalendarAuth.getCalendarService(calData)
            .flatMap(service -> Mono.fromCallable(() ->
                service.calendars()
                    .update(calendar.getId(), calendar)
                    .setQuotaUser(calData.getGuildId().asString())
                    .execute()
            ).subscribeOn(Schedulers.boundedElastic()))
            .onErrorResume(e -> Mono.empty());
    }

    public static Mono<Calendar> getCalendar(CalendarData data) {
        return CalendarAuth.getCalendarService(data)
            .flatMap(service -> Mono.fromCallable(() ->
                service.calendars()
                    .get(data.getCalendarAddress())
                    .setQuotaUser(data.getGuildId().asString())
                    .execute()
            ).subscribeOn(Schedulers.boundedElastic()))
            .doOnError(e -> LogFeed.log(LogObject.forException("Get Cal Failure", e, CalendarWrapper.class)))
            .onErrorResume(e -> Mono.empty());
    }

    public static Mono<Void> deleteCalendar(CalendarData data) {
        return Mono.just(data)
            .filter(cd -> !cd.getExternal())
            .filter(cd -> !"primary".equalsIgnoreCase(cd.getCalendarAddress()))
            .flatMap(cd ->
                CalendarAuth.getCalendarService(data).flatMap(service ->
                    Mono.fromCallable(() -> service.calendars()
                        .delete(cd.getCalendarAddress())
                        .setQuotaUser(data.getGuildId().asString())
                        .execute()
                    ).subscribeOn(Schedulers.boundedElastic())
                )
            )
            .onErrorResume(e -> Mono.empty())
            .then();
    }

    public static Mono<List<CalendarListEntry>> getUsersExternalCalendars(CalendarData calData) {
        return CalendarAuth.getExternalCalendarService(calData)
            .flatMap(service -> Mono.fromCallable(() ->
                service.calendarList()
                    .list()
                    .setMinAccessRole("writer")
                    .setQuotaUser(calData.getGuildId().asString())
                    .execute()
                    .getItems()
            ).subscribeOn(Schedulers.boundedElastic()))
            .doOnError(IOException.class, e ->
                LogFeed.log(LogObject.forException("Failed to list cals from ext. account", e,
                    CalendarWrapper.class))
            )
            .onErrorResume(e -> Mono.empty());
    }
}
