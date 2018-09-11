package org.dreamexposure.discal.core.utils;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.GuildChannel;

/**
 * Created by Nova Fox on 3/29/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
@SuppressWarnings("ConstantConditions")
public class ChannelUtils {
	/**
	 * Checks if the specified channel exists.
	 *
	 * @param nameOrId The channel name or ID.
	 * @param event    The event received.
	 * @return <code>true</code> if exists, else <code>false</code>.
	 */
	public static boolean channelExists(String nameOrId, MessageCreateEvent event) {
		if (nameOrId.contains("#"))
			nameOrId = nameOrId.replace("#", "");

		for (GuildChannel c : event.getGuild().block().getChannels().toIterable()) {
			if (c.getName().equalsIgnoreCase(nameOrId) || c.getId().asString().equals(nameOrId))
				return true;
		}
		return false;
	}

	public static boolean channelExists(String nameOrId, Guild guild) {
		if (nameOrId.contains("#"))
			nameOrId = nameOrId.replace("#", "");

		for (GuildChannel c : guild.getChannels().toIterable()) {
			if (c.getName().equalsIgnoreCase(nameOrId) || c.getId().asString().equals(nameOrId))
				return true;
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
	public static GuildChannel getChannelFromNameOrId(String nameOrId, MessageCreateEvent event) {
		if (nameOrId.contains("#"))
			nameOrId = nameOrId.replace("#", "");

		for (GuildChannel c : event.getGuild().block().getChannels().toIterable()) {
			if (c.getName().equalsIgnoreCase(nameOrId) || c.getId().asString().equals(nameOrId))
				return c;
		}
		return null;
	}

	/**
	 * Gets the IChannel from its name.
	 *
	 * @param nameOrId The channel name or ID.
	 * @return the IChannel if successful, else <code>null</code>.
	 */
	public static GuildChannel getChannelFromNameOrId(String nameOrId, Guild guild) {
		if (nameOrId.contains("#"))
			nameOrId = nameOrId.replace("#", "");

		for (GuildChannel c : guild.getChannels().toIterable()) {
			if (c.getName().equalsIgnoreCase(nameOrId) || c.getId().asString().equals(nameOrId))
				return c;
		}
		return null;
	}

	/**
	 * Gets the IChannel from its name.
	 *
	 * @param nameOrId The channel name or ID.
	 * @return the IChannel if successful, else <code>null</code>.
	 */
	public static String getChannelNameFromNameOrId(String nameOrId, Guild guild) {
		GuildChannel channel = getChannelFromNameOrId(nameOrId, guild);
		if (channel != null)
			return channel.getName();
		else
			return "ERROR";
	}
}