package org.dreamexposure.discal.client.message;

import com.google.api.services.calendar.model.Calendar;
import discord4j.core.object.util.Snowflake;
import discord4j.core.spec.EmbedCreateSpec;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.calendar.PreCalendar;
import org.dreamexposure.discal.core.utils.GlobalConst;

/**
 * Created by Nova Fox on 11/10/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
public class CalendarMessageFormatter {
	//TODO: Add support for multiple calendars.
	public static String getCalendarLink(Snowflake guildId) {
		return "https://www.discalbot.com/embed/calendar/" + guildId.asString();
	}

	public static EmbedCreateSpec getCalendarLinkEmbed(Calendar cal, GuildSettings settings) {
		EmbedCreateSpec em = new EmbedCreateSpec();
		em.setAuthor("DisCal", GlobalConst.discalSite, GlobalConst.iconUrl);
		em.setTitle(MessageManager.getMessage("Embed.Calendar.Link.Title", settings));
		em.addField(MessageManager.getMessage("Embed.Calendar.Link.Summary", settings), cal.getSummary(), true);
		try {
			em.addField(MessageManager.getMessage("Embed.Calendar.Link.Description", settings), cal.getDescription(), true);
		} catch (NullPointerException | IllegalArgumentException e) {
			//Some error, desc probably never set, just ignore no need to log.
		}
		em.addField(MessageManager.getMessage("Embed.Calendar.Link.TimeZone", settings), cal.getTimeZone(), false);
		em.setUrl(CalendarMessageFormatter.getCalendarLink(settings.getGuildID()));
		em.setFooter(MessageManager.getMessage("Embed.Calendar.Link.CalendarId", "%id%", cal.getId(), settings), null);
		em.setColor(GlobalConst.discalColor);

		return em;
	}

	/**
	 * Creates an EmbedObject for the PreCalendar.
	 *
	 * @param calendar The PreCalendar to create an EmbedObject for.
	 * @return The EmbedObject for the PreCalendar.
	 */
	public static EmbedCreateSpec getPreCalendarEmbed(PreCalendar calendar, GuildSettings settings) {
		EmbedCreateSpec em = new EmbedCreateSpec();
		em.setAuthor("DisCal", GlobalConst.discalSite, GlobalConst.iconUrl);
		em.setTitle(MessageManager.getMessage("Embed.Calendar.Pre.Title", settings));
		if (calendar.getSummary() != null)
			em.addField(MessageManager.getMessage("Embed.Calendar.Pre.Summary", settings), calendar.getSummary(), true);
		else
			em.addField(MessageManager.getMessage("Embed.Calendar.Pre.Summary", settings), "***UNSET***", true);

		if (calendar.getDescription() != null)
			em.addField(MessageManager.getMessage("Embed.Calendar.Pre.Description", settings), calendar.getDescription(), false);
		else
			em.addField(MessageManager.getMessage("Embed.Calendar.Pre.Description", settings), "***UNSET***", false);

		if (calendar.getTimezone() != null)
			em.addField(MessageManager.getMessage("Embed.Calendar.Pre.TimeZone", settings), calendar.getTimezone(), true);
		else
			em.addField(MessageManager.getMessage("Embed.Calendar.Pre.TimeZone", settings), "***UNSET***", true);

		if (calendar.isEditing())
			em.addField(MessageManager.getMessage("Embed.Calendar.Pre.CalendarId", settings), calendar.getCalendarId(), false);


		em.setFooter(MessageManager.getMessage("Embed.Calendar.Pre.Key", settings), null);
		em.setColor(GlobalConst.discalColor);

		return em;
	}
}