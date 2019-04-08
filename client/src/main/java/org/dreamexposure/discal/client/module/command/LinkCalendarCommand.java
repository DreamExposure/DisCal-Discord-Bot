package org.dreamexposure.discal.client.module.command;

import com.google.api.services.calendar.model.Calendar;
import discord4j.core.event.domain.message.MessageCreateEvent;
import org.dreamexposure.discal.client.message.CalendarMessageFormatter;
import org.dreamexposure.discal.client.message.MessageManager;
import org.dreamexposure.discal.core.calendar.CalendarAuth;
import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.logger.Logger;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.calendar.CalendarData;
import org.dreamexposure.discal.core.object.command.CommandInfo;

import java.util.ArrayList;

/**
 * Created by Nova Fox on 1/3/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
@SuppressWarnings({"ConstantConditions", "OptionalGetWithoutIsPresent"})
public class LinkCalendarCommand implements ICommand {
	/**
	 * Gets the command this Object is responsible for.
	 *
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
		aliases.add("linkcallador");
		return aliases;
	}

	/**
	 * Gets the info on the command (not sub command) to be used in help menus.
	 *
	 * @return The command info.
	 */
	@Override
	public CommandInfo getCommandInfo() {
		CommandInfo info = new CommandInfo("linkCalendar");
		info.setDescription("Links the guild's calendar in a pretty embed!");
		info.setExample("!linkCalendar");
		return info;
	}

	/**
	 * Issues the command this Object is responsible for.
	 *
	 * @param args  The command arguments.
	 * @param event The event received.
	 * @return <code>true</code> if successful, else <code>false</code>.
	 */
	@Override
	public boolean issueCommand(String[] args, MessageCreateEvent event, GuildSettings settings) {
		try {
			//TODO: Handle multiple calendars...
			CalendarData data = DatabaseManager.getManager().getMainCalendar(event.getGuild().block().getId());

			if (data.getCalendarAddress().equalsIgnoreCase("primary")) {
				//Does not have a calendar.
				MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Calendar.NoCalendar", settings), event);
			} else {
				Calendar cal = CalendarAuth.getCalendarService(settings).calendars().get(data.getCalendarAddress()).execute();

				MessageManager.sendMessageAsync(CalendarMessageFormatter.getCalendarLinkEmbed(cal, settings), event);
			}
		} catch (Exception e) {
			Logger.getLogger().exception(event.getMember().get(), "Failed to connect to Google Cal.", e, true, this.getClass());
			MessageManager.sendMessageAsync(MessageManager.getMessage("Notification.Error.Unknown", settings), event);
		}
		return false;
	}
}