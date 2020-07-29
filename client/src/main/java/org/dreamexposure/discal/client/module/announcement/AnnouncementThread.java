package org.dreamexposure.discal.client.module.announcement;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;

import org.dreamexposure.discal.client.message.AnnouncementMessageFormatter;
import org.dreamexposure.discal.core.calendar.CalendarAuth;
import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.enums.event.EventColor;
import org.dreamexposure.discal.core.logger.LogFeed;
import org.dreamexposure.discal.core.logger.object.LogObject;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.announcement.Announcement;
import org.dreamexposure.discal.core.object.calendar.CalendarData;
import org.dreamexposure.discal.core.utils.GlobalConst;
import org.dreamexposure.discal.core.wrapper.google.EventWrapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Guild;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.dreamexposure.discal.core.enums.announcement.AnnouncementType.SPECIFIC;
import static reactor.function.TupleUtils.function;

public class AnnouncementThread {
    private final GatewayDiscordClient client;

    private final Map<Snowflake, Mono<GuildSettings>> allSettings = new ConcurrentHashMap<>();
    private final Map<Snowflake, Mono<CalendarData>> calendars = new ConcurrentHashMap<>();
    private final Map<Snowflake, Mono<Calendar>> customServices = new ConcurrentHashMap<>();
    private final Map<Snowflake, Mono<List<Event>>> allEvents = new ConcurrentHashMap<>();

    private final Map<Integer, Mono<Calendar>> discalServices = new HashMap<>();

    public AnnouncementThread(GatewayDiscordClient client) {
        this.client = client;

        for (int i = 0; i < CalendarAuth.credentialsCount(); i++) {
            this.discalServices.put(i, CalendarAuth.getCalendarService(i).cache());
        }
    }

    public Mono<Void> run() {
        return client.getGuilds()
            .flatMap(guild -> DatabaseManager.getEnabledAnnouncements(guild.getId())
                .flatMapMany(Flux::fromIterable)
                .flatMap(a -> {

                    Mono<GuildSettings> s = getSettings(a);
                    Mono<CalendarData> cd = getCalendarData(a);
                    Mono<Calendar> se = s.flatMap(this::getService);

                    return Mono.zip(s, cd, se)
                        .flatMap(function((settings, calData, service) -> {
                            switch (a.getModifier()) {
                                case BEFORE:
                                    return handleBeforeModifier(guild, a, settings, calData, service);
                                case DURING:
                                    return handleDuringModifier(guild, a, settings, calData, service);
                                case END:
                                    return handleEndModifier(guild, a, settings, calData, service);
                                default:
                                    return Mono.empty();
                            }
                        }));
                })
                .doOnError(e -> LogFeed.log(LogObject.forException("Announcement Error", e, AnnouncementThread.class)))
                .onErrorResume(e -> Mono.empty())
            )
            .doOnError(e -> LogFeed.log(LogObject.forException("Announcement Error", e, AnnouncementThread.class)))
            .onErrorResume(e -> Mono.empty())
            .doFinally(ignore -> {
                allSettings.clear();
                calendars.clear();
                customServices.clear();
                allEvents.clear();
            })
            .then();
    }

