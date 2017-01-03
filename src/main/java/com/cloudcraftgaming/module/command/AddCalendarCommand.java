package com.cloudcraftgaming.module.command;

import com.cloudcraftgaming.database.DatabaseManager;
import com.cloudcraftgaming.internal.data.BotData;
import com.cloudcraftgaming.utils.Message;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;

/**
 * Created by Nova Fox on 1/3/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class AddCalendarCommand implements ICommand {
    @Override
    public String getCommand() {
        return "addCalendar";
    }

    @Override
    public Boolean issueCommand(String[] args, MessageReceivedEvent event, IDiscordClient client) {
        if (args.length < 1) {
            //Not enough args, calendar ID must be listed.
            Message.sendMessage("You must specify the Calendar ID to be used!", event, client);
            return false;
        } else {
            String calendarId = args[0];
            BotData bd = new BotData(event.getMessage().getGuild().getID());
            bd.setCalendarId(calendarId);
            DatabaseManager.getManager().updateData(bd);
            Message.sendMessage("Calendar changed to: " + calendarId, event, client);
        }
        return false;
    }
}