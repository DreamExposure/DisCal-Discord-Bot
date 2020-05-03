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
    public static Mono<Calendar> createCalendar(Calendar calendar, GuildSettings settings) {
        return CalendarAuth.getCalendarService(settings)
            .flatMap(service -> Mono.fromCallable(() ->
                service.calendars()
                    .insert(calendar)
                    .execute()
            ).subscribeOn(Schedulers.boundedElastic()))
            .onErrorResume(e -> Mono.empty());
    }

    public static Mono<Calendar> updateCalendar(Calendar calendar, GuildSettings settings) {
        return CalendarAuth.getCalendarService(settings)
            .flatMap(service -> Mono.fromCallable(() ->
                service.calendars()
                    .update(calendar.getId(), calendar)
                    .execute()
            ).subscribeOn(Schedulers.boundedElastic()))
            .onErrorResume(e -> Mono.empty());
    }

    public static Mono<Calendar> getCalendar(CalendarData data, GuildSettings settings) {
        return CalendarAuth.getCalendarService(settings)
            .flatMap(service -> Mono.fromCallable(() ->
                service.calendars()
                    .get(data.getCalendarAddress())
                    .execute()
            ).subscribeOn(Schedulers.boundedElastic()))
            .onErrorResume(e -> Mono.empty());
    }

    public static Mono<Void> deleteCalendar(CalendarData data, GuildSettings settings) {
        return Mono.just(data)
            .filter(cd -> !cd.isExternal())
            .filter(cd -> !cd.getCalendarAddress().equalsIgnoreCase("primary"))
            .flatMap(cd ->
                CalendarAuth.getCalendarService(settings).flatMap(service ->
                    Mono.fromCallable(() -> service.calendars()
                        .delete(cd.getCalendarAddress())
                        .execute()
                    ).subscribeOn(Schedulers.boundedElastic())
                )
            )
            .onErrorResume(e -> Mono.empty())
            .then();
    }

    public static Mono<List<CalendarListEntry>> getUsersExternalCalendars(GuildSettings settings) {
        return CalendarAuth.getCalendarService(settings)
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
