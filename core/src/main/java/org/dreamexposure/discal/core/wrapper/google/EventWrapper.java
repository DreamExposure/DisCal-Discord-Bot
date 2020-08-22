package org.dreamexposure.discal.core.wrapper.google;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;

import org.dreamexposure.discal.core.calendar.CalendarAuth;
import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.logger.LogFeed;
import org.dreamexposure.discal.core.logger.object.LogObject;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.calendar.CalendarData;

import java.util.List;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@SuppressWarnings("DuplicatedCode")
public class EventWrapper {
    public static Mono<Event> createEvent(final CalendarData data, final Event event, final GuildSettings settings) {
        return CalendarAuth.getCalendarService(settings, data).flatMap(service ->
            Mono.fromCallable(() ->
                service.events()
                    .insert(data.getCalendarId(), event)
                    .execute()
            ).subscribeOn(Schedulers.boundedElastic())
        ).onErrorResume(GoogleJsonResponseException.class, e -> {
            if (e.getStatusCode() == 404 ||
                "requiredAccessLevel".equalsIgnoreCase(e.getDetails().getErrors().get(0).getReason())) {
                //This is caused by credentials issue. Lets fix it.
                LogFeed.log(LogObject.forDebug("Attempting credentials fix...", "Guild Id: " + settings.getGuildID()));

                return correctAssignedCredentialId(data).flatMap(success -> {
                    if (success) {
                        return DatabaseManager.getCalendar(data.getGuildId(), data.getCalendarNumber())
                            .flatMap(cd -> createEvent(cd, event, settings));
                    } else {
                        return Mono.empty();
                    }
                });
            }
            return Mono.empty();
        }).doOnError(e -> LogFeed.log(LogObject
            .forException("Event create error; Cred Id: " + data.getCredentialId(), e, EventWrapper.class))
        ).onErrorResume(e -> Mono.empty());
    }

    public static Mono<Event> updateEvent(final CalendarData data, final Event event, final GuildSettings settings) {
        return CalendarAuth.getCalendarService(settings, data).flatMap(service ->
            Mono.fromCallable(() ->
                service.events()
                    .update(data.getCalendarId(), event.getId(), event)
                    .execute()
            ).subscribeOn(Schedulers.boundedElastic())
        ).onErrorResume(GoogleJsonResponseException.class, e -> {
            if (e.getStatusCode() == 404 ||
                "requiredAccessLevel".equalsIgnoreCase(e.getDetails().getErrors().get(0).getReason())) {
                //This is caused by credentials issue. Lets fix it.
                LogFeed.log(LogObject.forDebug("Attempting credentials fix...", "Guild Id: " + settings.getGuildID()));

                return correctAssignedCredentialId(data).flatMap(success -> {
                    if (success) {
                        return DatabaseManager.getCalendar(data.getGuildId(), data.getCalendarNumber())
                            .flatMap(cd -> updateEvent(cd, event, settings));
                    } else {
                        return Mono.empty();
                    }
                });
            }
            return Mono.empty();
        }).onErrorResume(e -> Mono.empty());
    }

    public static Mono<Event> getEvent(final CalendarData data, final GuildSettings settings, final String id) {
        return CalendarAuth.getCalendarService(settings, data).flatMap(service ->
            Mono.fromCallable(() ->
                service.events()
                    .get(data.getCalendarAddress(), id)
                    .execute()
            ).subscribeOn(Schedulers.boundedElastic())
        ).onErrorResume(e -> Mono.empty()); //Can ignore this, the event just doesn't exist.
    }

    public static Mono<List<Event>> getEvents(final CalendarData data, final GuildSettings settings, final int amount,
                                              final long start) {
        return CalendarAuth.getCalendarService(settings, data).flatMap(service ->
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

    public static Mono<List<Event>> getEvents(final CalendarData data, final Calendar service, final int amount,
                                              final long start) {
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

    public static Mono<List<Event>> getEvents(final CalendarData data, final GuildSettings settings, final int amount,
                                              final long start, final long end) {
        return CalendarAuth.getCalendarService(settings, data).flatMap(service ->
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

    public static Mono<List<Event>> getEvents(final CalendarData data, final GuildSettings settings, final long start,
                                              final long end) {
        return CalendarAuth.getCalendarService(settings, data).flatMap(service ->
            Mono.fromCallable(() ->
                service.events().list(data.getCalendarId())
                    .setTimeMin(new DateTime(start))
                    .setTimeMax(new DateTime(end))
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .setShowDeleted(false)
                    .execute().getItems()
            ).subscribeOn(Schedulers.boundedElastic())
        ).onErrorResume(e -> Mono.empty());
    }

    public static Mono<Void> deleteEvent(final CalendarData data, final GuildSettings settings, final String id) {
        return CalendarAuth.getCalendarService(settings, data).flatMap(service ->
            Mono.fromCallable(() ->
                service.events()
                    .delete(data.getCalendarAddress(), id)
                    .execute()
            ).subscribeOn(Schedulers.boundedElastic())
        ).onErrorResume(GoogleJsonResponseException.class, e -> {
            if (e.getStatusCode() == 404 ||
                "requiredAccessLevel".equalsIgnoreCase(e.getDetails().getErrors().get(0).getReason())) {
                //This is caused by credentials issue. Lets fix it.
                LogFeed.log(LogObject.forDebug("Attempting credentials fix...", "Guild Id: " + settings.getGuildID()));

                return correctAssignedCredentialId(data).flatMap(success -> {
                    if (success) {
                        return DatabaseManager.getCalendar(data.getGuildId(), data.getCalendarNumber())
                            .flatMap(cd -> deleteEvent(cd, settings, id));
                    } else {
                        return Mono.empty();
                    }
                });
            }
            return Mono.empty();
        }).onErrorResume(e -> Mono.empty());
    }


    private static Mono<Boolean> correctAssignedCredentialId(CalendarData data) {
        return Flux.range(0, CalendarAuth.credentialsCount()).flatMap(i ->
            CalendarAuth.getCalendarService(i).flatMap(service -> Mono.fromCallable(() ->
                    service.calendarList().get(data.getCalendarId()).execute()
                ).subscribeOn(Schedulers.boundedElastic())
                .onErrorResume(GoogleJsonResponseException.class, e -> {
                    if (e.getStatusCode() == 404 ||
                        "requiredAccessLevel".equalsIgnoreCase(e.getDetails().getErrors().get(0).getReason())) {
                        return Mono.empty();
                    }

                    return Mono.empty();
                })
            ).map(cal -> "owner".equalsIgnoreCase(cal.getAccessRole()) ? i : -1)
            ).filter(i -> i > -1)
            .flatMap(correctCredential -> {
                //Ayyyyy we found the correct one!! Lets go ahead and save that and bump this back to the calling method
                CalendarData corrected = CalendarData.fromData(
                    data.getGuildId(),
                    data.getCalendarNumber(),
                    data.getCalendarId(),
                    data.getCalendarAddress(),
                    data.isExternal(),
                    correctCredential);


                LogFeed.log(LogObject.forDebug("Corrected credentials issue! Yay!", "Guild ID: " + data.getGuildId()));
                return DatabaseManager.updateCalendar(corrected);
            }).any(bool -> bool);
    }
}
