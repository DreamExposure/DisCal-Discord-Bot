package com.cloudcraftgaming.discal.bot.module.command;

import com.cloudcraftgaming.discal.api.calendar.CalendarAuth;
import com.cloudcraftgaming.discal.api.database.DatabaseManager;
import com.cloudcraftgaming.discal.api.message.Message;
import com.cloudcraftgaming.discal.api.message.MessageManager;
import com.cloudcraftgaming.discal.api.network.google.Authorization;
import com.cloudcraftgaming.discal.api.object.GuildSettings;
import com.cloudcraftgaming.discal.api.object.calendar.CalendarData;
import com.cloudcraftgaming.discal.api.object.command.CommandInfo;
import com.cloudcraftgaming.discal.api.utils.PermissionChecker;
import com.cloudcraftgaming.discal.logger.Logger;
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
		aliases.add("addcal");

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
		info.setExample("!addCalendar (calendar ID)");

		return info;
	}

	/**
	 * Issues the command this Object is responsible for.
	 *
	 * @param args     The command arguments.
	 * @param event    The event received.
	 * @return <code>true</code> if successful, else <code>false</code>.
	 */
	@Override
	public boolean issueCommand(String[] args, MessageReceivedEvent event, GuildSettings settings) {
		if (settings.isDevGuild() || settings.isPatronGuild()) {
			if (PermissionChecker.hasManageServerRole(event)) {
				if (args.length == 0) {
					if (DatabaseManager.getManager().getMainCalendar(settings.getGuildID()).getCalendarAddress().equalsIgnoreCase("primary")) {
						Message.sendMessage(MessageManager.getMessage("AddCalendar.Start", settings), event);
						Authorization.getAuth().requestCode(event, settings);
					} else {
						Message.sendMessage(MessageManager.getMessage("Creator.Calendar.HasCalendar", settings), event);
					}
				} else if (args.length == 1) {
					//Check if arg is calendar ID that is supported, if so, complete the setup.
					if (!DatabaseManager.getManager().getMainCalendar(settings.getGuildID()).getCalendarAddress().equalsIgnoreCase("primary")) {
						Message.sendMessage(MessageManager.getMessage("Creator.Calendar.HasCalendar", settings), event);
					} else if (settings.getEncryptedAccessToken().equalsIgnoreCase("N/a") && settings.getEncryptedRefreshToken().equalsIgnoreCase("N/a")) {
						Message.sendMessage(MessageManager.getMessage("AddCalendar.Select.NotAuth", settings), event);
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
								data.setExternal(true);
								DatabaseManager.getManager().updateCalendar(data);

								//Update guild settings
								settings.setUseExternalCalendar(true);
								DatabaseManager.getManager().updateSettings(settings);

								Message.sendMessage(MessageManager.getMessage("AddCalendar.Select.Success", settings), event);
							} else {
								//Invalid
								Message.sendMessage(MessageManager.getMessage("AddCalendar.Select.Failure.Invalid", settings), event);
							}
						} catch (Exception e) {
							Message.sendMessage(MessageManager.getMessage("AddCalendar.Select.Failure.Unknown", settings), event);
							Logger.getLogger().exception(event.getAuthor(), "Failed to connect external calendar!", e, this.getClass(), true);
						}
					}
				} else {
					Message.sendMessage(MessageManager.getMessage("AddCalendar.Specify", settings), event);
				}
			} else {
				Message.sendMessage(MessageManager.getMessage("Notification.Perm.MANAGE_SERVER", settings), event);
			}
		} else {
			Message.sendMessage(MessageManager.getMessage("Notification.Patron", settings), event);
		}
		return false;
	}
}