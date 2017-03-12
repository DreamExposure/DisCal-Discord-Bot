package com.cloudcraftgaming.module.command;

import com.cloudcraftgaming.Main;
import com.cloudcraftgaming.database.DatabaseManager;
import com.cloudcraftgaming.internal.calendar.CalendarAuth;
import com.cloudcraftgaming.internal.calendar.calendar.CalendarMessageFormatter;
import com.cloudcraftgaming.internal.email.EmailSender;
import com.cloudcraftgaming.utils.Message;
import com.google.api.services.calendar.model.Calendar;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.util.EmbedBuilder;

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
            EmbedBuilder em = new EmbedBuilder();
            em.withAuthorIcon(Main.client.getGuildByID("266063520112574464").getIconURL());
            em.withAuthorName("DisCal");
            em.withTitle("Guild Calendar");
            em.appendField("Calendar Name/Summary", cal.getSummary(), true);
            try {
                em.appendField("Description", cal.getDescription(), true);
            } catch (NullPointerException e) {
                //Some error, desc probably never set, just ignore no need to email.
                em.appendField("Description", "N/a", true);
            }
            em.appendField("Timezone", cal.getTimeZone(), false);
            em.withUrl(CalendarMessageFormatter.getCalendarLink(event));
            em.withFooterText("Calendar ID: " + calId);
            em.withColor(36, 153, 153);

            Message.sendMessage(em.build(), event, client);
        } catch (IOException e) {
            EmailSender.getSender().sendExceptionEmail(e);
            Message.sendMessage("Oops! Something went wrong! I have emailed the developer!", event, client);
        }
        return false;
    }
}