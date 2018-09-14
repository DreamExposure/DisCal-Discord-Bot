package org.dreamexposure.discal.client.module.command;

import com.google.api.services.calendar.model.Calendar;
import org.dreamexposure.discal.client.message.CalendarMessageFormatter;
import org.dreamexposure.discal.client.message.MessageManager;
import org.dreamexposure.discal.core.calendar.CalendarAuth;
import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.logger.Logger;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.calendar.CalendarData;
import org.dreamexposure.discal.core.object.command.CommandInfo;
import org.dreamexposure.discal.core.utils.GlobalConst;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.util.EmbedBuilder;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * Created by Nova Fox on 6/16/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class TimeCommand implements ICommand {

	/**
	 * Gets the command this Object is responsible for.
	 *
	 * @return The command this Object is responsible for.
	 */
	@Override
	public String getCommand() {
		return "time";
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
		return new ArrayList<>();
	}

	/**
	 * Gets the info on the command (not sub command) to be used in help menus.
	 *
	 * @return The command info.
	 */
	@Override
	public CommandInfo getCommandInfo() {
		CommandInfo info = new CommandInfo("time");
		info.setDescription("Displays the current time for the calendar in its respective TimeZone.");
		info.setExample("!time");
		return info;
	}

	/**
	 * Issues the command this Object is responsible for.
	 *
	 * @param args     The command arguments.
	 * @param event    The event received.
	 * @param settings The guild settings.
	 * @return <code>true</code> if successful, else <code>false</code>.
	 */
	@Override
	public boolean issueCommand(String[] args, MessageReceivedEvent event, GuildSettings settings) {
		calendarTime(event, settings);
		return false;
	}

	private void calendarTime(MessageReceivedEvent event, GuildSettings settings) {
		try {
			//TODO: Handle multiple calendars...
			CalendarData data = DatabaseManager.getManager().getMainCalendar(event.getGuild().getLongID());

			if (data.getCalendarAddress().equalsIgnoreCase("primary")) {
				//Does not have a calendar.
				MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Calendar.NoCalendar", settings), event);
			} else {
				Calendar cal = CalendarAuth.getCalendarService(settings).calendars().get(data.getCalendarAddress()).execute();

				LocalDateTime ldt = LocalDateTime.now(ZoneId.of(cal.getTimeZone()));

				//Okay... format and then we can go from there...
				DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy/MM/dd hh:mm:ss a");
				String thisIsTheCorrectTime = format.format(ldt);

				//Build embed and send.
				EmbedBuilder em = new EmbedBuilder();
				em.withAuthorIcon(GlobalConst.iconUrl);
				em.withAuthorName("DisCal");
				em.withAuthorUrl(GlobalConst.discalSite);
				em.withTitle(MessageManager.getMessage("Embed.Time.Title", settings));
				em.appendField(MessageManager.getMessage("Embed.Time.Time", settings), thisIsTheCorrectTime, false);
				em.appendField(MessageManager.getMessage("Embed.Time.TimeZone", settings), cal.getTimeZone(), false);

				em.withFooterText(MessageManager.getMessage("Embed.Time.Footer", settings));
				em.withUrl(CalendarMessageFormatter.getCalendarLink(settings.getGuildID()));
				em.withColor(GlobalConst.discalColor);
				MessageManager.sendMessageAsync(em.build(), event);
			}
		} catch (Exception e) {
			Logger.getLogger().exception(event.getAuthor(), "Failed to connect to Google Cal.", e, this.getClass());
			MessageManager.sendMessageAsync(MessageManager.getMessage("Notification.Error.Unknown", settings), event);
		}
	}
}