package com.cloudcraftgaming.discal.module.command;

import com.cloudcraftgaming.discal.database.DatabaseManager;
import com.cloudcraftgaming.discal.internal.data.BotData;
import com.cloudcraftgaming.discal.utils.Message;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;

/**
 * Created by Nova Fox on 1/3/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class AddCalendarCommand implements ICommand {
    /**
     * Gets the command this Object is responsible for.
     * @return The command this Object is responsible for.
     */
    @Override
    public String getCommand() {
        return "addCalendar";
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
        if (args.length < 2) {
            //Not enough args, calendar ID must be listed.
            Message.sendMessage("You must specify the Calendar ID and its address to be used!", event, client);
            return false;
        } else {
            String calendarId = args[0];
            String calendarAddress = args[1];
            BotData bd = new BotData(event.getMessage().getGuild().getID());
            bd.setCalendarId(calendarId);
            bd.setCalendarAddress(calendarAddress);
            DatabaseManager.getManager().updateData(bd);
            Message.sendMessage("Calendar changed to: " + calendarId + ", " + calendarAddress, event, client);
        }
        return false;
    }
}