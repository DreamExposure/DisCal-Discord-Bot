package org.dreamexposure.discal.client.module.announcement;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;

import org.dreamexposure.discal.client.DisCalClient;
import org.dreamexposure.discal.client.message.AnnouncementMessageFormatter;
import org.dreamexposure.discal.core.calendar.CalendarAuth;
import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.enums.announcement.AnnouncementType;
import org.dreamexposure.discal.core.enums.event.EventColor;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.announcement.Announcement;
import org.dreamexposure.discal.core.object.calendar.CalendarData;
import org.dreamexposure.discal.core.utils.EventUtils;
import org.dreamexposure.discal.core.wrapper.google.EventWrapper;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import discord4j.rest.util.Snowflake;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.function.TupleUtils;

@SuppressWarnings({"WeakerAccess", "Duplicates"})
public class AnnouncementThread {

    private final Mono<Calendar> discalService;

    private final Map<Snowflake, Mono<GuildSettings>> allSettings = new ConcurrentHashMap<>();
    private final Map<Snowflake, Mono<CalendarData>> calendars = new ConcurrentHashMap<>();
    private final Map<Snowflake, Mono<Calendar>> customServices = new ConcurrentHashMap<>();
    private final Map<Snowflake, Mono<List<Event>>> allEvents = new ConcurrentHashMap<>();

    public AnnouncementThread() {
        discalService = CalendarAuth.getCalendarService(null).cache();
    }

    public Mono<Void> run() {
        return Mono.defer(() -> {
            if (DisCalClient.getClient() == null)
                return Mono.empty();

            return DisCalClient.getClient().getGuilds()
                .flatMap(guild -> DatabaseManager.getEnabledAnnouncements(guild.getId())
                    .flatMapMany(Flux::fromIterable)
                    .flatMap(a -> {
                        Mono<GuildSettings> s = getSettings(a);
                        Mono<CalendarData> cd = getCalendarData(a);
                        Mono<Calendar> se = s.flatMap(this::getService);

                        return Mono.zip(s, cd, se)
                            .map(TupleUtils.function((settings, calData, service) -> {
                                switch (a.getAnnouncementType()) {
                                    case SPECIFIC:
                                        return EventUtils.eventExists(settings, calData.getCalendarNumber(), a.getEventId())
                                            .filter(identity -> identity)
                                            .switchIfEmpty(DatabaseManager.deleteAnnouncement(a.getAnnouncementId().toString())
                                                .then(Mono.empty()))
                                            .flatMap(ignored -> EventWrapper.getEvent(calData, settings, a.getEventId()))
                                            .filter(event -> inRange(a, event))
                                            .flatMap(e ->
                                                AnnouncementMessageFormatter.sendAnnouncementMessage(guild, a, e, calData, settings)
                                                    .then(DatabaseManager.deleteAnnouncement(a.getAnnouncementId().toString()))
                                            );
                                    case UNIVERSAL:
                                        return getEvents(settings, calData, service)
                                            .flatMapMany(Flux::fromIterable)
                                            .filter(e -> inRange(a, e))
                                            .flatMap(e ->
                                                AnnouncementMessageFormatter
                                                    .sendAnnouncementMessage(guild, a, e, calData, settings)
                                            );
                                    case COLOR:
                                        return getEvents(settings, calData, service)
                                            .flatMapMany(Flux::fromIterable)
                                            .filter(e -> e.getColorId() != null
                                                && a.getEventColor().equals(EventColor.fromNameOrHexOrID(e.getColorId()))
                                            )
                                            .filter(e -> inRange(a, e))
                                            .flatMap(e ->
                                                AnnouncementMessageFormatter
                                                    .sendAnnouncementMessage(guild, a, e, calData, settings));
                                    case RECUR:
                                        return getEvents(settings, calData, service)
                                            .flatMapMany(Flux::fromIterable)
                                            .filter(e -> e.getId().contains("_") && e.getId().split("_")[0].equals(a.getEventId()))
                                            .filter(e -> inRange(a, e))
                                            .flatMap(e ->
                                                AnnouncementMessageFormatter
                                                    .sendAnnouncementMessage(guild, a, e, calData, settings));
                                    default:
                                        return Mono.empty();
                                }
                            }));
                    }).onErrorResume(e -> Mono.empty())
                ).onErrorResume(e -> Mono.empty())
                .doFinally(ignore -> {
                    allSettings.clear();
                    calendars.clear();
                    customServices.clear();
                    allEvents.clear();
                }).subscribeOn(Schedulers.immediate())
                .then();
        });
    }

    private boolean inRange(Announcement a, Event e) {
        long maxDifferenceMs = 5 * 60 * 1000; //5 minutes

        long announcementTimeMs = Integer.toUnsignedLong(a.getMinutesBefore() + (a.getHoursBefore() * 60)) * 60 * 1000;
        long timeUntilEvent = getEventStartMs(e) - System.currentTimeMillis();

        long difference = timeUntilEvent - announcementTimeMs;

        if (difference < 0) {
            //Event past, we can delete announcement depending on the type
            if (a.getAnnouncementType() == AnnouncementType.SPECIFIC)
                DatabaseManager.deleteAnnouncement(a.getAnnouncementId().toString()).subscribe();

            return false;
        } else {
            return difference <= maxDifferenceMs;
        }
    }

    private long getEventStartMs(Event e) {
        if (e.getStart().getDateTime() != null)
            return e.getStart().getDateTime().getValue();
        else
            return e.getStart().getDate().getValue();

    }

    private Mono<GuildSettings> getSettings(Announcement a) {
        if (!allSettings.containsKey(a.getGuildId()))
            allSettings.put(a.getGuildId(), DatabaseManager.getSettings(a.getGuildId()).cache());

        return allSettings.get(a.getGuildId());
    }

    //TODO: Allow multiple calendar support
    private Mono<CalendarData> getCalendarData(Announcement a) {
        if (!calendars.containsKey(a.getGuildId()))
            calendars.put(a.getGuildId(), DatabaseManager.getMainCalendar(a.getGuildId()).cache());

        return calendars.get(a.getGuildId());
    }

    private Mono<Calendar> getService(GuildSettings gs) {
        if (gs.useExternalCalendar()) {
            if (!customServices.containsKey(gs.getGuildID()))
                customServices.put(gs.getGuildID(), CalendarAuth.getCalendarService(gs).cache());

            return customServices.get(gs.getGuildID());
        }
        return discalService;
    }

    private Mono<List<Event>> getEvents(GuildSettings gs, CalendarData cd, Calendar service) {
        if (!allEvents.containsKey(gs.getGuildID())) {
            Mono<List<Event>> events = EventWrapper.getEvents(cd, service, 15, System.currentTimeMillis()).cache();
            allEvents.put(gs.getGuildID(), events);
        }
        return allEvents.get(gs.getGuildID());
    }
}