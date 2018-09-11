package org.dreamexposure.discal.core.utils;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.*;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.PermissionSet;
import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.logger.Logger;
import org.dreamexposure.discal.core.object.GuildSettings;

/**
 * Created by Nova Fox on 1/19/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
@SuppressWarnings({"ConstantConditions", "OptionalGetWithoutIsPresent"})
public class PermissionChecker {
	/**
	 * Checks if the user who sent the received message has the proper role to use a command.
	 *
	 * @param event The Event received to check for the user and guild.
	 * @return <code>true</code> if the user has the proper role, otherwise <code>false</code>.
	 */
	public static boolean hasSufficientRole(MessageCreateEvent event) {
		//TODO: Figure out exactly what is causing a NPE here...
		try {
			GuildSettings settings = DatabaseManager.getManager().getSettings(event.getGuild().block().getId());
			if (!settings.getControlRole().equalsIgnoreCase("everyone")) {
				String roleId = settings.getControlRole();
				Role role = null;

				for (Role r : event.getGuild().map(Guild::getRoles).block().toIterable()) {
					if (r.getId().asString().equalsIgnoreCase(roleId)) {
						role = r;
						break;
					}
				}

				if (role != null) {
					for (Role r : event.getMember().get().getRoles().toIterable()) {
						if (r.getId() == role.getId() || r.getPosition().block() > role.getPosition().block())
							return true;

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
			Logger.getLogger().exception(event.getMessage().getAuthor().block(), "Failed to check for sufficient control role.", e, PermissionChecker.class);
			return true;
		}
		return true;
	}

	public static boolean hasSufficientRole(Guild guild, Member member) {
		//TODO: Figure out exactly what is causing a NPE here...
		try {
			GuildSettings settings = DatabaseManager.getManager().getSettings(guild.getId());
			if (!settings.getControlRole().equalsIgnoreCase("everyone")) {
				String roleId = settings.getControlRole();
				Role role = null;

				for (Role r : guild.getRoles().toIterable()) {
					if (r.getId().asString().equals(roleId)) {
						role = r;
						break;
					}
				}

				if (role != null) {
					for (Role r : member.getRoles().toIterable()) {
						if (r.getId().equals(role.getId()) || r.getPosition().block() > role.getPosition().block())
							return true;

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
			//Something broke so we will harmlessly allow access and notify the dev team
			Logger.getLogger().exception(member, "Failed to check for sufficient control role.", e, PermissionChecker.class);
			return true;
		}
		return true;
	}

	public static boolean hasManageServerRole(MessageCreateEvent event) {
		PermissionSet set = event.getMessage().getChannel().ofType(TextChannel.class)
				.flatMap(c -> c.getEffectivePermissions(event.getMember().get().getId())).block();

		return set.contains(Permission.MANAGE_GUILD);
	}

	public static boolean hasManageServerRole(Member m) {
		for (Role r : m.getRoles().toIterable()) {
			if (r.getPermissions().contains(Permission.MANAGE_GUILD))
				return true;
		}
		return false;
	}

	/**
	 * Checks if the user sent the command in a DisCal channel (if set).
	 *
	 * @param event The event received to check for the correct channel.
	 * @return <code>true</code> if in correct channel, otherwise <code>false</code>.
	 */
	public static boolean isCorrectChannel(MessageCreateEvent event, GuildSettings settings) {
		try {
			if (settings.getDiscalChannel().equalsIgnoreCase("all"))
				return true;


			GuildChannel channel = null;
			for (GuildChannel c : event.getGuild().block().getChannels().toIterable()) {
				if (c.getId().toString().equals(settings.getDiscalChannel())) {
					channel = c;
					break;
				}
			}

			if (channel != null)
				return event.getMessage().getChannel().block().getId().equals(channel.getId());

			//If we got here, the channel no longer exists, reset data and return true.
			settings.setDiscalChannel("all");
			DatabaseManager.getManager().updateSettings(settings);
			return true;
		} catch (Exception e) {
			//Catch any errors so that the bot always responds...
			Logger.getLogger().exception(event.getMessage().getAuthor().block(), "Failed to check for discal channel.", e, PermissionChecker.class);
			return true;
		}
	}

	public static boolean botHasMessageManagePerms(MessageCreateEvent event) {
		PermissionSet set = event.getMessage().getChannel().ofType(TextChannel.class)
				.flatMap(c -> c.getEffectivePermissions(c.getClient().getSelfId().get())).block();

		return set.contains(Permission.MANAGE_MESSAGES);
	}

}