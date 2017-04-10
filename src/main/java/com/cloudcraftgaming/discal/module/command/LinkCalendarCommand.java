package com.cloudcraftgaming.discal.module.command;

import com.cloudcraftgaming.discal.database.DatabaseManager;
import com.cloudcraftgaming.discal.internal.calendar.CalendarAuth;
import com.cloudcraftgaming.discal.internal.calendar.calendar.CalendarMessageFormatter;
import com.cloudcraftgaming.discal.internal.data.CalendarData;
import com.cloudcraftgaming.discal.internal.email.EmailSender;
import com.cloudcraftgaming.discal.module.command.info.CommandInfo;
import com.cloudcraftgaming.discal.utils.Message;
import com.google.api.services.calendar.model.Calendar;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;

import java.io.IOException;
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
        aliases.add("callador");
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
     * @param client The Client associated with the Bot.
     * @return <code>true</code> if successful, else <code>false</code>.
     */
    @Override
    public Boolean issueCommand(String[] args, MessageReceivedEvent event, IDiscordClient client) {
        try {
            //TODO: Handle multiple calendars...
            CalendarData data = DatabaseManager.getManager().getMainCalendar(event.getMessage().getGuild().getID());

            if (data.getCalendarAddress().equalsIgnoreCase("primary")) {
                //Does not have a calendar.
                Message.sendMessage("You do not have a calendar to link!" + Message.lineBreak + "Use `!calendar create` to create a new calendar!", event, client);
            } else {
                Calendar cal = CalendarAuth.getCalendarService().calendars().get(data.getCalendarAddress()).execute();

                Message.sendMessage(CalendarMessageFormatter.getCalendarLinkEmbed(cal), event, client);
            }
        } catch (IOException e) {
            EmailSender.getSender().sendExceptionEmail(e, this.getClass());
            Message.sendMessage("Oops! Something went wrong! I have emailed the developer!", event, client);
        }
        return false;
    }
}