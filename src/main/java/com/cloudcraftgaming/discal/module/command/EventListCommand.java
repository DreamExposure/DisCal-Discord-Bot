package com.cloudcraftgaming.discal.module.command;

import com.cloudcraftgaming.discal.database.DatabaseManager;
import com.cloudcraftgaming.discal.internal.calendar.CalendarAuth;
import com.cloudcraftgaming.discal.internal.calendar.event.EventMessageFormatter;
import com.cloudcraftgaming.discal.internal.data.CalendarData;
import com.cloudcraftgaming.discal.internal.email.EmailSender;
import com.cloudcraftgaming.discal.module.command.info.CommandInfo;
import com.cloudcraftgaming.discal.utils.Message;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nova Fox on 1/3/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class EventListCommand implements ICommand {
    /**
     * Gets the command this Object is responsible for.
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
        CommandInfo info = new CommandInfo("events");
        info.setDescription("Lists the specified amount of events from the guild calendar.");
        info.setExample("!events (number)");
        return info;
    }

    /**
     * Issues the command this Object is responsible for.
     * @param args The command arguments.
     * @param event The event received.
     * @param client The Client associated with the Bot.
     * @return <code>true</code> if successful, else <code>false</code>.
     */
    @Override
    public Boolean issueCommand(String[] args, MessageReceivedEvent event, IDiscordClient client) {
        //Get events from calendar
        if (args.length < 1) {
            Message.sendMessage("Please specify how many events to list with `!events <amount>`", event, client);
        } else {
            try {
                Integer eventNum = Integer.valueOf(args[0]);
                if (eventNum > 16) {
                    Message.sendMessage("You cannot list more than 15 events!", event, client);
                    return false;
                }
                try {
                    Calendar service = CalendarAuth.getCalendarService();
                    DateTime now = new DateTime(System.currentTimeMillis());
                    CalendarData calendarData = DatabaseManager.getManager().getMainCalendar(event.getMessage().getGuild().getID());
                    Events events = service.events().list(calendarData.getCalendarAddress())
                            .setMaxResults(eventNum)
                            .setTimeMin(now)
                            .setOrderBy("startTime")
                            .setSingleEvents(true)
                            .execute();
                    List<Event> items = events.getItems();
                    if (items.size() == 0) {
                        Message.sendMessage("No upcoming events found.", event, client);
                        return true;
                    } else if (items.size() == 1) {
                        String guildId = event.getMessage().getGuild().getID();
                        Message.sendMessage(EventMessageFormatter.getEventEmbed(items.get(0), guildId), "1 upcoming event found:", event, client);
                    } else {
                        //List events by Id only.
                        Message.sendMessage(items.size() + " upcoming events found... Please note that this list may be delayed due to rate limiting...", event, client);
                        for (Event e : items) {
                            Message.sendMessage(EventMessageFormatter.getCondensedEventEmbed(e), event, client);
                        }
                        Message.sendMessage("Use `!event view <id>` for more info.", event, client);
                        return true;
                    }
                } catch (IOException e) {
                    Message.sendMessage("Oops! Something terrible happened! I have emailed the developer!", event, client);
                    EmailSender.getSender().sendExceptionEmail(e, this.getClass());
                    e.printStackTrace();
                }
            } catch (NumberFormatException e) {
                Message.sendMessage("Event amount must be an Integer!", event, client);
            }
        }
        return false;
    }
}