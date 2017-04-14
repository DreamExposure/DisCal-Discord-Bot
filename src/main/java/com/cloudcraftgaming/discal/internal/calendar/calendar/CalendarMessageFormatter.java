package com.cloudcraftgaming.discal.internal.calendar.calendar;

import com.cloudcraftgaming.discal.Main;
import com.google.api.services.calendar.model.Calendar;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.util.EmbedBuilder;

import java.net.URI;

/**
 * Created by Nova Fox on 1/4/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class CalendarMessageFormatter {
    private static String getCalendarLink(String calId) {
        URI callURI = URI.create(calId);
        return "https://calendar.google.com/calendar/embed?src=" + callURI;
    }

    public static EmbedObject getCalendarLinkEmbed(Calendar cal) {
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
        em.withUrl(CalendarMessageFormatter.getCalendarLink(cal.getId()));
        em.withFooterText("Calendar ID: " + cal.getId());
        em.withColor(56, 138, 237);

        return em.build();
    }

    /**
     * Creates an EmbedObject for the PreCalendar.
     * @param calendar The PreCalendar to create an EmbedObject for.
     * @return The EmbedObject for the PreCalendar.
     */
    public static EmbedObject getPreCalendarEmbed(PreCalendar calendar) {
        EmbedBuilder em = new EmbedBuilder();
        em.withAuthorIcon(Main.client.getGuildByID("266063520112574464").getIconURL());
        em.withAuthorName("DisCal");
        em.withTitle("Calendar Info");
        em.appendField("[R] Calendar Name/Summary", calendar.getSummary(), true);
        if (calendar.getDescription() != null) {
            em.appendField("[R] Calendar Description", calendar.getDescription(), false);
        } else {
            em.appendField("[R] Calendar Description", "Error/Unset", false);
        }
        if (calendar.getTimezone() != null) {
            em.appendField("[R] TimeZone", calendar.getTimezone(), true);
        } else {
            em.appendField("[R] TimeZone", "Error/Unset", true);
        }
        if (calendar.isEditing()) {
            em.appendField("Calendar ID", calendar.getCalendarId(), true);
        } else {
            em.appendField("Calendar ID", "Unknown until creation complete", true);
        }

        em.withFooterText("[R] means required, field needs a value.");
        em.withColor(56, 138, 237);

        return em.build();
    }
}