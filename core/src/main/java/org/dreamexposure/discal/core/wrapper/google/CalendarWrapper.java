package org.dreamexposure.discal.core.wrapper.google;

import com.google.api.services.calendar.model.Calendar;
import com.google.api.services.calendar.model.CalendarListEntry;

import org.dreamexposure.discal.core.calendar.CalendarAuth;
import org.dreamexposure.discal.core.logger.LogFeed;
import org.dreamexposure.discal.core.logger.object.LogObject;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.calendar.CalendarData;

import java.io.IOException;
import java.util.List;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class CalendarWrapper {
    public static Mono<Calendar> createCalendar(final Calendar calendar, final int credId) {
        return CalendarAuth.getCalendarService(credId)
            .flatMap(service -> Mono.fromCallable(() ->
                service.calendars()
                    .insert(calendar)
                    .execute()
            ).subscribeOn(Schedulers.boundedElastic()))
            .onErrorResume(e -> Mono.empty());
    }

    public static Mono<Calendar> updateCalendar(final Calendar calendar, final GuildSettings settings,
                                                final CalendarData calData) {
        return CalendarAuth.getCalendarService(settings, calData)
            .flatMap(service -> Mono.fromCallable(() ->
                service.calendars()
                    .update(calendar.getId(), calendar)
                    .execute()
            ).subscribeOn(Schedulers.boundedElastic()))
            .onErrorResume(e -> Mono.empty());
    }

    public static Mono<Calendar> getCalendar(final CalendarData data, final GuildSettings settings) {
        return CalendarAuth.getCalendarService(settings, data)
            .flatMap(service -> Mono.fromCallable(() ->
                service.calendars()
                    .get(data.getCalendarAddress())
                    .execute()
            ).subscribeOn(Schedulers.boundedElastic()))
            .doOnError(e -> LogFeed.log(LogObject.forException("Get Cal Failure", e, CalendarWrapper.class)))
            .onErrorResume(e -> Mono.empty());
    }

    public static Mono<Void> deleteCalendar(final CalendarData data, final GuildSettings settings) {
        return Mono.just(data)
            .filter(cd -> !cd.getExternal())
            .filter(cd -> !"primary".equalsIgnoreCase(cd.getCalendarAddress()))
            .flatMap(cd ->
                CalendarAuth.getCalendarService(settings, data).flatMap(service ->
                    Mono.fromCallable(() -> service.calendars()
                        .delete(cd.getCalendarAddress())
                        .execute()
                    ).subscribeOn(Schedulers.boundedElastic())
                )
            )
            .onErrorResume(e -> Mono.empty())
            .then();
    }

    public static Mono<List<CalendarListEntry>> getUsersExternalCalendars(final GuildSettings settings) {
        return CalendarAuth.getExternalCalendarService(settings)
            .flatMap(service -> Mono.fromCallable(() ->
                service.calendarList()
                    .list()
                    .setMinAccessRole("writer")
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
