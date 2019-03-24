package org.dreamexposure.discal.client.message;

import com.google.api.services.calendar.model.Calendar;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.util.Image;
import discord4j.core.object.util.Snowflake;
import discord4j.core.spec.EmbedCreateSpec;
import org.dreamexposure.discal.client.DisCalClient;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.calendar.PreCalendar;
import org.dreamexposure.discal.core.utils.GlobalConst;

import java.util.function.Consumer;

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

	public static Consumer<EmbedCreateSpec> getCalendarLinkEmbed(Calendar cal, GuildSettings settings) {
		return spec -> {
			Guild guild = DisCalClient.getClient().getGuildById(settings.getGuildID()).block();

			if (settings.isBranded() && guild != null)
				spec.setAuthor(guild.getName(), GlobalConst.discalSite, guild.getIconUrl(Image.Format.PNG).orElse(GlobalConst.iconUrl));
			else
				spec.setAuthor("DisCal", GlobalConst.discalSite, GlobalConst.iconUrl);

			spec.setTitle(MessageManager.getMessage("Embed.Calendar.Link.Title", settings));
			spec.addField(MessageManager.getMessage("Embed.Calendar.Link.Summary", settings), cal.getSummary(), true);
			try {
				spec.addField(MessageManager.getMessage("Embed.Calendar.Link.Description", settings), cal.getDescription(), true);
			} catch (NullPointerException | IllegalArgumentException e) {
				//Some error, desc probably never set, just ignore no need to log.
			}
			spec.addField(MessageManager.getMessage("Embed.Calendar.Link.TimeZone", settings), cal.getTimeZone(), false);
			spec.setUrl(CalendarMessageFormatter.getCalendarLink(settings.getGuildID()));
			spec.setFooter(MessageManager.getMessage("Embed.Calendar.Link.CalendarId", "%id%", cal.getId(), settings), null);
			spec.setColor(GlobalConst.discalColor);
		};
	}

	/**
	 * Creates an EmbedObject for the PreCalendar.
	 *
	 * @param calendar The PreCalendar to create an EmbedObject for.
	 * @return The EmbedObject for the PreCalendar.
	 */
	public static Consumer<EmbedCreateSpec> getPreCalendarEmbed(PreCalendar calendar, GuildSettings settings) {
		return spec -> {
			Guild guild = DisCalClient.getClient().getGuildById(settings.getGuildID()).block();

			if (settings.isBranded() && guild != null)
				spec.setAuthor(guild.getName(), GlobalConst.discalSite, guild.getIconUrl(Image.Format.PNG).orElse(GlobalConst.iconUrl));
			else
				spec.setAuthor("DisCal", GlobalConst.discalSite, GlobalConst.iconUrl);

			spec.setTitle(MessageManager.getMessage("Embed.Calendar.Pre.Title", settings));
			if (calendar.getSummary() != null)
				spec.addField(MessageManager.getMessage("Embed.Calendar.Pre.Summary", settings), calendar.getSummary(), true);
			else
				spec.addField(MessageManager.getMessage("Embed.Calendar.Pre.Summary", settings), "***UNSET***", true);

			if (calendar.getDescription() != null)
				spec.addField(MessageManager.getMessage("Embed.Calendar.Pre.Description", settings), calendar.getDescription(), false);
			else
				spec.addField(MessageManager.getMessage("Embed.Calendar.Pre.Description", settings), "***UNSET***", false);

			if (calendar.getTimezone() != null)
				spec.addField(MessageManager.getMessage("Embed.Calendar.Pre.TimeZone", settings), calendar.getTimezone(), true);
			else
				spec.addField(MessageManager.getMessage("Embed.Calendar.Pre.TimeZone", settings), "***UNSET***", true);

			if (calendar.isEditing())
				spec.addField(MessageManager.getMessage("Embed.Calendar.Pre.CalendarId", settings), calendar.getCalendarId(), false);


			spec.setFooter(MessageManager.getMessage("Embed.Calendar.Pre.Key", settings), null);
			spec.setColor(GlobalConst.discalColor);

		};
	}
}