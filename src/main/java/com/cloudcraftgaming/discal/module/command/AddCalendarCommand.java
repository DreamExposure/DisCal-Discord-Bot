package com.cloudcraftgaming.discal.module.command;

import com.cloudcraftgaming.discal.database.DatabaseManager;
import com.cloudcraftgaming.discal.internal.calendar.CalendarAuth;
import com.cloudcraftgaming.discal.internal.data.CalendarData;
import com.cloudcraftgaming.discal.internal.data.GuildSettings;
import com.cloudcraftgaming.discal.internal.network.google.Authorization;
import com.cloudcraftgaming.discal.module.command.info.CommandInfo;
import com.cloudcraftgaming.discal.utils.ExceptionHandler;
import com.cloudcraftgaming.discal.utils.Message;
import com.cloudcraftgaming.discal.utils.MessageManager;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.CalendarListEntry;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nova Fox on 6/29/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class AddCalendarCommand implements ICommand {
	/**
	 * Gets the command this Object is responsible for.
	 *
	 * @return The command this Object is responsible for.
	 */
	@Override
	public String getCommand() {
		return "addCalendar";
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
		aliases.add("addCal");

		return aliases;
	}

	/**
	 * Gets the info on the command (not sub command) to be used in help menus.
	 *
	 * @return The command info.
	 */
	@Override
	public CommandInfo getCommandInfo() {
		CommandInfo info = new CommandInfo("addCalendar");
		info.setDescription("Starts the process of adding an external calendar");
		info.setExample("!addCalendar");

		return info;
	}

	/**
	 * Issues the command this Object is responsible for.
	 *
	 * @param args     The command arguments.
	 * @param event    The event received.
	 * @param settings
	 * @return <code>true</code> if successful, else <code>false</code>.
	 */
	@Override
	public Boolean issueCommand(String[] args, MessageReceivedEvent event, GuildSettings settings) {
		if (settings.isDevGuild()) {
			if (args.length == 0) {
				if (!DatabaseManager.getManager().getMainCalendar(settings.getGuildID()).getCalendarAddress().equalsIgnoreCase("primary")) {
					//TODO: add check to make sure process has not been started!!!!!!!!
					Message.sendMessage("Please check your DMs for instructions on how to add an external calendar!", event);
					Authorization.getAuth().requestCode(event);
				} else {
					Message.sendMessage(MessageManager.getMessage("Creator.Calendar.HasCalendar", settings), event);
				}
			} else if (args.length == 1) {
				//Check if arg is calendar ID that is supported, if so, complete the setup.
				if (settings.getEncryptedAccessToken().equalsIgnoreCase("N/a") && settings.getEncryptedRefreshToken().equalsIgnoreCase("N/a")) {
					Message.sendMessage("An external account has not been authorized yet! Use `!addCalendar` to start the authorization process if not started!", event);
				} else {
					try {
						Calendar service = CalendarAuth.getCalendarService(settings);
						List<CalendarListEntry> items = service.calendarList().list().setMinAccessRole("writer").execute().getItems();
						boolean valid = false;
						for (CalendarListEntry i : items) {
							if (!i.isDeleted() && i.getId().equals(args[0])) {
								//valid
								valid = true;
								break;
							}
						}
						if (valid) {
							//Update and save.
							CalendarData data = new CalendarData(event.getGuild().getLongID(), 1);
							data.setCalendarId(args[0]);
							data.setCalendarAddress(args[0]);
							DatabaseManager.getManager().updateCalendar(data, false);

							//Update guild settings
							settings.setUseExternalCalendar(true);
							DatabaseManager.getManager().updateSettings(settings);

							Message.sendMessage("Calendar successfully connected! You may start making events on it!!!", event);
						} else {
							//Invalid
							Message.sendMessage("Calendar ID is invalid! Please make sure the ID specified is valid!", event);
						}
					} catch (Exception e) {
						Message.sendMessage("An error occurred! The development team has been alerted!", event);
						ExceptionHandler.sendException(event.getAuthor(), "Failed to connect external calendar!", e, this.getClass());
					}
				}
			} else {
				Message.sendMessage(MessageManager.getMessage("Notification.Disabled", settings), event);
			}
		}
		return false;
	}
}