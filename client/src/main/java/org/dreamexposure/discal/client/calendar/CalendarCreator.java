package org.dreamexposure.discal.client.calendar;

import com.google.api.services.calendar.model.AclRule;
import com.google.api.services.calendar.model.Calendar;

import org.dreamexposure.discal.client.message.CalendarMessageFormatter;
import org.dreamexposure.discal.client.message.Messages;
import org.dreamexposure.discal.core.calendar.CalendarAuth;
import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.calendar.CalendarCreatorResponse;
import org.dreamexposure.discal.core.object.calendar.CalendarData;
import org.dreamexposure.discal.core.object.calendar.PreCalendar;
import org.dreamexposure.discal.core.wrapper.google.AclRuleWrapper;
import org.dreamexposure.discal.core.wrapper.google.CalendarWrapper;

import java.util.List;
import java.util.Random;
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
    public Mono<PreCalendar> init(final MessageCreateEvent e, final String calendarName, final GuildSettings settings) {
        if (!this.hasPreCalendar(settings.getGuildID())) {
            return Mono.just(new PreCalendar(settings.getGuildID(), calendarName))
                .flatMap(calendar -> {
                        this.calendars.add(calendar);

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
        return Mono.justOrEmpty(this.getPreCalendar(settings.getGuildID()));
    }

    @Deprecated
    public Mono<PreCalendar> edit(final MessageCreateEvent event, final GuildSettings settings) {
        if (!this.hasPreCalendar(settings.getGuildID())) {
            return DatabaseManager.getMainCalendar(settings.getGuildID()).flatMap(data ->
                CalendarWrapper.getCalendar(data, settings)
                    .flatMap(calendar -> {
                        final PreCalendar preCalendar = new PreCalendar(settings.getGuildID(), calendar);
                        preCalendar.setEditing(true);
                        preCalendar.setCalendarId(data.getCalendarAddress());
                        preCalendar.setCalendarData(data);

                        this.calendars.add(preCalendar);

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
        return Mono.justOrEmpty(this.getPreCalendar(settings.getGuildID()));
    }

    public Mono<PreCalendar> edit(final int calNumber, final MessageCreateEvent event, final GuildSettings settings) {
        if (!this.hasPreCalendar(settings.getGuildID())) {
            return DatabaseManager.getCalendar(settings.getGuildID(), calNumber).flatMap(data ->
                CalendarWrapper.getCalendar(data, settings)
                    .flatMap(calendar -> {
                        final PreCalendar preCalendar = new PreCalendar(settings.getGuildID(), calendar);
                        preCalendar.setEditing(true);
                        preCalendar.setCalendarId(data.getCalendarAddress());
                        preCalendar.setCalendarData(data);

                        this.calendars.add(preCalendar);

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
        return Mono.justOrEmpty(this.getPreCalendar(settings.getGuildID()));
    }

    public void terminate(final Snowflake guildId) {
        this.calendars.remove(this.getPreCalendar(guildId));
    }

    public Mono<CalendarCreatorResponse> confirmCalendar(final GuildSettings settings) {
        return Mono.justOrEmpty(this.getPreCalendar(settings.getGuildID()))
            .filter(PreCalendar::hasRequiredValues)
            .flatMap(pre -> {
                final Calendar calendar = new Calendar();
                calendar.setSummary(pre.getSummary());
                calendar.setDescription(pre.getDescription());
                calendar.setTimeZone(pre.getTimezone());

                //Randomly determine what credentials account the calendar will be assigned to.
                // If creation fails, and the user retries, it will pick another random credential to use.
                final int credId = new Random().nextInt(CalendarAuth.credentialsCount());

                if (!pre.isEditing()) {
                    return CalendarWrapper.createCalendar(calendar, credId)
                        .flatMap(confirmed -> {
                            final CalendarData data = CalendarData.fromData(
                                settings.getGuildID(),
                                1, //TODO: Support multi-calendar
                                confirmed.getId(),
                                confirmed.getId(),
                                false,
                                credId);

                            final AclRule rule = new AclRule()
                                .setScope(new AclRule.Scope().setType("default"))
                                .setRole("reader");

                            final CalendarCreatorResponse response = new CalendarCreatorResponse(true,
                                confirmed, pre.getCreatorMessage(), false);

                            return Mono.when(
                                DatabaseManager.updateCalendar(data),
                                AclRuleWrapper.insertRule(rule, data, settings)
                            )
                                .then(Mono.fromRunnable(() -> this.terminate(settings.getGuildID())))
                                .thenReturn(response);
                        }).defaultIfEmpty(new CalendarCreatorResponse(false, null, pre.getCreatorMessage(), false));
                } else {
                    //Editing calendar...
                    calendar.setId(pre.getCalendarId());
                    return CalendarWrapper.updateCalendar(calendar, settings, pre.getCalendarData())
                        .flatMap(confirmed -> {
                            final AclRule rule = new AclRule()
                                .setScope(new AclRule.Scope().setType("default"))
                                .setRole("reader");

                            final CalendarCreatorResponse response = new CalendarCreatorResponse(true,
                                confirmed, pre.getCreatorMessage(), true);

                            return AclRuleWrapper.insertRule(rule, pre.getCalendarData(), settings)
                                .doOnNext(a -> this.terminate(settings.getGuildID()))
                                .thenReturn(response);
                        }).defaultIfEmpty(new CalendarCreatorResponse(false, null, pre.getCreatorMessage(), true));
                }
            }).defaultIfEmpty(new CalendarCreatorResponse(false, null, null, false));
    }

    //Getters
    public PreCalendar getPreCalendar(final Snowflake guildId) {
        for (final PreCalendar c : this.calendars) {
            if (c.getGuildId().equals(guildId)) {
                c.setLastEdit(System.currentTimeMillis());
                return c;
            }
        }
        return null;
    }

    public List<PreCalendar> getAllPreCalendars() {
        return this.calendars;
    }

    //Booleans/Checkers
    public boolean hasPreCalendar(final Snowflake guildId) {
        for (final PreCalendar c : this.calendars) {
            if (c.getGuildId().equals(guildId))
                return true;
        }
        return false;
    }
}