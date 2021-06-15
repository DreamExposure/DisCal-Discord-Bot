package org.dreamexposure.discal.client.event;

import com.google.api.services.calendar.model.Event;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
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
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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
    private final List<PreEvent> events = new CopyOnWriteArrayList<>();

    private EventCreator() {
    } //Prevent initialization.

    public static EventCreator getCreator() {
        return instance;
    }

    //Functional
    public Mono<PreEvent> init(final MessageCreateEvent e, final GuildSettings settings) {
        if (!this.hasPreEvent(settings.getGuildID())) {
            return DatabaseManager.INSTANCE.getCalendar(settings.getGuildID(), 1) //TODO: handle multiple calendars
                .flatMap(calData -> {
                    final PreEvent event = new PreEvent(settings.getGuildID(), calData.getCalendarNumber());
                    this.events.add(event);

                    return CalendarWrapper.getCalendar(calData)
                        .doOnNext(c -> event.setTimezone(c.getTimeZone()))
                        .flatMap(c -> EventMessageFormatter.getPreEventEmbed(event, settings))
                        .flatMap(embed -> Messages.sendMessage(
                            Messages.getMessage("Creator.Event.Create.Init", settings), embed, e))
                        .doOnNext(event::setCreatorMessage)
                        .then(Messages.deleteMessage(e))
                        .thenReturn(event);
                });
        }
        return Mono.justOrEmpty(this.getPreEvent(settings.getGuildID()));
    }

    public Mono<PreEvent> init(final MessageCreateEvent e, final GuildSettings settings, final String summary) {
        if (!this.hasPreEvent(settings.getGuildID())) {
            return DatabaseManager.INSTANCE.getCalendar(settings.getGuildID(), 1) //TODO: handle multiple calendars
                .flatMap(calData -> {
                    final PreEvent event = new PreEvent(settings.getGuildID(), calData.getCalendarNumber());
                    event.setSummary(summary);

                    this.events.add(event);

                    return CalendarWrapper.getCalendar(calData)
                        .doOnNext(c -> event.setTimezone(c.getTimeZone()))
                        .flatMap(c -> EventMessageFormatter.getPreEventEmbed(event, settings))
                        .flatMap(embed -> Messages.sendMessage(
                            Messages.getMessage("Creator.Event.Create.Init", settings), embed, e))
                        .doOnNext(event::setCreatorMessage)
                        .then(Messages.deleteMessage(e))
                        .thenReturn(event);
                });
        }
        return Mono.justOrEmpty(this.getPreEvent(settings.getGuildID()));
    }

    //Copy event
    public Mono<PreEvent> init(final MessageCreateEvent e, final String eventId, final GuildSettings settings) {
        if (!this.hasPreEvent(settings.getGuildID())) {
            return DatabaseManager.INSTANCE.getCalendar(settings.getGuildID(), 1) //TODO: handle multiple calendars
                .flatMap(calData -> EventWrapper.getEvent(calData, eventId)
                    .flatMap(toCopy -> PreEvent.copy(settings.getGuildID(), toCopy, calData))
                    .flatMap(event -> {
                        this.events.add(event);

                        return CalendarWrapper.getCalendar(calData)
                            .doOnNext(c -> event.setTimezone(c.getTimeZone()))
                            .flatMap(c -> EventMessageFormatter.getPreEventEmbed(event, settings))
                            .flatMap(embed -> Messages.sendMessage(
                                Messages.getMessage("Creator.Event.Copy.Init", settings), embed, e))
                            .doOnNext(event::setCreatorMessage)
                            .then(Messages.deleteMessage(e))
                            .thenReturn(event);
                    }));
        }
        return Mono.justOrEmpty(this.getPreEvent(settings.getGuildID()));
    }

    public Mono<PreEvent> edit(final MessageCreateEvent e, final String eventId, final GuildSettings settings) {
        if (!this.hasPreEvent(settings.getGuildID())) {
            return DatabaseManager.INSTANCE.getCalendar(settings.getGuildID(), 1) //TODO: handle multiple calendars
                .flatMap(calData -> EventWrapper.getEvent(calData, eventId)
                    .flatMap(toEdit -> PreEvent.copy(settings.getGuildID(), toEdit, calData))
                    .flatMap(event -> {
                        event.setEditing(true);

                        this.events.add(event);

                        return CalendarWrapper.getCalendar(calData)
                            .doOnNext(c -> event.setTimezone(c.getTimeZone()))
                            .flatMap(c -> EventMessageFormatter.getPreEventEmbed(event, settings))
                            .flatMap(embed -> Messages.sendMessage(
                                Messages.getMessage("Creator.Event.Edit.Init", settings), embed, e))
                            .doOnNext(event::setCreatorMessage)
                            .then(Messages.deleteMessage(e))
                            .thenReturn(event);
                    }));
        }
        return Mono.justOrEmpty(this.getPreEvent(settings.getGuildID()));
    }

    public void terminate(final Snowflake guildId) {
        this.events.remove(this.getPreEvent(guildId));
    }

    public Mono<EventCreatorResponse> confirmEvent(final GuildSettings settings) {
        return Mono.justOrEmpty(this.getPreEvent(settings.getGuildID()))
            .filter(PreEvent::hasRequiredValues)
            .flatMap(pre ->
                DatabaseManager.INSTANCE.getCalendar(settings.getGuildID(), 1) //TODO: Add multi-cal support
                    .flatMap(calData -> {
                        final Event event = new Event();
                        event.setSummary(pre.getSummary());
                        event.setDescription(pre.getDescription());
                        event.setStart(pre.getStartDateTime().setTimeZone(pre.getTimezone()));
                        event.setEnd(pre.getEndDateTime().setTimeZone(pre.getTimezone()));
                        event.setVisibility("public");
                        if (!pre.getColor().equals(EventColor.NONE))
                            event.setColorId(String.valueOf(pre.getColor().getId()));

                        if (pre.getLocation() != null && !"".equalsIgnoreCase(pre.getLocation()))
                            event.setLocation(pre.getLocation());

                        //Set recurrence
                        if (pre.getRecur())
                            event.setRecurrence(Collections.singletonList(pre.getRecurrence().toRRule()));

                        if (!pre.getEditing()) {
                            event.setId(KeyGenerator.generateEventId());

                            return EventWrapper.createEvent(calData, event)
                                .flatMap(confirmed -> {
                                    final EventCreatorResponse response = new EventCreatorResponse(true,
                                        confirmed, pre.getCreatorMessage(), false);

                                    EventData eventData = this.handleEventData(pre, confirmed, settings);

                                    this.terminate(settings.getGuildID());

                                    return Mono.just(eventData)
                                        .filter(EventData::shouldBeSaved)
                                        .flatMap(DatabaseManager.INSTANCE::updateEventData)
                                        .thenReturn(response);
                                }).defaultIfEmpty(new EventCreatorResponse(false, null,
                                    pre.getCreatorMessage(), false));
                        } else {
                            event.setId(pre.getEventId());

                            return EventWrapper.updateEvent(calData, event)
                                .flatMap(confirmed -> {
                                    final EventCreatorResponse response = new EventCreatorResponse(true,
                                        confirmed, pre.getCreatorMessage(), true);

                                    EventData eventData = this.handleEventData(pre, confirmed, settings);

                                    this.terminate(settings.getGuildID());

                                    return Mono.just(eventData)
                                        .filter(EventData::shouldBeSaved)
                                        .flatMap(DatabaseManager.INSTANCE::updateEventData)
                                        .thenReturn(response);
                                }).defaultIfEmpty(new EventCreatorResponse(false, null,
                                    pre.getCreatorMessage(), true));
                        }
                    })
            ).defaultIfEmpty(new EventCreatorResponse(false, null, null, false));
    }

    //Getters
    public PreEvent getPreEvent(final Snowflake guildId) {
        for (final PreEvent e : this.events) {
            if (e.getGuildId().equals(guildId)) {
                e.setLastEdit(System.currentTimeMillis());
                return e;
            }
        }
        return null;
    }

    public List<PreEvent> getAllPreEvents() {
        return this.events;
    }

    //Booleans/Checkers
    public boolean hasPreEvent(final Snowflake guildId) {
        for (final PreEvent e : this.events) {
            if (e.getGuildId().equals(guildId))
                return true;
        }
        return false;
    }


    private EventData handleEventData(PreEvent pre, Event confirmed, GuildSettings settings) {
        try {
            if (pre.getEventData() != null && pre.getEventData().shouldBeSaved()) {

                return new EventData(
                    settings.getGuildID(),
                    confirmed.getId(),
                    confirmed.getEnd().getDateTime().getValue(),
                    pre.getEventData().getImageLink()
                );
            } else {
                return new EventData();
            }
        } catch (NullPointerException ignore) {
            return new EventData();
        }
    }
}
