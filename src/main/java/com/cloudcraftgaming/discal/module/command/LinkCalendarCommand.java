package com.cloudcraftgaming.discal.module.command;

import com.cloudcraftgaming.discal.Main;
import com.cloudcraftgaming.discal.database.DatabaseManager;
import com.cloudcraftgaming.discal.internal.calendar.CalendarAuth;
import com.cloudcraftgaming.discal.internal.calendar.calendar.CalendarMessageFormatter;
import com.cloudcraftgaming.discal.internal.email.EmailSender;
import com.cloudcraftgaming.discal.utils.Message;
import com.google.api.services.calendar.model.Calendar;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.util.EmbedBuilder;

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
        return aliases;
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
            String calId = DatabaseManager.getManager().getData(event.getMessage().getGuild().getID()).getCalendarAddress();
            Calendar cal = CalendarAuth.getCalendarService().calendars().get(calId).execute();
            EmbedBuilder em = new EmbedBuilder();
            em.withAuthorIcon(Main.client.getGuildByID("266063520112574464").getIconURL());
            em.withAuthorName("DisCal");
            em.withTitle("Guild Calendar");
            em.appendField("Calendar Name/Summary", cal.getSummary(), true);
            try {
                em.appendField("Description", cal.getDescription(), true);
            } catch (NullPointerException | IllegalArgumentException e) {
                //Some error, desc probably never set, just ignore no need to email.
                em.appendField("Description", "N/a", true);
            }
            em.appendField("Timezone", cal.getTimeZone(), false);
            em.withUrl(CalendarMessageFormatter.getCalendarLink(event));
            em.withFooterText("Calendar ID: " + calId);
            em.withColor(36, 153, 153);

            Message.sendMessage(em.build(), event, client);
        } catch (IOException e) {
            EmailSender.getSender().sendExceptionEmail(e, this.getClass());
            Message.sendMessage("Oops! Something went wrong! I have emailed the developer!", event, client);
        }
        return false;
    }
}