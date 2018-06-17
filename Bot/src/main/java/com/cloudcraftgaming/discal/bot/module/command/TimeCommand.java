package com.cloudcraftgaming.discal.bot.module.command;

import com.cloudcraftgaming.discal.api.DisCalAPI;
import com.cloudcraftgaming.discal.api.calendar.CalendarAuth;
import com.cloudcraftgaming.discal.api.database.DatabaseManager;
import com.cloudcraftgaming.discal.api.message.Message;
import com.cloudcraftgaming.discal.api.message.MessageManager;
import com.cloudcraftgaming.discal.api.message.calendar.CalendarMessageFormatter;
import com.cloudcraftgaming.discal.api.object.GuildSettings;
import com.cloudcraftgaming.discal.api.object.calendar.CalendarData;
import com.cloudcraftgaming.discal.api.object.command.CommandInfo;
import com.cloudcraftgaming.discal.logger.Logger;
import com.google.api.services.calendar.model.Calendar;
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
				Message.sendMessage(MessageManager.getMessage("Creator.Calendar.NoCalendar", settings), event);
			} else {
				Calendar cal = CalendarAuth.getCalendarService(settings).calendars().get(data.getCalendarAddress()).execute();

				LocalDateTime ldt = LocalDateTime.now(ZoneId.of(cal.getTimeZone()));

				//Okay... format and then we can go from there...
				DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy/MM/dd hh:mm:ss a");
				String thisIsTheCorrectTime = format.format(ldt);

				//Build embed and send.
				EmbedBuilder em = new EmbedBuilder();
				em.withAuthorIcon(DisCalAPI.getAPI().iconUrl);
				em.withAuthorName("DisCal");
				em.withTitle(MessageManager.getMessage("Embed.Time.Title", settings));
				em.appendField(MessageManager.getMessage("Embed.Time.Time", settings), thisIsTheCorrectTime, false);
				em.appendField(MessageManager.getMessage("Embed.Time.TimeZone", settings), cal.getTimeZone(), false);

				em.withFooterText(MessageManager.getMessage("Embed.Time.Footer", settings));
				em.withUrl(CalendarMessageFormatter.getCalendarLink(settings.getGuildID()));
				em.withColor(56, 138, 237);
				Message.sendMessage(em.build(), event);
			}
		} catch (Exception e) {
			Logger.getLogger().exception(event.getAuthor(), "Failed to connect to Google Cal.", e, this.getClass(), true);
			Message.sendMessage(MessageManager.getMessage("Notification.Error.Unknown", settings), event);
		}
	}
}