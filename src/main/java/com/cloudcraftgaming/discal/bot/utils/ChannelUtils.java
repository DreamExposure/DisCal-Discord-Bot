package com.cloudcraftgaming.discal.bot.utils;

import com.cloudcraftgaming.discal.Main;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;

/**
 * Created by Nova Fox on 3/29/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class ChannelUtils {
	/**
	 * Checks if the specified channel exists.
	 *
	 * @param nameOrId The channel name or ID.
	 * @param event    The event received.
	 * @return <code>true</code> if exists, else <code>false</code>.
	 */
	public static Boolean channelExists(String nameOrId, MessageReceivedEvent event) {
		if (nameOrId.contains("#")) {
			nameOrId = nameOrId.replace("#", "");
		}
		for (IChannel c : event.getGuild().getChannels()) {
			if (c.getName().equalsIgnoreCase(nameOrId) || c.getStringID().equals(nameOrId)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Gets the IChannel from its name.
	 *
	 * @param nameOrId The channel name or ID.
	 * @param event    The event received.
	 * @return the IChannel if successful, else <code>null</code>.
	 */
	public static IChannel getChannelFromNameOrId(String nameOrId, MessageReceivedEvent event) {
		if (nameOrId.contains("#")) {
			nameOrId = nameOrId.replace("#", "");
		}
		for (IChannel c : event.getGuild().getChannels()) {
			if (c.getName().equalsIgnoreCase(nameOrId) || c.getStringID().equals(nameOrId)) {
				return c;
			}
		}
		return null;
	}

	/**
	 * Gets the IChannel from its name.
	 *
	 * @param nameOrId The channel name or ID.
	 * @param guildId  The ID of the guild this channel belongs to.
	 * @return the IChannel if successful, else <code>null</code>.
	 */
	public static IChannel getChannelFromNameOrId(String nameOrId, long guildId) {
		IGuild guild = Main.client.getGuildByID(guildId);
		if (nameOrId.contains("#")) {
			nameOrId = nameOrId.replace("#", "");
		}
		for (IChannel c : guild.getChannels()) {
			if (c.getName().equalsIgnoreCase(nameOrId) || c.getStringID().equals(nameOrId)) {
				return c;
			}
		}
		return null;
	}

	/**
	 * Gets the IChannel from its name.
	 *
	 * @param nameOrId The channel name or ID.
	 * @param guildId  The ID of the guild this channel belongs to.
	 * @return the IChannel if successful, else <code>null</code>.
	 */
	public static String getChannelNameFromNameOrId(String nameOrId, long guildId) {
		IGuild guild = Main.client.getGuildByID(guildId);
		if (nameOrId.contains("#")) {
			nameOrId = nameOrId.replace("#", "");
		}
		for (IChannel c : guild.getChannels()) {
			if (c.getName().equalsIgnoreCase(nameOrId) || c.getStringID().equals(nameOrId)) {
				return c.getName();
			}
		}
		return "ERROR";
	}
}