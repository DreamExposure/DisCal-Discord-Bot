package org.dreamexposure.discal.core.wrapper.google;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpStatusCodes;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import org.dreamexposure.discal.core.calendar.CalendarAuth;
import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.logger.LogFeed;
import org.dreamexposure.discal.core.logger.object.LogObject;
import org.dreamexposure.discal.core.object.calendar.CalendarData;
import org.dreamexposure.discal.core.utils.GlobalConst;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@SuppressWarnings("DuplicatedCode")
public class EventWrapper {
    public static Mono<Event> createEvent(CalendarData data, Event event) {
        return CalendarAuth.getCalendarService(data).flatMap(service ->
            Mono.fromCallable(() ->
                service.events()
                    .insert(data.getCalendarId(), event)
                    .setQuotaUser(data.getGuildId().asString())
                    .execute()
            ).subscribeOn(Schedulers.boundedElastic())
        ).onErrorResume(GoogleJsonResponseException.class, e -> {
            if (e.getStatusCode() == GlobalConst.STATUS_NOT_FOUND ||
                "requiredAccessLevel".equalsIgnoreCase(e.getDetails().getErrors().get(0).getReason())) {
                //This is caused by credentials issue. Lets fix it.
                LogFeed.log(LogObject.forDebug("Attempting credentials fix...", "Guild Id: " + data.getGuildId()));

                return correctAssignedCredentialId(data).flatMap(success -> {
                    if (success) {
                        return DatabaseManager.INSTANCE.getCalendar(data.getGuildId(), data.getCalendarNumber())
                            .flatMap(cd -> createEvent(cd, event));
                    } else {
                        return Mono.empty();
                    }
                });
            } else {
                //Some other error, lets log it, might be getting swallowed
                LogFeed.log(LogObject
                    .forException("Event create error; Cred Id: " + data.getCredentialId(), e, EventWrapper.class));
            }
            return Mono.empty();
        }).doOnError(e -> LogFeed.log(LogObject
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
        ).onErrorResume(GoogleJsonResponseException.class, e -> {
            if (e.getStatusCode() == GlobalConst.STATUS_NOT_FOUND ||
                "requiredAccessLevel".equalsIgnoreCase(e.getDetails().getErrors().get(0).getReason())) {
                //This is caused by credentials issue. Lets fix it.
                LogFeed.log(LogObject.forDebug("Attempting credentials fix...", "Guild Id: " + data.getGuildId()));

                return correctAssignedCredentialId(data).flatMap(success -> {
                    if (success) {
                        return DatabaseManager.INSTANCE.getCalendar(data.getGuildId(), data.getCalendarNumber())
                            .flatMap(cd -> patchEvent(cd, event));
                    } else {
                        return Mono.empty();
                    }
                });
            } else {
                //Some other error, lets log it, might be getting swallowed
                LogFeed.log(LogObject
                    .forException("Event patch error; Cred Id: " + data.getCredentialId(), e, EventWrapper.class));
            }
            return Mono.empty();
        }).doOnError(e -> LogFeed.log(LogObject.forException("Failed to patch event", e, EventWrapper.class))
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
        ).onErrorResume(GoogleJsonResponseException.class, e -> {
            if (e.getStatusCode() == GlobalConst.STATUS_NOT_FOUND ||
                "requiredAccessLevel".equalsIgnoreCase(e.getDetails().getErrors().get(0).getReason())) {
                //This is caused by credentials issue. Lets fix it.
                LogFeed.log(LogObject.forDebug("Attempting credentials fix...", "Guild Id: " + data.getGuildId()));

                return correctAssignedCredentialId(data).flatMap(success -> {
                    if (success) {
                        return DatabaseManager.INSTANCE.getCalendar(data.getGuildId(), data.getCalendarNumber())
                            .flatMap(cd -> updateEvent(cd, event));
                    } else {
                        return Mono.empty();
                    }
                });
            } else {
                //Some other error, lets log it, might be getting swallowed
                LogFeed.log(LogObject
                    .forException("Event update error; Cred Id: " + data.getCredentialId(), e, EventWrapper.class));
            }
            return Mono.empty();
        }).doOnError(e -> LogFeed.log(LogObject.forException("Failed to edit event", e, EventWrapper.class))
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
        ).onErrorResume(GoogleJsonResponseException.class, e -> {
            if ("requiredAccessLevel".equalsIgnoreCase(e.getDetails().getErrors().get(0).getReason())) {
                //This is caused by credentials issue. Lets fix it.
                LogFeed.log(LogObject.forDebug("Attempting credentials fix...", "Guild Id: " + data.getGuildId()));

                return correctAssignedCredentialId(data).flatMap(success -> {
                    if (success) {
                        return DatabaseManager.INSTANCE.getCalendar(data.getGuildId(), data.getCalendarNumber())
                            .flatMap(cd -> deleteEvent(cd, id));
                    } else {
                        return Mono.just(false);
                    }
                });
            } else {
                //This is some other issue I am not currently aware of, logging for handling
                LogFeed.log(LogObject.forException("GJRE: Event delete Failure", e, EventWrapper.class));
                return Mono.just(false);
            }
        }).doOnError(e -> LogFeed.log(LogObject.forException("Event Delete Failure", e, EventWrapper.class))
        ).onErrorReturn(false);
    }


    private static Mono<Boolean> correctAssignedCredentialId(CalendarData data) {
        return Flux.range(0, CalendarAuth.credentialsCount()).flatMap(i ->
            CalendarAuth.getCalendarService(i).flatMap(service -> Mono.fromCallable(() ->
                    service.calendarList()
                        .get(data.getCalendarId())
                        .setQuotaUser(data.getGuildId().asString())
                        .execute()
                ).subscribeOn(Schedulers.boundedElastic())
                    .onErrorResume(GoogleJsonResponseException.class, e -> {
                        if (e.getStatusCode() == GlobalConst.STATUS_NOT_FOUND ||
                            "requiredAccessLevel".equalsIgnoreCase(e.getDetails().getErrors().get(0).getReason())) {
                            return Mono.empty();
                        }

                        return Mono.empty();
                    })
            ).map(cal -> "owner".equalsIgnoreCase(cal.getAccessRole()) ? i : -1)
        ).filter(i -> i > -1)
            .flatMap(correctCredential -> {
                //Ayyyyy we found the correct one!! Lets go ahead and save that and bump this back to the calling method
                CalendarData corrected = new CalendarData(
                    data.getGuildId(),
                    data.getCalendarNumber(),
                    data.getHost(),
                    data.getCalendarId(),
                    data.getCalendarAddress(),
                    data.getExternal(),
                    correctCredential,
                    data.getPrivateKey(),
                    data.getEncryptedAccessToken(),
                    data.getEncryptedRefreshToken());


                LogFeed.log(LogObject.forDebug("Corrected credentials issue! Yay!", "Guild ID: " + data.getGuildId()));
                return DatabaseManager.INSTANCE.updateCalendar(corrected);
            }).any(bool -> bool);
    }
}
