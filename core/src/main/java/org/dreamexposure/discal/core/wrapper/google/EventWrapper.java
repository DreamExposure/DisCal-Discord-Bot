package org.dreamexposure.discal.core.wrapper.google;

import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpStatusCodes;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import org.dreamexposure.discal.core.calendar.CalendarAuth;
import org.dreamexposure.discal.core.logger.LogFeed;
import org.dreamexposure.discal.core.logger.object.LogObject;
import org.dreamexposure.discal.core.object.calendar.CalendarData;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

public class EventWrapper {
    public static Mono<Event> createEvent(CalendarData data, Event event) {
        return CalendarAuth.getCalendarService(data).flatMap(service ->
            Mono.fromCallable(() ->
                service.events()
                    .insert(data.getCalendarId(), event)
                    .setQuotaUser(data.getGuildId().asString())
                    .execute()
            ).subscribeOn(Schedulers.boundedElastic())
        ).doOnError(e -> LogFeed.log(LogObject
            .forException("Event create error; Cred Id: " + data.getCredentialId(), e, EventWrapper.class))
        ).onErrorResume(e -> Mono.empty());
    }

    public static Mono<Event> patchEvent(CalendarData data, Event event) {
        return CalendarAuth.getCalendarService(data).flatMap(service ->
            Mono.fromCallable(() ->
                service.events()
                    .patch(data.getCalendarId(), event.getId(), event)
                    .setQuotaUser(data.getGuildId().asString())
                    .execute()
            ).subscribeOn(Schedulers.boundedElastic())
        ).doOnError(e -> LogFeed.log(LogObject.forException("Failed to patch event", e, EventWrapper.class))
        ).onErrorResume(e -> Mono.empty());
    }

    public static Mono<Event> updateEvent(CalendarData data, Event event) {
        return CalendarAuth.getCalendarService(data).flatMap(service ->
            Mono.fromCallable(() ->
                service.events()
                    .update(data.getCalendarId(), event.getId(), event)
                    .setQuotaUser(data.getGuildId().asString())
                    .execute()
            ).subscribeOn(Schedulers.boundedElastic())
        ).doOnError(e -> LogFeed.log(LogObject.forException("Failed to edit event", e, EventWrapper.class))
        ).onErrorResume(e -> Mono.empty());
    }

    public static Mono<Event> getEvent(CalendarData data, String id) {
        return CalendarAuth.getCalendarService(data).flatMap(service ->
            Mono.fromCallable(() ->
                service.events()
                    .get(data.getCalendarAddress(), id)
                    .setQuotaUser(data.getGuildId().asString())
                    .execute()
            ).subscribeOn(Schedulers.boundedElastic())
        ).onErrorResume(e -> Mono.empty()); //Can ignore this, the event just doesn't exist.
    }

    public static Mono<List<Event>> getEvents(CalendarData data, int amount, long start) {
        return CalendarAuth.getCalendarService(data).flatMap(service ->
            Mono.fromCallable(() ->
                service.events().list(data.getCalendarId())
                    .setMaxResults(amount)
                    .setTimeMin(new DateTime(start))
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .setShowDeleted(false)
                    .setQuotaUser(data.getGuildId().asString())
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
                .setQuotaUser(data.getGuildId().asString())
                .execute().getItems()
        ).subscribeOn(Schedulers.boundedElastic())
            .onErrorResume(e -> Mono.empty());
    }

    public static Mono<List<Event>> getEvents(CalendarData data, int amount, long start, long end) {
        return CalendarAuth.getCalendarService(data).flatMap(service ->
            Mono.fromCallable(() ->
                service.events().list(data.getCalendarId())
                    .setMaxResults(amount)
                    .setTimeMin(new DateTime(start))
                    .setTimeMax(new DateTime(end))
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .setShowDeleted(false)
                    .setQuotaUser(data.getGuildId().asString())
                    .execute().getItems()
            ).subscribeOn(Schedulers.boundedElastic())
        ).onErrorResume(e -> Mono.empty());
    }

    public static Mono<List<Event>> getEvents(CalendarData data, long start, long end) {
        return CalendarAuth.getCalendarService(data).flatMap(service ->
            Mono.fromCallable(() ->
                service.events().list(data.getCalendarId())
                    .setTimeMin(new DateTime(start))
                    .setTimeMax(new DateTime(end))
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .setShowDeleted(false)
                    .setQuotaUser(data.getGuildId().asString())
                    .execute().getItems()
            ).subscribeOn(Schedulers.boundedElastic())
        ).onErrorResume(e -> Mono.empty());
    }

    public static Mono<Boolean> deleteEvent(CalendarData data, String id) {
        return CalendarAuth.getCalendarService(data).flatMap(service ->
            Mono.fromCallable(() -> {
                HttpResponse response = service.events()
                    .delete(data.getCalendarAddress(), id)
                    .setQuotaUser(data.getGuildId().asString())
                    .executeUnparsed();

                //Log error code if one happened
                if (response.getStatusCode() != HttpStatusCodes.STATUS_CODE_OK) {
                    LogFeed.log(LogObject.forDebug(
                            "Event Delete Error | " + response.getStatusCode() + " | " + response.getStatusMessage()
                        ));
                    }

                    return response.getStatusCode() == HttpStatusCodes.STATUS_CODE_OK;
                }
            ).subscribeOn(Schedulers.boundedElastic())
        ).doOnError(e -> LogFeed.log(LogObject.forException("Event Delete Failure", e, EventWrapper.class))
        ).onErrorReturn(false);
    }
}
