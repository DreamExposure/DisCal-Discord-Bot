package org.dreamexposure.discal.client.event;

import com.google.api.services.calendar.model.Event;

import org.dreamexposure.discal.client.message.EventMessageFormatter;
import org.dreamexposure.discal.client.message.Messages;
import org.dreamexposure.discal.core.crypto.KeyGenerator;
import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.enums.event.EventColor;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.event.EventCreatorResponse;
import org.dreamexposure.discal.core.object.event.EventData;
import org.dreamexposure.discal.core.object.event.PreEvent;
import org.dreamexposure.discal.core.wrapper.google.CalendarWrapper;
import org.dreamexposure.discal.core.wrapper.google.EventWrapper;

import java.util.ArrayList;
import java.util.Arrays;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.rest.util.Snowflake;
import reactor.core.publisher.Mono;

/**
 * Created by Nova Fox on 1/3/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class EventCreator {
    static {
        instance = new EventCreator();
    }

    private static final EventCreator instance;
    private final ArrayList<PreEvent> events = new ArrayList<>();

    private EventCreator() {
    } //Prevent initialization.

    public static EventCreator getCreator() {
        return instance;
    }

    //Functional
    public Mono<PreEvent> init(MessageCreateEvent e, GuildSettings settings) {
        if (!hasPreEvent(settings.getGuildID())) {
            return DatabaseManager.getCalendar(settings.getGuildID(), 1) //TODO: handle multiple calendars
                .flatMap(calData -> {
                    PreEvent event = new PreEvent(settings.getGuildID());
                    events.add(event);

                    return CalendarWrapper.getCalendar(calData, settings)
                        .doOnNext(c -> event.setTimeZone(c.getTimeZone()))
                        .flatMap(c -> EventMessageFormatter.getPreEventEmbed(event, settings))
                        .flatMap(embed -> Messages.sendMessage(
                            Messages.getMessage("Creator.Event.Copy.Init", settings), embed, e))
                        .doOnNext(event::setCreatorMessage)
                        .thenReturn(event);
                });
        }
        return Mono.justOrEmpty(getPreEvent(settings.getGuildID()));
    }

    public Mono<PreEvent> init(MessageCreateEvent e, GuildSettings settings, String summary) {
        if (!hasPreEvent(settings.getGuildID())) {
            return DatabaseManager.getCalendar(settings.getGuildID(), 1) //TODO: handle multiple calendars
                .flatMap(calData -> {
                    PreEvent event = new PreEvent(settings.getGuildID());
                    event.setSummary(summary);

                    events.add(event);

                    return CalendarWrapper.getCalendar(calData, settings)
                        .doOnNext(c -> event.setTimeZone(c.getTimeZone()))
                        .flatMap(c -> EventMessageFormatter.getPreEventEmbed(event, settings))
                        .flatMap(embed -> Messages.sendMessage(
                            Messages.getMessage("Creator.Event.Create.Init", settings), embed, e))
                        .doOnNext(event::setCreatorMessage)
                        .thenReturn(event);
                });
        }
        return Mono.justOrEmpty(getPreEvent(settings.getGuildID()));
    }

    //Copy event
    public Mono<PreEvent> init(MessageCreateEvent e, String eventId, GuildSettings settings) {
        if (!hasPreEvent(settings.getGuildID())) {
            return DatabaseManager.getCalendar(settings.getGuildID(), 1) //TODO: handle multiple calendars
                .flatMap(calData -> EventWrapper.getEvent(calData, settings, eventId)
                    .flatMap(toCopy -> {
                        PreEvent event = new PreEvent(settings.getGuildID(), toCopy);

                        events.add(event);

                        return CalendarWrapper.getCalendar(calData, settings)
                            .doOnNext(c -> event.setTimeZone(c.getTimeZone()))
                            .flatMap(c -> EventMessageFormatter.getPreEventEmbed(event, settings))
                            .flatMap(embed -> Messages.sendMessage(
                                Messages.getMessage("Creator.Event.Copy.Init", settings), embed, e))
                            .doOnNext(event::setCreatorMessage)
                            .thenReturn(event);
                    }));
        }
        return Mono.justOrEmpty(getPreEvent(settings.getGuildID()));
    }

    public Mono<PreEvent> edit(MessageCreateEvent e, String eventId, GuildSettings settings) {
        if (!hasPreEvent(settings.getGuildID())) {
            return DatabaseManager.getCalendar(settings.getGuildID(), 1) //TODO: handle multiple calendars
                .flatMap(calData -> EventWrapper.getEvent(calData, settings, eventId)
                    .flatMap(toEdit -> {
                        PreEvent event = new PreEvent(settings.getGuildID(), toEdit);
                        event.setEditing(true);

                        events.add(event);

                        return CalendarWrapper.getCalendar(calData, settings)
                            .doOnNext(c -> event.setTimeZone(c.getTimeZone()))
                            .flatMap(c -> EventMessageFormatter.getPreEventEmbed(event, settings))
                            .flatMap(embed -> Messages.sendMessage(
                                Messages.getMessage("Creator.Event.Edit.Init", settings), embed, e))
                            .doOnNext(event::setCreatorMessage)
                            .thenReturn(event);
                    }));
        }
        return Mono.justOrEmpty(getPreEvent(settings.getGuildID()));
    }

    public void terminate(Snowflake guildId) {
        events.remove(getPreEvent(guildId));
    }

    public Mono<EventCreatorResponse> confirmEvent(GuildSettings settings) {
        return Mono.justOrEmpty(getPreEvent(settings.getGuildID()))
            .filter(PreEvent::hasRequiredValues)
            .flatMap(pre ->
                DatabaseManager.getCalendar(settings.getGuildID(), 1) //TODO: Add multi-cal support
                    .flatMap(calData -> {
                        Event event = new Event();
                        event.setSummary(pre.getSummary());
                        event.setDescription(pre.getDescription());
                        event.setStart(pre.getStartDateTime().setTimeZone(pre.getTimeZone()));
                        event.setEnd(pre.getEndDateTime().setTimeZone(pre.getTimeZone()));
                        event.setVisibility("public");
                        if (!pre.getColor().equals(EventColor.NONE))
                            event.setColorId(String.valueOf(pre.getColor().getId()));

                        if (pre.getLocation() != null && !pre.getLocation().equalsIgnoreCase(""))
                            event.setLocation(pre.getLocation());

                        //Set recurrence
                        if (pre.shouldRecur()) {
                            String[] recurrence = new String[]{pre.getRecurrence().toRRule()};
                            event.setRecurrence(Arrays.asList(recurrence));
                        }

                        if (!pre.isEditing()) {
                            event.setId(KeyGenerator.generateEventId());

                            return EventWrapper.createEvent(calData, event, settings)
                                .flatMap(confirmed -> {
                                    EventCreatorResponse response = new EventCreatorResponse(true,
                                        confirmed, pre.getCreatorMessage(), false);

                                    EventData eventData = EventData.fromImage(
                                        settings.getGuildID(),
                                        confirmed.getId(),
                                        confirmed.getEnd().getDateTime().getValue(),
                                        pre.getEventData().getImageLink()
                                    );

                                    terminate(settings.getGuildID());

                                    return Mono.just(eventData)
                                        .filter(EventData::shouldBeSaved)
                                        .flatMap(DatabaseManager::updateEventData)
                                        .thenReturn(response);
                                }).defaultIfEmpty(new EventCreatorResponse(false, null,
                                    pre.getCreatorMessage(), false));
                        } else {
                            return EventWrapper.updateEvent(calData, event, settings)
                                .flatMap(confirmed -> {
                                    EventCreatorResponse response = new EventCreatorResponse(true,
                                        confirmed, pre.getCreatorMessage(), true);

                                    EventData eventData = EventData.fromImage(
                                        settings.getGuildID(),
                                        confirmed.getId(),
                                        confirmed.getEnd().getDateTime().getValue(),
                                        pre.getEventData().getImageLink()
                                    );

                                    terminate(settings.getGuildID());

                                    return Mono.just(eventData)
                                        .filter(EventData::shouldBeSaved)
                                        .flatMap(DatabaseManager::updateEventData)
                                        .thenReturn(response);
                                }).defaultIfEmpty(new EventCreatorResponse(false, null,
                                    pre.getCreatorMessage(), true));
                        }
                    })
            ).defaultIfEmpty(new EventCreatorResponse(false, null, null, false));
    }

    //Getters
    public PreEvent getPreEvent(Snowflake guildId) {
        for (PreEvent e : events) {
            if (e.getGuildId().equals(guildId)) {
                e.setLastEdit(System.currentTimeMillis());
                return e;
            }
        }
        return null;
    }

    public ArrayList<PreEvent> getAllPreEvents() {
        return events;
    }

    //Booleans/Checkers
    public boolean hasPreEvent(Snowflake guildId) {
        for (PreEvent e : events) {
            if (e.getGuildId().equals(guildId))
                return true;
        }
        return false;
    }
}