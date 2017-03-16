package com.cloudcraftgaming.internal.calendar.calendar;

import com.cloudcraftgaming.Main;
import com.cloudcraftgaming.database.DatabaseManager;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.util.EmbedBuilder;

import java.net.URI;

/**
 * Created by Nova Fox on 1/4/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class CalendarMessageFormatter {
    public static String getCalendarLink(MessageReceivedEvent e) {
        String calId = DatabaseManager.getManager().getData(e.getMessage().getGuild().getID()).getCalendarAddress();
        URI callURI = URI.create(calId);
        return "https://calendar.google.com/calendar/embed?src=" + callURI;
    }

    public static EmbedObject getPreCalendarEmbed(PreCalendar calendar) {
        EmbedBuilder em = new EmbedBuilder();
        em.withAuthorIcon(Main.client.getGuildByID("266063520112574464").getIconURL());
        em.withAuthorName("DisCal");
        em.withTitle("Calendar Info");
        em.appendField("[R] Calendar Name/Summary", calendar.getSummary(), true);
        em.appendField("[R] Calendar Description", calendar.getDescription(), true);
        em.appendField("[R] TimeZone", calendar.getTimezone(), true);
        em.appendField("Calendar ID", "Unknown until creation complete", true);

        em.withFooterText("[R] means required, field needs a value.");
        em.withColor(36, 153, 153);

        return em.build();
    }
}