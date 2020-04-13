package org.dreamexposure.discal.client.module.command;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import org.dreamexposure.discal.client.message.EventMessageFormatter;
import org.dreamexposure.discal.client.message.MessageManager;
import org.dreamexposure.discal.core.calendar.CalendarAuth;
import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.logger.LogFeed;
import org.dreamexposure.discal.core.logger.object.LogObject;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.calendar.CalendarData;
import org.dreamexposure.discal.core.object.command.CommandInfo;

import java.util.ArrayList;
import java.util.List;

import discord4j.core.event.domain.message.MessageCreateEvent;

/**
 * Created by Nova Fox on 1/3/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
@SuppressWarnings({"ConstantConditions"})
public class EventListCommand implements ICommand {
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
    public boolean issueCommand(String[] args, MessageCreateEvent event, GuildSettings settings) {
        //Get events from calendar
        if (args.length < 1) {
            moduleSimpleList(args, event, settings);
        } else {
            switch (args[0].toLowerCase()) {
                case "search":
                    if (settings.isDevGuild())
                        moduleSearch(args, event, settings);
                    else
                        MessageManager.sendMessageAsync(MessageManager.getMessage("Notification.Disabled", settings), event);
                    break;
                case "today":
                    if (settings.isDevGuild())
                        moduleDay(args, event, settings);
                    else
                        MessageManager.sendMessageAsync(MessageManager.getMessage("Notification.Disabled", settings), event);
                    break;
                default:
                    moduleSimpleList(args, event, settings);
                    break;
            }
        }
        return false;
    }

    @SuppressWarnings("Duplicates")
    private void moduleSimpleList(String[] args, MessageCreateEvent event, GuildSettings settings) {
        if (args.length == 0) {
            try {
                Calendar service = CalendarAuth.getCalendarService(settings);

                DateTime now = new DateTime(System.currentTimeMillis());
                CalendarData calendarData = DatabaseManager.getMainCalendar(settings.getGuildID()).block();
                Events events = service.events().list(calendarData.getCalendarAddress())
                        .setMaxResults(1)
                        .setTimeMin(now)
                        .setOrderBy("startTime")
                        .setSingleEvents(true)
                        .setShowDeleted(false)
                        .execute();
                List<Event> items = events.getItems();
                if (items.size() == 0) {
                    MessageManager.sendMessageAsync(MessageManager.getMessage("Event.List.Found.None", settings), event);
                } else if (items.size() == 1) {
                    MessageManager.sendMessageAsync(MessageManager.getMessage("Event.List.Found.One", settings), EventMessageFormatter.getEventEmbed(items.get(0), settings), event);
                }
            } catch (Exception e) {
                MessageManager.sendMessageAsync(MessageManager.getMessage("Notification.Error.Unknown", settings), event);
                LogFeed.log(LogObject.forException("Failed to list events", e, this.getClass()));
            }
        } else if (args.length == 1) {
            try {
                int eventNum = Integer.parseInt(args[0]);
                if (eventNum > 15) {
                    MessageManager.sendMessageAsync(MessageManager.getMessage("Event.List.Amount.Over", settings), event);
                    return;
                }
                if (eventNum < 1) {
                    MessageManager.sendMessageAsync(MessageManager.getMessage("Event.List.Amount.Under", settings), event);
                    return;
                }
                try {
                    Calendar service = CalendarAuth.getCalendarService(settings);

                    DateTime now = new DateTime(System.currentTimeMillis());
                    CalendarData calendarData = DatabaseManager.getMainCalendar(settings.getGuildID()).block();
                    Events events = service.events().list(calendarData.getCalendarAddress())
                            .setMaxResults(eventNum)
                            .setTimeMin(now)
                            .setOrderBy("startTime")
                            .setSingleEvents(true)
                            .execute();
                    List<Event> items = events.getItems();
                    if (items.size() == 0) {
                        MessageManager.sendMessageAsync(MessageManager.getMessage("Event.List.Found.None", settings), event);
                    } else if (items.size() == 1) {
                        MessageManager.sendMessageAsync(MessageManager.getMessage("Event.List.Found.One", settings), EventMessageFormatter.getEventEmbed(items.get(0), settings), event);
                    } else {
                        //List events by Id only.
                        MessageManager.sendMessageAsync(MessageManager.getMessage("Event.List.Found.Many", "%amount%", items.size() + "", settings), event);
                        for (Event e : items) {
                            MessageManager.sendMessageAsync(EventMessageFormatter.getCondensedEventEmbed(e, settings), event);
                        }
                    }
                } catch (Exception e) {
                    MessageManager.sendMessageAsync(MessageManager.getMessage("Notification.Error.Unknown", settings), event);
                    LogFeed.log(LogObject.forException("Failed to list events", e, this.getClass()));
                }
            } catch (NumberFormatException e) {
                MessageManager.sendMessageAsync(MessageManager.getMessage("Notification.Args.Value.Integer", settings), event);
            }
        } else {
            MessageManager.sendMessageAsync(MessageManager.getMessage("Event.List.Args.Many", settings), event);
        }
    }

    private void moduleSearch(String[] args, MessageCreateEvent event, GuildSettings settings) {

    }

    @SuppressWarnings("Duplicates")
    private void moduleDay(String[] args, MessageCreateEvent event, GuildSettings settings) {
        if (args.length == 1) {
            //Get the upcoming events in the next 24 hours.
            try {
                Calendar service = CalendarAuth.getCalendarService(settings);

                DateTime now = new DateTime(System.currentTimeMillis());
                DateTime twentyFourHoursFromNow = new DateTime(now.getValue() + 86400000L);
                CalendarData calendarData = DatabaseManager.getMainCalendar(settings.getGuildID()).block();
                Events events = service.events().list(calendarData.getCalendarAddress())
                        .setMaxResults(20)
                        .setTimeMin(now)
                        .setTimeMax(twentyFourHoursFromNow)
                        .setOrderBy("startTime")
                        .setSingleEvents(true)
                        .setShowDeleted(false)
                        .execute();
                List<Event> items = events.getItems();
                if (items.size() == 0) {
                    MessageManager.sendMessageAsync(MessageManager.getMessage("Event.List.Found.None", settings), event);
                } else if (items.size() == 1) {
                    MessageManager.sendMessageAsync(MessageManager.getMessage("Event.List.Found.One", settings), EventMessageFormatter.getEventEmbed(items.get(0), settings), event);
                } else {
                    //List events by Id only.
                    MessageManager.sendMessageAsync(MessageManager.getMessage("Event.List.Found.Many", "%amount%", items.size() + "", settings), event);
                    for (Event e : items) {
                        MessageManager.sendMessageAsync(EventMessageFormatter.getCondensedEventEmbed(e, settings), event);
                    }
                }
            } catch (Exception e) {
                MessageManager.sendMessageAsync(MessageManager.getMessage("Notification.Error.Unknown", settings), event);
                LogFeed.log(LogObject.forException("Failed to list events", e, this.getClass()));
            }
        }
    }
}