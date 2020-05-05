package org.dreamexposure.discal.client.module.command;

import org.dreamexposure.discal.client.message.EventMessageFormatter;
import org.dreamexposure.discal.client.message.Messages;
import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.command.CommandInfo;
import org.dreamexposure.discal.core.utils.GlobalConst;
import org.dreamexposure.discal.core.wrapper.google.EventWrapper;

import java.util.ArrayList;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Created by Nova Fox on 1/3/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class EventListCommand implements Command {
    /**
     * Gets the command this Object is responsible for.
     *
     * @return The command this Object is responsible for.
     */
    @Override
    public String getCommand() {
        return "events";
    }

    /**
     * Gets the short aliases of the command this object is responsible for.
     * </br>
     * This will return an empty ArrayList if none are present
     *
     * @return The aliases of the command.
     */
    @Override
    public ArrayList<String> getAliases() {
        return new ArrayList<>();
    }

    /**
     * Gets the info on the command (not sub command) to be used in help menus.
     *
     * @return The command info.
     */
    @Override
    public CommandInfo getCommandInfo() {
        CommandInfo info = new CommandInfo(
            "events",
            "Lists the specified amount of events from the guild calendar.",
            "!events (number or function) (other args if applicable)"
        );

        info.getSubCommands().put("search", "Searches for events based on specific criteria rather than just the next upcoming events");
        info.getSubCommands().put("today", "Lists events occurring today (max. 20)");
        return info;
    }

    /**
     * Issues the command this Object is responsible for.
     *
     * @param args  The command arguments.
     * @param event The event received.
     * @return <code>true</code> if successful, else <code>false</code>.
     */
    @Override
    public Mono<Void> issueCommand(String[] args, MessageCreateEvent event, GuildSettings settings) {
        return Mono.defer(() -> {
            if (args.length < 1) {
                return moduleSimpleList(args, event, settings);
            } else {
                switch (args[0].toLowerCase()) {
                    case "search":
                        if (settings.isDevGuild())
                            return moduleSearch(args, event, settings);
                        else
                            return Messages.sendMessage(Messages.getMessage("Notification.Disabled", settings), event);
                    case "today":
                        return moduleDay(args, event, settings);
                    default:
                        return moduleSimpleList(args, event, settings);
                }
            }
        }).then();
    }

    private Mono<Void> moduleSimpleList(String[] args, MessageCreateEvent event, GuildSettings settings) {
        return Mono.defer(() -> {
            if (args.length == 0) {
                return DatabaseManager.getMainCalendar(settings.getGuildID()) //TODO: support multi-cal
                    .flatMap(data -> EventWrapper.getEvents(data, settings, 1, System.currentTimeMillis())
                        .flatMap(events -> {
                            if (events.size() == 0) {
                                return Messages.sendMessage(Messages.getMessage("Event.List.Found.None", settings), event);
                            } else { //It will always be one here, so just use an else, there is no way it can be higher
                                return EventMessageFormatter
                                    .getEventEmbed(events.get(0), data.getCalendarNumber(), settings).flatMap(embed ->
                                        Messages.sendMessage(
                                            Messages.getMessage("Event.List.Found.One", settings), embed, event));
                            }
                        })
                    ).switchIfEmpty(Messages.sendMessage(Messages.getMessage("Creator.Calendar.NoCalendar", settings), event));
            } else if (args.length == 1) {
                return Mono.just(Integer.parseInt(args[0])).flatMap(count -> {
                    if (count > 15) {
                        return Messages.sendMessage(Messages.getMessage("Event.List.Amount.Over", settings), event);
                    } else if (count < 1) {
                        return Messages.sendMessage(Messages.getMessage("Event.List.Amount.Under", settings), event);
                    } else {
                        return DatabaseManager.getMainCalendar(settings.getGuildID()) //TODO: support multi-cal
                            .flatMap(data -> EventWrapper.getEvents(data, settings, count, System.currentTimeMillis())
                                .flatMap(events -> {
                                    if (events.size() == 0) {
                                        return Messages.sendMessage(Messages.getMessage("Event.List.Found.None", settings), event);
                                    } else if (events.size() == 1) {
                                        return EventMessageFormatter.getEventEmbed(events.get(0),
                                            data.getCalendarNumber(), settings).flatMap(embed -> Messages.sendMessage(
                                            Messages.getMessage("Event.List.Found.One", settings), embed, event));
                                    } else {
                                        return Messages.sendMessage(Messages.getMessage("Event.List.Found.Many", "%amount%",
                                            events.size() + "", settings), event).then(Flux.fromIterable(events)
                                            .flatMap(e -> EventMessageFormatter.getCondensedEventEmbed(e,
                                                data.getCalendarNumber(), settings).flatMap(embed ->
                                                Messages.sendMessage(embed, event)))
                                            .then()).thenReturn(GlobalConst.NOT_EMPTY);
                                    }
                                })
                            ).switchIfEmpty(Messages.sendMessage(Messages.getMessage("Creator.Calendar.NoCalendar", settings), event));
                    }
                }).onErrorResume(NumberFormatException.class, e ->
                    Messages.sendMessage(Messages.getMessage("Notification.Args.Value.Integer", settings), event));
            } else {
                return Messages.sendMessage(Messages.getMessage("Event.List.Args.Many", settings), event);
            }
        }).then();
    }

    private Mono<Void> moduleSearch(String[] args, MessageCreateEvent event, GuildSettings settings) {
        return Mono.empty(); //TODO: Actually make this...
    }

    private Mono<Void> moduleDay(String[] args, MessageCreateEvent event, GuildSettings settings) {
        return Mono.defer(() -> {
            if (args.length == 1) {
                return DatabaseManager.getMainCalendar(settings.getGuildID()) //TODO: Support multi-cal
                    .flatMap(data -> {
                        long now = System.currentTimeMillis();
                        long end = now + GlobalConst.oneDayMs;

                        return EventWrapper.getEvents(data, settings, 20, now, end).flatMap(events -> {
                            if (events.size() == 0) {
                                return Messages.sendMessage(Messages.getMessage("Event.List.Found.None", settings), event);
                            } else if (events.size() == 1) {
                                return EventMessageFormatter
                                    .getEventEmbed(events.get(0), data.getCalendarNumber(), settings).flatMap(embed ->
                                        Messages.sendMessage(
                                            Messages.getMessage("Event.List.Found.One", settings), embed, event));
                            } else {
                                return Messages.sendMessage(Messages.getMessage("Event.List.Found.Many", "%amount%",
                                    events.size() + "", settings), event).then(Flux.fromIterable(events).flatMap(e ->
                                    EventMessageFormatter.getCondensedEventEmbed(e, data.getCalendarNumber(), settings)
                                        .flatMap(embed -> Messages.sendMessage(embed, event))).then())
                                    .thenReturn(GlobalConst.NOT_EMPTY);
                            }
                        });
                    }).switchIfEmpty(Messages.sendMessage(Messages.getMessage("Creator.Calendar.NoCalendar", settings), event));
            } else {
                return Messages.sendMessage(Messages.getMessage("Event.List.Args.Many", settings), event);
            }
        }).then();
    }
}