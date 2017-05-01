package com.cloudcraftgaming.discal.internal.calendar.calendar;

import com.cloudcraftgaming.discal.Main;
import com.cloudcraftgaming.discal.utils.MessageManager;
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

    public static EmbedObject getCalendarLinkEmbed(Calendar cal, long guildId) {
        EmbedBuilder em = new EmbedBuilder();
        em.withAuthorIcon(Main.client.getGuildByID(266063520112574464L).getIconURL());
        em.withAuthorName("DisCal");
        em.withTitle(MessageManager.getMessage("Embed.Calendar.Link.Title", guildId));
        em.appendField(MessageManager.getMessage("Embed.Calendar.Link.Summary", guildId), cal.getSummary(), true);
        try {
            em.appendField(MessageManager.getMessage("Embed.Calendar.Link.Description", guildId), cal.getDescription(), true);
        } catch (NullPointerException | IllegalArgumentException e) {
            //Some error, desc probably never set, just ignore no need to log.
        }
        em.appendField(MessageManager.getMessage("Embed.Calendar.Link.TimeZone", guildId), cal.getTimeZone(), false);
        em.withUrl(CalendarMessageFormatter.getCalendarLink(cal.getId()));
        em.withFooterText(MessageManager.getMessage("Embed.Calendar.Link.CalendarId", "%id%", cal.getId(), guildId));
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
        em.withAuthorIcon(Main.client.getGuildByID(266063520112574464L).getIconURL());
        em.withAuthorName("DisCal");
        em.withTitle(MessageManager.getMessage("Embed.Calendar.Pre.Title", calendar.getGuildId()));
        em.appendField(MessageManager.getMessage("Embed.Calendar.Pre.Summary", calendar.getGuildId()), calendar.getSummary(), true);
        if (calendar.getDescription() != null) {
            em.appendField(MessageManager.getMessage("Embed.Calendar.Pre.Description", calendar.getGuildId()), calendar.getDescription(), false);
        } else {
            em.appendField(MessageManager.getMessage("Embed.Calendar.Pre.Description", calendar.getGuildId()), "Error/Unset", false);
        }
        if (calendar.getTimezone() != null) {
            em.appendField(MessageManager.getMessage("Embed.Calendar.Pre.TimeZone", calendar.getGuildId()), calendar.getTimezone(), true);
        } else {
            em.appendField(MessageManager.getMessage("Embed.Calendar.Pre.TimeZone", calendar.getGuildId()), "***UNSET***", true);
        }
        if (calendar.isEditing()) {
            em.appendField(MessageManager.getMessage("Embed.Calendar.Pre.CalendarId", calendar.getGuildId()), calendar.getCalendarId(), true);
        } //No else needed, just don't post it.

        em.withFooterText(MessageManager.getMessage("Embed.Calendar.Pre.Key", calendar.getGuildId()));
        em.withColor(56, 138, 237);

        return em.build();
    }
}