    //Modifier handling
    private Mono<Void> handleBeforeModifier(Guild guild, Announcement a, GuildSettings settings, CalendarData calData,
                                            Calendar service) {
        switch (a.getAnnouncementType()) {
            case SPECIFIC:
                return EventWrapper.getEvent(calData, settings, a.getEventId())
                    .flatMap(e -> inRangeSpecific(a, e)
                        .flatMap(inRange -> {
                            if (inRange) {
                                return AnnouncementMessageFormatter
                                    .sendAnnouncementMessage(guild, a, e, calData, settings)
                                    .then(DatabaseManager
                                        .deleteAnnouncement(a.getAnnouncementId().toString())
                                    );
                            } else {
                                return Mono.empty(); //Not in range, but still valid.
                            }
                        }))
                    .switchIfEmpty(DatabaseManager
                        .deleteAnnouncement(a.getAnnouncementId().toString()))
                    .then();
            case UNIVERSAL:
                return getEvents(settings, calData, service)
                    .flatMapMany(Flux::fromIterable)
                    .filter(e -> inRange(a, e))
                    .flatMap(e -> AnnouncementMessageFormatter
                        .sendAnnouncementMessage(guild, a, e, calData, settings))
                    .then();
            case COLOR:
                return getEvents(settings, calData, service)
                    .flatMapMany(Flux::fromIterable)
                    .filter(e -> e.getColorId() != null
                        && a.getEventColor().equals(EventColor
                        .fromNameOrHexOrID(e.getColorId())))
                    .filter(e -> inRange(a, e))
                    .flatMap(e -> AnnouncementMessageFormatter
                        .sendAnnouncementMessage(guild, a, e, calData, settings))
                    .then();

            case RECUR:
                return getEvents(settings, calData, service)
                    .flatMapMany(Flux::fromIterable)
                    .filter(e -> e.getId().contains("_")
                        && e.getId().split("_")[0].equals(a.getEventId()))
                    .filter(e -> inRange(a, e))
                    .flatMap(e -> AnnouncementMessageFormatter
                        .sendAnnouncementMessage(guild, a, e, calData, settings))
                    .then();
            default:
                return Mono.empty();
        }
    }

    //TODO: Actually support this.
    private Mono<Void> handleDuringModifier(Guild guild, Announcement a, GuildSettings settings, CalendarData calData,
                                            Calendar service) {
        switch (a.getAnnouncementType()) {
            case SPECIFIC:
            case UNIVERSAL:
            case COLOR:
            case RECUR:
            default:
                return Mono.empty();
        }
    }

    //TODO: Actually support this too
    private Mono<Void> handleEndModifier(Guild guild, Announcement a, GuildSettings settings, CalendarData calData,
                                         Calendar service) {
        switch (a.getAnnouncementType()) {
            case SPECIFIC:
            case UNIVERSAL:
            case COLOR:
            case RECUR:
            default:
                return Mono.empty();
        }
    }


    //Utility
    private Mono<Boolean> inRangeSpecific(Announcement a, Event e) {
        return Mono.defer(() -> {
            long maxDifferenceMs = 5 * GlobalConst.oneMinuteMs;

            long announcementTimeMs = Integer.toUnsignedLong(a.getMinutesBefore() + (a.getHoursBefore() * 60)) * 60 * 1000;
            long timeUntilEvent = getEventStartMs(e) - System.currentTimeMillis();

            long difference = timeUntilEvent - announcementTimeMs;

            if (difference < 0) {
                //Event past, we can delete announcement depending on the type
                if (a.getAnnouncementType() == SPECIFIC)
                    return DatabaseManager.deleteAnnouncement(a.getAnnouncementId().toString())
                        .thenReturn(false);

                return Mono.just(false);
            } else {
                return Mono.just(difference <= maxDifferenceMs);
            }
        });
    }

    private boolean inRange(Announcement a, Event e) {
        long maxDifferenceMs = 5 * GlobalConst.oneMinuteMs;

        long announcementTimeMs = Integer.toUnsignedLong(a.getMinutesBefore() + (a.getHoursBefore() * 60)) * 60 * 1000;
        long timeUntilEvent = getEventStartMs(e) - System.currentTimeMillis();

        long difference = timeUntilEvent - announcementTimeMs;

        if (difference < 0) {
            //Event past, we can delete announcement depending on the type
            if (a.getAnnouncementType() == SPECIFIC)
                return false; //Shouldn't even be used for specific types...

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
        return discalServices.get(gs.getCredentialsId());
    }

    private Mono<List<Event>> getEvents(GuildSettings gs, CalendarData cd, Calendar service) {
        if (!allEvents.containsKey(gs.getGuildID())) {
            Mono<List<Event>> events = EventWrapper.getEvents(cd, service, 15, System.currentTimeMillis()).cache();
            allEvents.put(gs.getGuildID(), events);
        }
        return allEvents.get(gs.getGuildID());
    }
}