package com.cloudcraftgaming.discal.module.command;

import com.cloudcraftgaming.discal.database.DatabaseManager;
import com.cloudcraftgaming.discal.internal.calendar.CalendarAuth;
import com.cloudcraftgaming.discal.internal.calendar.event.EventMessageFormatter;
import com.cloudcraftgaming.discal.internal.data.CalendarData;
import com.cloudcraftgaming.discal.internal.data.GuildSettings;
import com.cloudcraftgaming.discal.module.command.info.CommandInfo;
import com.cloudcraftgaming.discal.utils.ExceptionHandler;
import com.cloudcraftgaming.discal.utils.Message;
import com.cloudcraftgaming.discal.utils.MessageManager;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
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
        info.setExample("!events (number or function) (other args if applicable)");

        info.getSubCommands().add("search");
        return info;
    }

    /**
     * Issues the command this Object is responsible for.
     * @param args The command arguments.
     * @param event The event received.
     * @return <code>true</code> if successful, else <code>false</code>.
     */
    @Override
    public Boolean issueCommand(String[] args, MessageReceivedEvent event)  {
        //Get events from calendar
        if (args.length < 1) {
            Message.sendMessage(MessageManager.getMessage("Event.List.Args.Few", event), event);
        } else {
            GuildSettings settings = DatabaseManager.getManager().getSettings(event.getMessage().getGuild().getID());
            switch (args[0].toLowerCase()) {
                case "search":
                    if (settings.isDevGuild()) {
                        //To search module.
                        Message.sendMessage("Uh... I'm working on it okay~", event);
                    } else {
                        Message.sendMessage(MessageManager.getMessage("Notification.Disabled", event), event);
                    }
                    break;
                default:
                    moduleSimpleList(args, event);
                    break;
            }
        }
        return false;
    }

    private void moduleSimpleList(String[] args, MessageReceivedEvent event) {
        if (args.length == 1) {
            try {
                Integer eventNum = Integer.valueOf(args[0]);
                if (eventNum > 15) {
                    Message.sendMessage(MessageManager.getMessage("Event.List.Amount.Over", event), event);
                    return;
                }
                if (eventNum < 1) {
                    Message.sendMessage(MessageManager.getMessage("Event.List.Amount.Under", event), event);
                    return;
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
                        Message.sendMessage(MessageManager.getMessage("Event.List.Found.None", event), event);
                    } else if (items.size() == 1) {
                        String guildId = event.getMessage().getGuild().getID();
                        Message.sendMessage(EventMessageFormatter.getEventEmbed(items.get(0), guildId), MessageManager.getMessage("Event.List.Found.One", event), event);
                    } else {
                        //List events by Id only.
                        Message.sendMessage(MessageManager.getMessage("Event.List.Found.Many", "%amount%", items.size() + "", event), event);
                        for (Event e : items) {
                            Message.sendMessage(EventMessageFormatter.getCondensedEventEmbed(e, event.getMessage().getGuild().getID()), event);
                        }
                    }
                } catch (IOException e) {
                    Message.sendMessage(MessageManager.getMessage("Notification.Error.Unknown", event), event);
                    ExceptionHandler.sendException(event.getMessage().getAuthor(), "Failed to list events.", e, this.getClass());
                    e.printStackTrace();
                }
            } catch (NumberFormatException e) {
                Message.sendMessage(MessageManager.getMessage("Notification.Args.Value.Integer", event), event);
            }
        } else {
            Message.sendMessage(MessageManager.getMessage("Event.List.Args.Few", event), event);
        }
    }
}