package com.cloudcraftgaming.discal.bot.utils;

import com.cloudcraftgaming.discal.Main;
import com.cloudcraftgaming.discal.api.database.DatabaseManager;
import com.cloudcraftgaming.discal.api.object.GuildSettings;
import com.cloudcraftgaming.discal.api.utils.ExceptionHandler;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Permissions;

/**
 * Created by Nova Fox on 1/19/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class PermissionChecker {
	/**
	 * Checks if the user who sent the received message has the proper role to use a command.
	 *
	 * @param event The Event received to check for the user and guild.
	 * @return <code>true</code> if the user has the proper role, otherwise <code>false</code>.
	 */
	public static boolean hasSufficientRole(MessageReceivedEvent event) {
		//TODO: Figure out exactly what is causing a NPE here...
		try {
			GuildSettings settings = DatabaseManager.getManager().getSettings(event.getGuild().getLongID());
			if (!settings.getControlRole().equalsIgnoreCase("everyone")) {
				IUser sender = event.getMessage().getAuthor();
				String roleId = settings.getControlRole();
				IRole role = null;

				for (IRole r : event.getMessage().getGuild().getRoles()) {
					if (r.getStringID().equals(roleId)) {
						role = r;
						break;
					}
				}

				if (role != null) {
					for (IRole r : sender.getRolesForGuild(event.getMessage().getGuild())) {
						if (r.getStringID().equals(role.getStringID()) || r.getPosition() > role.getPosition()) {
							return true;
						}
					}
					return false;
				} else {
					//Role not found... reset Db...
					settings.setControlRole("everyone");
					DatabaseManager.getManager().updateSettings(settings);
					return true;
				}
			}
		} catch (Exception e) {
			//Something broke so we will harmlessly allow access and email the dev.
			ExceptionHandler.sendException(event.getMessage().getAuthor(), "Failed to check for sufficient control role.", e, PermissionChecker.class);
			return true;
		}
		return true;
	}

	public static boolean hasManageServerRole(MessageReceivedEvent event) {
		return event.getMessage().getAuthor().getPermissionsForGuild(event.getMessage().getGuild()).contains(
				Permissions.MANAGE_SERVER);
	}

	/**
	 * Checks if the user sent the command in a DisCal channel (if set).
	 *
	 * @param event The event received to check for the correct channel.
	 * @return <code>true</code> if in correct channel, otherwise <code>false</code>.
	 */
	public static boolean isCorrectChannel(MessageReceivedEvent event) {
		try {
			GuildSettings settings = DatabaseManager.getManager().getSettings(event.getGuild().getLongID());
			if (settings.getDiscalChannel().equalsIgnoreCase("all")) {
				return true;
			}

			IChannel channel = null;
			for (IChannel c : event.getMessage().getGuild().getChannels()) {
				if (c.getStringID().equals(settings.getDiscalChannel())) {
					channel = c;
					break;
				}
			}

			if (channel != null) {
				return event.getMessage().getChannel().getStringID().equals(channel.getStringID());
			}

			//If we got here, the channel no longer exists, reset data and return true.
			settings.setDiscalChannel("all");
			DatabaseManager.getManager().updateSettings(settings);
			return true;
		} catch (Exception e) {
			//Catch any errors so that the bot always responds...
			ExceptionHandler.sendException(event.getMessage().getAuthor(), "Failed to check for discal channel.", e, PermissionChecker.class);
			return true;
		}
	}

	public static boolean botHasMessageManagePerms(MessageReceivedEvent event) {
		return Main.getSelfUser().getPermissionsForGuild(event.getMessage().getGuild()).contains(Permissions.MANAGE_MESSAGES);
	}
}