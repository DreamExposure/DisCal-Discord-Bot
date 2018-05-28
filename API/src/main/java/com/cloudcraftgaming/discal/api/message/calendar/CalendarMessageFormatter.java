package com.cloudcraftgaming.discal.api.message.calendar;

import com.cloudcraftgaming.discal.api.DisCalAPI;
import com.cloudcraftgaming.discal.api.message.MessageManager;
import com.cloudcraftgaming.discal.api.object.GuildSettings;
import com.cloudcraftgaming.discal.api.object.calendar.PreCalendar;
import com.google.api.services.calendar.model.Calendar;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.util.EmbedBuilder;

/**
 * Created by Nova Fox on 11/10/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
public class CalendarMessageFormatter {
	//TODO: Add support for multiple calendars.
	public static String getCalendarLink(long guildId) {
		return "https://www.discalbot.com/embed/calendar/" + guildId;
	}

	public static EmbedObject getCalendarLinkEmbed(Calendar cal, GuildSettings settings) {
		EmbedBuilder em = new EmbedBuilder();
		em.withAuthorIcon(DisCalAPI.getAPI().getClient().getGuildByID(266063520112574464L).getIconURL());
		em.withAuthorName("DisCal");
		em.withTitle(MessageManager.getMessage("Embed.Calendar.Link.Title", settings));
		em.appendField(MessageManager.getMessage("Embed.Calendar.Link.Summary", settings), cal.getSummary(), true);
		try {
			em.appendField(MessageManager.getMessage("Embed.Calendar.Link.Description", settings), cal.getDescription(), true);
		} catch (NullPointerException | IllegalArgumentException e) {
			//Some error, desc probably never set, just ignore no need to log.
		}
		em.appendField(MessageManager.getMessage("Embed.Calendar.Link.TimeZone", settings), cal.getTimeZone(), false);
		em.withUrl(CalendarMessageFormatter.getCalendarLink(settings.getGuildID()));
		em.withFooterText(MessageManager.getMessage("Embed.Calendar.Link.CalendarId", "%id%", cal.getId(), settings));
		em.withColor(56, 138, 237);

		return em.build();
	}

	/**
	 * Creates an EmbedObject for the PreCalendar.
	 *
	 * @param calendar The PreCalendar to create an EmbedObject for.
	 * @return The EmbedObject for the PreCalendar.
	 */
	public static EmbedObject getPreCalendarEmbed(PreCalendar calendar, GuildSettings settings) {
		EmbedBuilder em = new EmbedBuilder();
		em.withAuthorIcon(DisCalAPI.getAPI().getClient().getGuildByID(266063520112574464L).getIconURL());
		em.withAuthorName("DisCal");
		em.withTitle(MessageManager.getMessage("Embed.Calendar.Pre.Title", settings));
		if (calendar.getSummary() != null) {
			em.appendField(MessageManager.getMessage("Embed.Calendar.Pre.Summary", settings), calendar.getSummary(), true);
		} else {
			em.appendField(MessageManager.getMessage("Embed.Calendar.Pre.Summary", settings), "***UNSET***", true);
		}
		if (calendar.getDescription() != null) {
			em.appendField(MessageManager.getMessage("Embed.Calendar.Pre.Description", settings), calendar.getDescription(), false);
		} else {
			em.appendField(MessageManager.getMessage("Embed.Calendar.Pre.Description", settings), "***UNSET***", false);
		}
		if (calendar.getTimezone() != null) {
			em.appendField(MessageManager.getMessage("Embed.Calendar.Pre.TimeZone", settings), calendar.getTimezone(), true);
		} else {
			em.appendField(MessageManager.getMessage("Embed.Calendar.Pre.TimeZone", settings), "***UNSET***", true);
		}
		if (calendar.isEditing()) {
			em.appendField(MessageManager.getMessage("Embed.Calendar.Pre.CalendarId", settings), calendar.getCalendarId(), false);
		} //No else needed, just don't post it.

		em.withFooterText(MessageManager.getMessage("Embed.Calendar.Pre.Key", settings));
		em.withColor(56, 138, 237);

		return em.build();
	}
}