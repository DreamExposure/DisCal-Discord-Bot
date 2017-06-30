package com.cloudcraftgaming.discal.module.command;

import com.cloudcraftgaming.discal.database.DatabaseManager;
import com.cloudcraftgaming.discal.internal.calendar.CalendarAuth;
import com.cloudcraftgaming.discal.internal.calendar.calendar.CalendarMessageFormatter;
import com.cloudcraftgaming.discal.internal.data.CalendarData;
import com.cloudcraftgaming.discal.internal.data.GuildSettings;
import com.cloudcraftgaming.discal.module.command.info.CommandInfo;
import com.cloudcraftgaming.discal.utils.ExceptionHandler;
import com.cloudcraftgaming.discal.utils.Message;
import com.cloudcraftgaming.discal.utils.MessageManager;
import com.google.api.services.calendar.model.Calendar;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

import java.util.ArrayList;

/**
 * Created by Nova Fox on 1/3/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class LinkCalendarCommand implements ICommand {
    /**
     * Gets the command this Object is responsible for.
     * @return The command this Object is responsible for.
     */
    @Override
    public String getCommand() {
        return "LinkCalendar";
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
        ArrayList<String> aliases = new ArrayList<>();
        aliases.add("linkcal");
        aliases.add("calendarlink");
        aliases.add("callink");
        aliases.add("linkcallador");
        return aliases;
    }

    /**
     * Gets the info on the command (not sub command) to be used in help menus.
     *
     * @return The command info.
     */
    @Override
    public CommandInfo getCommandInfo() {
        CommandInfo info = new CommandInfo("linkCalendar");
        info.setDescription("Links the guild's calendar in a pretty embed!");
        info.setExample("!linkCalendar");
        return info;
    }

    /**
     * Issues the command this Object is responsible for.
     * @param args The command arguments.
     * @param event The event received.
     * @return <code>true</code> if successful, else <code>false</code>.
     */
    @Override
    public Boolean issueCommand(String[] args, MessageReceivedEvent event, GuildSettings settings) {
        try {
            //TODO: Handle multiple calendars...
            CalendarData data = DatabaseManager.getManager().getMainCalendar(event.getGuild().getLongID());

            if (data.getCalendarAddress().equalsIgnoreCase("primary")) {
                //Does not have a calendar.
                Message.sendMessage(MessageManager.getMessage("Creator.Calendar.NoCalendar", settings), event);
            } else {
            	Calendar cal;
            	if (settings.useExternalCalendar()) {
            		cal = CalendarAuth.getCalendarService(settings).calendars().get(data.getCalendarAddress()).execute();
				} else {
					cal = CalendarAuth.getCalendarService().calendars().get(data.getCalendarAddress()).execute();
				}

                Message.sendMessage(CalendarMessageFormatter.getCalendarLinkEmbed(cal, settings), event);
            }
        } catch (Exception e) {
            ExceptionHandler.sendException(event.getAuthor(), "Failed to connect to Google Cal.", e, this.getClass());
            Message.sendMessage(MessageManager.getMessage("Notification.Error.Unknown", settings), event);
        }
        return false;
    }
}