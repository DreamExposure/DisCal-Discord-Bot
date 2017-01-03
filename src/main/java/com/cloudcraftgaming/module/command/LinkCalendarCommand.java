package com.cloudcraftgaming.module.command;

import com.cloudcraftgaming.database.DatabaseManager;
import com.cloudcraftgaming.utils.Message;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;

/**
 * Created by Nova Fox on 1/3/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class LinkCalendarCommand implements ICommand {
    @Override
    public String getCommand() {
        return "LinkCalendar";
    }

    @Override
    public Boolean issueCommand(String[] args, MessageReceivedEvent event, IDiscordClient client) {
        String calId = DatabaseManager.getManager().getData(event.getMessage().getGuild().getID()).getCalendarId();
        String link = "https://calendar.google.com/calendar/embed?src=" + calId + "%40group.calendar.google.com";
        Message.sendMessage("Calendar: " + link, event, client);
        return false;
    }
}
