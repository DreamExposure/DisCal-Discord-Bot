package com.cloudcraftgaming.discal.utils;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Nova Fox on 3/29/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class UserUtils {
	public static IUser getUserFromMention(String mention, MessageReceivedEvent event) {
		for (IUser u : event.getGuild().getUsers()) {
			if (mention.equalsIgnoreCase("<@" + u.getStringID() + ">") || mention.equalsIgnoreCase("<@!" + u.getStringID() + ">")) {
				return u;
			}
		}

		return null;
	}


	public static long getUser(String toLookFor, IGuild guild) {
		return getUser(toLookFor, null, guild);
	}

	public static long getUser(String toLookFor, IMessage m) {
		return getUser(toLookFor, m, m.getGuild());
	}

	/**
	 * Gets a user on the guild
	 *
	 * @param toLookFor The name or ID, if the user was mentioned this can be anything
	 * @param m         The message, incase of mention
	 * @return The ID of the user found.
	 */
	public static long getUser(String toLookFor, IMessage m, IGuild guild) {
		toLookFor = toLookFor.trim();
		final String lower = toLookFor.toLowerCase();

		long res = 0;

		if (m != null && !m.getMentions().isEmpty())
			res = m.getMentions().get(0).getLongID();


		if (toLookFor.matches("[0-9]+")) {
			IUser u = guild.getUserByID(Long.parseUnsignedLong(toLookFor));
			if (u != null) {
				return Long.parseUnsignedLong(toLookFor);
			}
		}
		List<IUser> users = guild.getUsers().stream()
				.filter(u -> u.getName().toLowerCase().contains(lower)
						|| u.getName().equalsIgnoreCase(lower) || u.getStringID().equals(lower)
						|| u.getDisplayName(guild).toLowerCase().contains(lower)
						|| u.getDisplayName(guild).equalsIgnoreCase(lower))
				.collect(Collectors.toList());
		if (!users.isEmpty())
			res = users.get(0).getLongID();

		return res;
	}

	public static IUser getIUser(String toLookFor, IMessage m, IGuild guild) {
		toLookFor = toLookFor.trim();
		final String lower = toLookFor.toLowerCase();

		IUser res = null;

		if (m != null && !m.getMentions().isEmpty())
			res = m.getMentions().get(0);

		if (toLookFor.matches("<@!?[0-9]+>")) {
			IUser u = guild.getUserByID(Long.parseUnsignedLong(toLookFor.replaceAll("[^0-9]", "")));
			if (u != null) {
				return u;
			}
		}

		List<IUser> users = guild.getUsers().stream()
				.filter(u -> u.getName().toLowerCase().contains(lower)
						|| u.getName().equalsIgnoreCase(lower) || u.getStringID().equals(lower)
						|| u.getDisplayName(guild).toLowerCase().contains(lower)
						|| u.getDisplayName(guild).equalsIgnoreCase(lower))
				.collect(Collectors.toList());
		if (!users.isEmpty())
			res = users.get(0);

		return res;
	}

}