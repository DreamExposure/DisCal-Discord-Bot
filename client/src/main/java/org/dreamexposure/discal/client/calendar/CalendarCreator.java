package org.dreamexposure.discal.client.calendar;

import com.google.api.services.calendar.model.AclRule;
import com.google.api.services.calendar.model.Calendar;

import org.dreamexposure.discal.client.message.CalendarMessageFormatter;
import org.dreamexposure.discal.client.message.Messages;
import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.calendar.CalendarCreatorResponse;
import org.dreamexposure.discal.core.object.calendar.CalendarData;
import org.dreamexposure.discal.core.object.calendar.PreCalendar;
import org.dreamexposure.discal.core.wrapper.google.AclRuleWrapper;
import org.dreamexposure.discal.core.wrapper.google.CalendarWrapper;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

/**
 * Created by Nova Fox on 1/4/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class CalendarCreator {
    static {
        instance = new CalendarCreator();
    }

    private final static CalendarCreator instance;
    private final List<PreCalendar> calendars = new CopyOnWriteArrayList<>();

    private CalendarCreator() {
    } //Prevent initialization

    public static CalendarCreator getCreator() {
        return instance;
    }

    //Functional
    public Mono<PreCalendar> init(MessageCreateEvent e, String calendarName, GuildSettings settings) {
        if (!hasPreCalendar(settings.getGuildID())) {
            return Mono.just(new PreCalendar(settings.getGuildID(), calendarName))
                .flatMap(calendar -> {
                        calendars.add(calendar);

                        return CalendarMessageFormatter.getPreCalendarEmbed(calendar, settings)
                            .flatMap(embed -> Messages.sendMessage(
                                Messages.getMessage("Creator.Calendar.Create.Init", settings),
                                embed, e)
                                .doOnNext(calendar::setCreatorMessage)
                                .then(Messages.deleteMessage(e))
                                .thenReturn(calendar));
                    }
                );
        }
        return Mono.justOrEmpty(getPreCalendar(settings.getGuildID()));
    }

    @Deprecated
    public Mono<PreCalendar> edit(MessageCreateEvent event, GuildSettings settings) {
        if (!hasPreCalendar(settings.getGuildID())) {
            return DatabaseManager.getMainCalendar(settings.getGuildID()).flatMap(data ->
                CalendarWrapper.getCalendar(data, settings)
                    .flatMap(calendar -> {
                        PreCalendar preCalendar = new PreCalendar(settings.getGuildID(), calendar);
                        preCalendar.setEditing(true);
                        preCalendar.setCalendarId(data.getCalendarAddress());

                        calendars.add(preCalendar);

                        return CalendarMessageFormatter.getPreCalendarEmbed(preCalendar, settings)
                            .flatMap(embed -> Messages.sendMessage(
                                Messages.getMessage("Creator.Calendar.Edit.Init", settings),
                                embed, event)
                                .doOnNext(preCalendar::setCreatorMessage)
                                .then(Messages.deleteMessage(event))
                                .thenReturn(preCalendar));
                    })
            );
        }
        return Mono.justOrEmpty(getPreCalendar(settings.getGuildID()));
    }

    public Mono<PreCalendar> edit(int calNumber, MessageCreateEvent event, GuildSettings settings) {
        if (!hasPreCalendar(settings.getGuildID())) {
            return DatabaseManager.getCalendar(settings.getGuildID(), calNumber).flatMap(data ->
                CalendarWrapper.getCalendar(data, settings)
                    .flatMap(calendar -> {
                        PreCalendar preCalendar = new PreCalendar(settings.getGuildID(), calendar);
                        preCalendar.setEditing(true);
                        preCalendar.setCalendarId(data.getCalendarAddress());

                        calendars.add(preCalendar);

                        return CalendarMessageFormatter.getPreCalendarEmbed(preCalendar, settings)
                            .flatMap(embed -> Messages.sendMessage(
                                Messages.getMessage("Creator.Calendar.Edit.Init", settings),
                                embed, event)
                                .doOnNext(preCalendar::setCreatorMessage)
                                .then(Messages.deleteMessage(event))
                                .thenReturn(preCalendar));
                    })
            );
        }
        return Mono.justOrEmpty(getPreCalendar(settings.getGuildID()));
    }

    public void terminate(Snowflake guildId) {
        calendars.remove(getPreCalendar(guildId));
    }

    public Mono<CalendarCreatorResponse> confirmCalendar(GuildSettings settings) {
        return Mono.justOrEmpty(getPreCalendar(settings.getGuildID()))
            .filter(PreCalendar::hasRequiredValues)
            .flatMap(pre -> {
                Calendar calendar = new Calendar();
                calendar.setSummary(pre.getSummary());
                calendar.setDescription(pre.getDescription());
                calendar.setTimeZone(pre.getTimezone());

                if (!pre.isEditing()) {
                    return CalendarWrapper.createCalendar(calendar, settings)
                        .flatMap(confirmed -> {
                            CalendarData data = CalendarData.fromData(
                                settings.getGuildID(),
                                1, //TODO: Support multi-calendar
                                confirmed.getId(),
                                confirmed.getId(),
                                false);

                            AclRule rule = new AclRule()
                                .setScope(new AclRule.Scope().setType("default"))
                                .setRole("reader");

                            CalendarCreatorResponse response = new CalendarCreatorResponse(true,
                                confirmed, pre.getCreatorMessage(), false);

                            return Mono.when(
                                DatabaseManager.updateCalendar(data),
                                AclRuleWrapper.insertRule(rule, data.getCalendarId(), settings)
                            )
                                .then(Mono.fromRunnable(() -> terminate(settings.getGuildID())))
                                .thenReturn(response);
                        }).defaultIfEmpty(new CalendarCreatorResponse(false, null, pre.getCreatorMessage(), false));
                } else {
                    //Editing calendar...
                    calendar.setId(pre.getCalendarId());
                    return CalendarWrapper.updateCalendar(calendar, settings)
                        .flatMap(confirmed -> {
                            AclRule rule = new AclRule()
                                .setScope(new AclRule.Scope().setType("default"))
                                .setRole("reader");

                            CalendarCreatorResponse response = new CalendarCreatorResponse(true,
                                confirmed, pre.getCreatorMessage(), true);

                            return AclRuleWrapper.insertRule(rule, confirmed.getId(), settings)
                                .doOnNext(a -> terminate(settings.getGuildID()))
                                .thenReturn(response);
                        }).defaultIfEmpty(new CalendarCreatorResponse(false, null, pre.getCreatorMessage(), true));
                }
            }).defaultIfEmpty(new CalendarCreatorResponse(false, null, null, false));
    }

    //Getters
    public PreCalendar getPreCalendar(Snowflake guildId) {
        for (PreCalendar c : calendars) {
            if (c.getGuildId().equals(guildId)) {
                c.setLastEdit(System.currentTimeMillis());
                return c;
            }
        }
        return null;
    }

    public List<PreCalendar> getAllPreCalendars() {
        return calendars;
    }

    //Booleans/Checkers
    public boolean hasPreCalendar(Snowflake guildId) {
        for (PreCalendar c : calendars) {
            if (c.getGuildId().equals(guildId))
                return true;
        }
        return false;
    }
}