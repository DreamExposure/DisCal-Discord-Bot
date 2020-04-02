package org.dreamexposure.discal.client.module.command;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.CalendarListEntry;

import org.dreamexposure.discal.client.message.MessageManager;
import org.dreamexposure.discal.client.network.google.GoogleExternalAuth;
import org.dreamexposure.discal.core.calendar.CalendarAuth;
import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.logger.Logger;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.calendar.CalendarData;
import org.dreamexposure.discal.core.object.command.CommandInfo;
import org.dreamexposure.discal.core.utils.PermissionChecker;

import java.util.ArrayList;
import java.util.List;

import discord4j.core.event.domain.message.MessageCreateEvent;

/**
 * Created by Nova Fox on 6/29/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
@SuppressWarnings({"OptionalGetWithoutIsPresent", "ConstantConditions"})
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
	 * @param args  The command arguments.
	 * @param event The event received.
	 * @return <code>true</code> if successful, else <code>false</code>.
	 */
	@Override
	public boolean issueCommand(String[] args, MessageCreateEvent event, GuildSettings settings) {
		if (settings.isDevGuild() || settings.isPatronGuild()) {
			if (PermissionChecker.hasManageServerRole(event).blockOptional().orElse(false)) {
				if (args.length == 0) {
					if (DatabaseManager.getManager().getMainCalendar(settings.getGuildID()).getCalendarAddress().equalsIgnoreCase("primary")) {
						MessageManager.sendMessageAsync(MessageManager.getMessage("AddCalendar.Start", settings), event);
						GoogleExternalAuth.getAuth().requestCode(event, settings);
					} else {
						MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Calendar.HasCalendar", settings), event);
					}
				} else if (args.length == 1) {
					//Check if arg is calendar ID that is supported, if so, complete the setup.
					if (!DatabaseManager.getManager().getMainCalendar(settings.getGuildID()).getCalendarAddress().equalsIgnoreCase("primary")) {
						MessageManager.sendMessageAsync(MessageManager.getMessage("Creator.Calendar.HasCalendar", settings), event);
					} else if (settings.getEncryptedAccessToken().equalsIgnoreCase("N/a") && settings.getEncryptedRefreshToken().equalsIgnoreCase("N/a")) {
						MessageManager.sendMessageAsync(MessageManager.getMessage("AddCalendar.Select.NotAuth", settings), event);
					} else {
						try {
							Calendar service = CalendarAuth.getCalendarService(settings);
							List<CalendarListEntry> items = service.calendarList().list().setMinAccessRole("writer").execute().getItems();
							boolean valid = false;
							for (CalendarListEntry i: items) {
								if (!i.isDeleted() && i.getId().equals(args[0])) {
									//valid
									valid = true;
									break;
								}
							}
							if (valid) {
								//Update and save.
								CalendarData data = new CalendarData(event.getGuild().block().getId(), 1);
								data.setCalendarId(args[0]);
								data.setCalendarAddress(args[0]);
								data.setExternal(true);
								DatabaseManager.getManager().updateCalendar(data);

								//Update guild settings
								settings.setUseExternalCalendar(true);
								DatabaseManager.getManager().updateSettings(settings);

								MessageManager.sendMessageAsync(MessageManager.getMessage("AddCalendar.Select.Success", settings), event);
							} else {
								//Invalid
								MessageManager.sendMessageAsync(MessageManager.getMessage("AddCalendar.Select.Failure.Invalid", settings), event);
							}
						} catch (Exception e) {
							MessageManager.sendMessageAsync(MessageManager.getMessage("AddCalendar.Select.Failure.Unknown", settings), event);
							Logger.getLogger().exception(event.getMember().get(), "Failed to connect external calendar!", e, true, this.getClass());
						}
					}
				} else {
					MessageManager.sendMessageAsync(MessageManager.getMessage("AddCalendar.Specify", settings), event);
				}
			} else {
				MessageManager.sendMessageAsync(MessageManager.getMessage("Notification.Perm.MANAGE_SERVER", settings), event);
			}
		} else {
			MessageManager.sendMessageAsync(MessageManager.getMessage("Notification.Patron", settings), event);
		}
		return false;
	}
}