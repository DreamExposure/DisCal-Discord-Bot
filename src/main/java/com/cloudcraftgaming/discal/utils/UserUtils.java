package com.cloudcraftgaming.discal.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.RequestBuffer;

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



	public static long getUser(String toLookFor, IMessage m) {
		return getUser(toLookFor,m.getGuild());
	}

	/**
	 * Grabs a user from a string
	 *
	 * @param toLookFor The String to look with
	 * @param guild     The guild
	 * @return The user if found, null otherwise
	 */
	public static long getUser(String toLookFor, IGuild guild) {
		final IChannel debugging = guild.getClient().getChannelByID(267685015016570880L); // DEBUG
		toLookFor = GeneralUtils.trim(toLookFor);
		final String a = toLookFor; // DEBUG
		RequestBuffer.request(() -> debugging.sendMessage(String.format("toLookFor: --%s--", a))); // DEBUG
		final String lower = toLookFor.toLowerCase();
		RequestBuffer.request(() -> debugging.sendMessage(String.format("toLookFor.toLowerCase: --%s--", lower))); // DEBUG
		if (lower.matches("<@!?[0-9]+>") || lower.matches("[0-9]+")) {
			RequestBuffer.request(() -> debugging.sendMessage(String.format("TO LOOK FOR MATCHES USER REGEX")));// DEBUG
			final String parse = toLookFor.replaceAll("[<@!>]", "");
			RequestBuffer.request(() -> debugging.sendMessage(String.format("PARSE: `%s`", parse))); // DEBUG
			IUser exists = guild.getUserByID(Long.parseLong(toLookFor.replaceAll("[<@!>]", "")));
			RequestBuffer.request(() -> debugging.sendMessage(String.format("USER IS NULL? %s", exists == null))); // DEBUG
			if (exists != null) {
				RequestBuffer.request(() -> debugging.sendMessage(String.format("RETURNING ID %s WHICH IS EQUAL TO USER WITH NAME %s", exists.getStringID(), exists.getName()))); // DEBUG
				return exists.getLongID();
			}
		}

		RequestBuffer.request(() -> debugging.sendMessage(String.format("NOT A VALID USER PING OR ID", a))); // DEBUG


		List<IUser> users = new ArrayList<>();
		List<IUser> us = guild.getUsers();
		users.addAll(us.stream().filter(u -> u.getName().equalsIgnoreCase(lower)).collect(Collectors.toList()));
		users.addAll(us.stream().filter(u -> u.getName().toLowerCase().contains(lower)).collect(Collectors.toList()));
		users.addAll(us.stream().filter(u -> (u.getName() + "#" + u.getDiscriminator()).equalsIgnoreCase(lower)).collect(Collectors.toList()));
		users.addAll(us.stream().filter(u -> u.getDiscriminator().equalsIgnoreCase(lower)).collect(Collectors.toList()));
		users.addAll(us.stream().filter(u -> u.getDisplayName(guild).equalsIgnoreCase(lower)).collect(Collectors.toList()));
		users.addAll(us.stream().filter(u -> u.getDisplayName(guild).toLowerCase().contains(lower)).collect(Collectors.toList()));

		final StringBuilder builder = new StringBuilder(); // DEBUG
		users.forEach(r -> builder.append(r.getName()).append(", ")); // DEBUG
		RequestBuffer.request(() -> debugging.sendMessage(String.format("USER LIST: %s", builder.toString()))); // DEBUG

		if (!users.isEmpty()) {
			RequestBuffer.request(() -> debugging.sendMessage(String.format("NOT EMPTY RETURNING ID %s WHICH IS EQUAL TO ROLE WITH NAME %s", users.get(0).getStringID(), users.get(0).getName()))); // DEBUG
			return users.get(0).getLongID();
		}

		RequestBuffer.request(() -> debugging.sendMessage(String.format("RETURNING 0 (no role found)"))); // DEBUG

		return 0;
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

	public static IUser getUserFromID(String id, IGuild guild) {
		try {
			return guild.getUserByID(Long.parseUnsignedLong(id));
		} catch (Exception e) {
			//Ignore. Probably invalid ID.
			return null;
		}
	}

	public static ArrayList<IUser> getUsers(ArrayList<String> userIds, IGuild guild) {
		ArrayList<IUser> users = new ArrayList<>();
		for (String u : userIds) {
			IUser user = getUserFromID(u, guild);
			if (user != null) {
				users.add(user);
			}
		}
		return users;
	}
}