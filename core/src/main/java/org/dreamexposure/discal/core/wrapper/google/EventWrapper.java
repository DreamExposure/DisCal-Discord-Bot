package org.dreamexposure.discal.core.wrapper.google;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;

import org.dreamexposure.discal.core.calendar.CalendarAuth;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.calendar.CalendarData;

import java.util.List;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class EventWrapper {
    public static Mono<Event> createEvent(CalendarData data, Event event, GuildSettings settings) {
        return CalendarAuth.getCalendarService(settings).flatMap(service ->
            Mono.fromCallable(() ->
                service.events()
                    .insert(data.getCalendarId(), event)
                    .execute()
            ).subscribeOn(Schedulers.boundedElastic())
        ).onErrorResume(e -> Mono.empty());
    }

    public static Mono<Event> updateEvent(CalendarData data, Event event, GuildSettings settings) {
        return CalendarAuth.getCalendarService(settings).flatMap(service ->
            Mono.fromCallable(() ->
                service.events()
                    .update(data.getCalendarId(), event.getId(), event)
                    .execute()
            ).subscribeOn(Schedulers.boundedElastic())
        ).onErrorResume(e -> Mono.empty());
    }

    public static Mono<Event> getEvent(CalendarData data, GuildSettings settings, String id) {
        return CalendarAuth.getCalendarService(settings).flatMap(service ->
            Mono.fromCallable(() ->
                service.events()
                    .get(data.getCalendarAddress(), id)
                    .execute()
            ).subscribeOn(Schedulers.boundedElastic())
        ).onErrorResume(e -> Mono.empty()); //Can ignore this, the event just doesn't exist.
    }

    public static Mono<List<Event>> getEvents(CalendarData data, GuildSettings settings, int amount, long start) {
        return CalendarAuth.getCalendarService(settings).flatMap(service ->
            Mono.fromCallable(() ->
                service.events().list(data.getCalendarId())
                    .setMaxResults(amount)
                    .setTimeMin(new DateTime(start))
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .setShowDeleted(false)
                    .execute().getItems()
            ).subscribeOn(Schedulers.boundedElastic())
        ).onErrorResume(e -> Mono.empty());
    }

    public static Mono<List<Event>> getEvents(CalendarData data, Calendar service, int amount, long start) {
        return Mono.fromCallable(() ->
            service.events().list(data.getCalendarId())
                .setMaxResults(amount)
                .setTimeMin(new DateTime(start))
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .setShowDeleted(false)
                .execute().getItems()
        ).subscribeOn(Schedulers.boundedElastic())
            .onErrorResume(e -> Mono.empty());
    }

    public static Mono<List<Event>> getEvents(CalendarData data, GuildSettings settings, int amount, long start,
                                              long end) {
        return CalendarAuth.getCalendarService(settings).flatMap(service ->
            Mono.fromCallable(() ->
                service.events().list(data.getCalendarId())
                    .setMaxResults(amount)
                    .setTimeMin(new DateTime(start))
                    .setTimeMax(new DateTime(end))
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .setShowDeleted(false)
                    .execute().getItems()
            ).subscribeOn(Schedulers.boundedElastic())
        ).onErrorResume(e -> Mono.empty());
    }

    public static Mono<Void> deleteEvent(CalendarData data, GuildSettings settings, String id) {
        return CalendarAuth.getCalendarService(settings).flatMap(service ->
            Mono.fromCallable(() ->
                service.events()
                    .delete(data.getCalendarAddress(), id)
                    .execute()
            ).subscribeOn(Schedulers.boundedElastic())
        ).onErrorResume(e -> Mono.empty());
    }
}
