package org.dreamexposure.discal.core.utils;

import com.google.api.services.calendar.model.Event;

import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.enums.event.EventColor;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.event.EventData;
import org.dreamexposure.discal.core.object.event.PreEvent;
import org.dreamexposure.discal.core.wrapper.google.EventWrapper;

import discord4j.common.util.Snowflake;
import reactor.core.publisher.Mono;

/**
 * Created by Nova Fox on 11/10/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
public class EventUtils {

    @Deprecated
    public static Mono<Boolean> deleteEvent(GuildSettings settings, String eventId) {
        return DatabaseManager.getMainCalendar(settings.getGuildID())
            .flatMap(data ->
                EventWrapper.deleteEvent(data, settings, eventId)
                    .then(Mono.when(
                        DatabaseManager.deleteAnnouncementsForEvent(settings.getGuildID(), eventId),
                        DatabaseManager.deleteEventData(eventId))
                    ).thenReturn(true)
            ).defaultIfEmpty(false);
    }

    public static Mono<Boolean> deleteEvent(GuildSettings settings, int calNumber, String eventId) {
        return DatabaseManager.getCalendar(settings.getGuildID(), calNumber)
            .flatMap(data ->
                EventWrapper.deleteEvent(data, settings, eventId)
                    .then(Mono.when(
                        DatabaseManager.deleteAnnouncementsForEvent(settings.getGuildID(), eventId),
                        DatabaseManager.deleteEventData(eventId))
                    ).thenReturn(true)
            ).defaultIfEmpty(false);
    }

    @Deprecated
    public static Mono<Boolean> eventExists(GuildSettings settings, String eventId) {
        return DatabaseManager.getMainCalendar(settings.getGuildID())
            .flatMap(data ->
                EventWrapper.getEvent(data, settings, eventId)
                    .hasElement()
            ).switchIfEmpty(Mono.just(false));
    }

    public static Mono<Boolean> eventExists(GuildSettings settings, int calNumber, String eventId) {
        return DatabaseManager.getCalendar(settings.getGuildID(), calNumber)
            .flatMap(data ->
                EventWrapper.getEvent(data, settings, eventId)
                    .hasElement()
            ).switchIfEmpty(Mono.just(false));
    }

    public static Mono<PreEvent> copyEvent(Snowflake guildId, Event event) {
        return DatabaseManager.getEventData(guildId, event.getId())
            .flatMap(data -> Mono.just(new PreEvent(guildId))
                .doOnNext(p -> p.setEventData(data))
                .doOnNext(p -> p.setSummary(event.getSummary()))
                .doOnNext(p -> p.setDescription(event.getDescription()))
                .doOnNext(p -> p.setLocation(event.getLocation()))
                .doOnNext(p -> {
                    if (event.getColorId() != null)
                        p.setColor(EventColor.fromNameOrHexOrID(event.getColorId()));
                    else
                        p.setColor(EventColor.NONE);
                })
            ).switchIfEmpty(Mono.just(new PreEvent(guildId))
                .doOnNext(p -> p.setEventData(EventData.empty()))
                .doOnNext(p -> p.setSummary(event.getSummary()))
                .doOnNext(p -> p.setDescription(event.getDescription()))
                .doOnNext(p -> p.setLocation(event.getLocation()))
                .doOnNext(p -> {
                    if (event.getColorId() != null)
                        p.setColor(EventColor.fromNameOrHexOrID(event.getColorId()));
                    else
                        p.setColor(EventColor.NONE);
                })
            );
    }
}