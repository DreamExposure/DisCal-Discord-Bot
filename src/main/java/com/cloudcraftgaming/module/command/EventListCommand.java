package com.cloudcraftgaming.module.command;

import com.cloudcraftgaming.database.DatabaseManager;
import com.cloudcraftgaming.internal.calendar.CalendarAuth;
import com.cloudcraftgaming.internal.calendar.event.EventMessageFormatter;
import com.cloudcraftgaming.utils.Message;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;

import java.io.IOException;
import java.util.List;

/**
 * Created by Nova Fox on 1/3/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class EventListCommand implements ICommand {

    @Override
    public String getCommand() {
        return "events";
    }

    @Override
    public Boolean issueCommand(String[] args, MessageReceivedEvent event, IDiscordClient client) {
        //Get events from calendar
        if (args.length < 1) {
            Message.sendMessage("Please specify how many events to list with '!events (amount)'...", event, client);
        } else {
            try {
                Integer eventNum = Integer.valueOf(args[0]);
                try {
                    Calendar service = CalendarAuth.getCalendarService();
                    DateTime now = new DateTime(System.currentTimeMillis());
                    String calendarAddress = DatabaseManager.getManager().getData(event.getMessage().getGuild().getID()).getCalendarAddress();
                    Events events = service.events().list(calendarAddress)
                            .setMaxResults(eventNum)
                            .setTimeMin(now)
                            .setOrderBy("startTime")
                            .setSingleEvents(true)
                            .execute();
                    List<Event> items = events.getItems();
                    if (items.size() == 0) {
                        Message.sendMessage("No upcoming events found.", event, client);
                        return true;
                    } else {
                     //List events.
                        Message.sendMessage("Upcoming events: ", event, client);
                        for (Event e : items) {
                            Message.sendMessage(EventMessageFormatter.getFormatEventMessage(e), event, client);
                        }
                        return true;
                    }
                } catch (IOException e) {
                    Message.sendMessage("Oops! Something terrible happened! Please report this to the NovaFox161 (developer).", event, client);
                    e.printStackTrace();
                }

            } catch (NumberFormatException e) {
                Message.sendMessage("Event amount must be an Integer!", event, client);
            }
        }
        return false;
    }
}