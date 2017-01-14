package com.cloudcraftgaming.module.command;

import com.cloudcraftgaming.database.DatabaseManager;
import com.cloudcraftgaming.internal.calendar.CalendarAuth;
import com.cloudcraftgaming.internal.calendar.calendar.CalendarMessageFormatter;
import com.cloudcraftgaming.utils.Message;
import com.google.api.services.calendar.model.Calendar;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;

import java.io.IOException;

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
        try {
            String calId = DatabaseManager.getManager().getData(event.getMessage().getGuild().getID()).getCalendarAddress();
            Calendar cal = CalendarAuth.getCalendarService().calendars().get(calId).execute();
            Message.sendMessage(CalendarMessageFormatter.getFormatEventMessage(event, cal), event, client);
        } catch (IOException e) {
            Message.sendMessage("Oops! Something went wrong! Please report this to the developer!", event, client);
        }
        return false;
    }
}