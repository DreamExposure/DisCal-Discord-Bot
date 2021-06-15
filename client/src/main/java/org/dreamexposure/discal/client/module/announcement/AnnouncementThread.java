package org.dreamexposure.discal.client.module.announcement;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Guild;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.dreamexposure.discal.core.enums.announcement.AnnouncementType.SPECIFIC;
import static reactor.function.TupleUtils.function;

public class AnnouncementThread {
    private final GatewayDiscordClient client;

    private final Map<Snowflake, Mono<GuildSettings>> allSettings = new ConcurrentHashMap<>();
    private final Map<Snowflake, Mono<CalendarData>> calendars = new ConcurrentHashMap<>();
    private final Map<Snowflake, Mono<Calendar>> customServices = new ConcurrentHashMap<>();
    private final Map<Snowflake, Mono<List<Event>>> allEvents = new ConcurrentHashMap<>();

    private final Map<Integer, Mono<Calendar>> discalServices = new HashMap<>();

    private final long maxDifferenceMs = 5 * GlobalConst.oneMinuteMs;

    public AnnouncementThread(GatewayDiscordClient client) {
        this.client = client;

        for (int i = 0; i < CalendarAuth.credentialsCount(); i++) {
            this.discalServices.put(i, CalendarAuth.getCalendarService(i).cache());
        }
    }

    public Mono<Void> run() {
        return this.client.getGuilds()
            .flatMap(guild -> DatabaseManager.INSTANCE.getEnabledAnnouncements(guild.getId())
                .flatMapMany(Flux::fromIterable)
                .flatMap(a -> {

                    final Mono<GuildSettings> s = this.getSettings(a).cache();
                    final Mono<CalendarData> cd = this.getCalendarData(a).cache();
                    final Mono<Calendar> se = cd.flatMap(calData -> s.flatMap(gs -> this.getService(calData)));

                    return Mono.zip(s, cd, se)
                        .flatMap(function((settings, calData, service) -> {
                            switch (a.getModifier()) {
                                case BEFORE:
                                    return this.handleBeforeModifier(guild, a, settings, calData, service);
                                case DURING:
                                    return this.handleDuringModifier(guild, a, settings, calData, service);
                                case END:
                                    return this.handleEndModifier(guild, a, settings, calData, service);
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
                this.allSettings.clear();
                this.calendars.clear();
                this.customServices.clear();
                this.allEvents.clear();
            })
            .then();
    }

    //Modifier handling
    private Mono<Void> handleBeforeModifier(Guild guild, Announcement a, GuildSettings settings, CalendarData calData,
                                            Calendar service) {
        switch (a.getType()) {
            case SPECIFIC:
                return EventWrapper.getEvent(calData, a.getEventId())
                    .switchIfEmpty(DatabaseManager.INSTANCE.deleteAnnouncement(a.getAnnouncementId().toString())
                        .then(Mono.empty())
                    ).flatMap(e -> this.inRangeSpecific(a, e)
                        .flatMap(inRange -> {
                            if (inRange) {
                                return AnnouncementMessageFormatter
                                    .sendAnnouncementMessage(guild, a, e, calData, settings)
                                    .then(DatabaseManager
                                        .INSTANCE.deleteAnnouncement(a.getAnnouncementId().toString())
                                    );
                            } else {
                                return Mono.empty(); //Not in range, but still valid.
                            }
                        }))
                    .then();
            case UNIVERSAL:
                return this.getEvents(calData, service)
                    .flatMapMany(Flux::fromIterable)
                    .filter(e -> this.isInRange(a, e))
                    .flatMap(e -> AnnouncementMessageFormatter
                        .sendAnnouncementMessage(guild, a, e, calData, settings))
                    .then();
            case COLOR:
                return this.getEvents(calData, service)
                    .flatMapMany(Flux::fromIterable)
                    .filter(e -> e.getColorId() != null
                        && a.getEventColor().equals(EventColor
                        .Companion.fromNameOrHexOrId(e.getColorId())))
                    .filter(e -> this.isInRange(a, e))
                    .flatMap(e -> AnnouncementMessageFormatter
                        .sendAnnouncementMessage(guild, a, e, calData, settings))
                    .then();

            case RECUR:
                return this.getEvents(calData, service)
                    .flatMapMany(Flux::fromIterable)
                    .filter(e -> e.getId().contains("_") && e.getId().split("_")[0].equals(a.getEventId()))
                    .filter(e -> this.isInRange(a, e))
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
        switch (a.getType()) {
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
        switch (a.getType()) {
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
            long announcementTimeMs = Integer.toUnsignedLong(a.getMinutesBefore() + (a.getHoursBefore() * 60)) * 60 * 1000;
            long timeUntilEvent = this.getEventStartMs(e) - System.currentTimeMillis();

            long difference = timeUntilEvent - announcementTimeMs;

            if (difference < 0) {
                //Event past, we can delete announcement depending on the type
                if (a.getType() == SPECIFIC)
                    return DatabaseManager.INSTANCE.deleteAnnouncement(a.getAnnouncementId().toString())
                        .thenReturn(false);

                return Mono.just(false);
            } else {
                return Mono.just(difference <= this.maxDifferenceMs);
            }
        });
    }

    private boolean isInRange(Announcement a, Event e) {
        long announcementTimeMs = Integer.toUnsignedLong(a.getMinutesBefore() + (a.getHoursBefore() * 60)) * 60 * 1000;
        long timeUntilEvent = this.getEventStartMs(e) - System.currentTimeMillis();

        long difference = timeUntilEvent - announcementTimeMs;

        if (difference < 0) {
            //Event past, we can delete announcement depending on the type
            if (a.getType() == SPECIFIC)
                return false; //Shouldn't even be used for specific types...

            return false;
        } else {
            return difference <= this.maxDifferenceMs;
        }
    }

    private long getEventStartMs(Event e) {
        if (e.getStart().getDateTime() != null)
            return e.getStart().getDateTime().getValue();
        else
            return e.getStart().getDate().getValue();

    }

    private Mono<GuildSettings> getSettings(Announcement a) {
        if (!this.allSettings.containsKey(a.getGuildId()))
            this.allSettings.put(a.getGuildId(), DatabaseManager.INSTANCE.getSettings(a.getGuildId()).cache());

        return this.allSettings.get(a.getGuildId());
    }

    //TODO: Allow multiple calendar support
    private Mono<CalendarData> getCalendarData(Announcement a) {
        if (!this.calendars.containsKey(a.getGuildId()))
            this.calendars.put(a.getGuildId(), DatabaseManager.INSTANCE.getMainCalendar(a.getGuildId()).cache());

        return this.calendars.get(a.getGuildId());
    }

    private Mono<Calendar> getService(CalendarData cd) {
        if (cd.getExternal()) {
            if (!this.customServices.containsKey(cd.getGuildId()))
                this.customServices.put(cd.getGuildId(), CalendarAuth.getCalendarService(cd).cache());

            return this.customServices.get(cd.getGuildId());
        }
        return this.discalServices.get(cd.getCredentialId());
    }

    private Mono<List<Event>> getEvents(CalendarData cd, Calendar service) {
        if (!this.allEvents.containsKey(cd.getGuildId())) {
            Mono<List<Event>> events = EventWrapper.getEvents(cd, service, 15, System.currentTimeMillis()).cache();
            this.allEvents.put(cd.getGuildId(), events);
        }
        return this.allEvents.get(cd.getGuildId());
    }
}
