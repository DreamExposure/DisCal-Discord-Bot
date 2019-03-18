package org.dreamexposure.discal.core.utils;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.util.Snowflake;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Nova Fox on 3/29/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
@SuppressWarnings("ConstantConditions")
public class UserUtils {
	public static Member getUserFromMention(String mention, MessageCreateEvent event) {
		for (Member u : event.getGuild().block().getMembers().toIterable()) {
			if (mention.equalsIgnoreCase("<@" + u.getId().asString() + ">") || mention.equalsIgnoreCase("<@!" + u.getId().asString() + ">"))
				return u;
		}

		return null;
	}

	public static Snowflake getUser(String toLookFor, Message m) {
		return getUser(toLookFor, m.getGuild().block());
	}

	/**
	 * Grabs a user from a string
	 *
	 * @param toLookFor The String to look with
	 * @param guild     The guild
	 * @return The user if found, null otherwise
	 */
	public static Snowflake getUser(String toLookFor, Guild guild) {
		toLookFor = GeneralUtils.trim(toLookFor);
		final String lower = toLookFor.toLowerCase();
		if (lower.matches("@!?[0-9]+") || lower.matches("[0-9]+")) {
			final String parse = toLookFor.replaceAll("[<@!>]", "");
			Member exists = guild.getMemberById(Snowflake.of(Long.parseLong(toLookFor.replaceAll("[<@!>]", "")))).block();
			if (exists != null)
				return exists.getId();
		}


		List<Member> users = new ArrayList<>();
		users.addAll(guild.getMembers().toStream().filter(u -> u.getUsername().equalsIgnoreCase(lower)).collect(Collectors.toList()));
		users.addAll(guild.getMembers().toStream().filter(u -> u.getUsername().toLowerCase().contains(lower)).collect(Collectors.toList()));
		users.addAll(guild.getMembers().toStream().filter(u -> (u.getUsername() + "#" + u.getDiscriminator()).equalsIgnoreCase(lower)).collect(Collectors.toList()));
		users.addAll(guild.getMembers().toStream().filter(u -> u.getDiscriminator().equalsIgnoreCase(lower)).collect(Collectors.toList()));
		users.addAll(guild.getMembers().toStream().filter(u -> u.getDisplayName().equalsIgnoreCase(lower)).collect(Collectors.toList()));
		users.addAll(guild.getMembers().toStream().filter(u -> u.getDisplayName().toLowerCase().contains(lower)).collect(Collectors.toList()));


		if (!users.isEmpty())
			return users.get(0).getId();

		return null;
	}

	public static Member getIUser(String toLookFor, Message m, Guild guild) {
		toLookFor = toLookFor.trim();
		final String lower = toLookFor.toLowerCase();

		Member res = null;

		if (m != null && m.getUserMentions().count().block() > 0)
			res = m.getUserMentions().blockFirst().asMember(guild.getId()).block();

		if (toLookFor.matches("<@!?[0-9]+>")) {
			Member u = guild.getMemberById(Snowflake.of(Long.parseUnsignedLong(toLookFor.replaceAll("[^0-9]", "")))).block();
			if (u != null)
				return u;
		}

		List<Member> users = guild.getMembers().toStream()
				.filter(u -> u.getUsername().toLowerCase().contains(lower)
					|| u.getUsername().equalsIgnoreCase(lower) || u.getId().asString().equals(lower)
						|| u.getDisplayName().toLowerCase().contains(lower)
						|| u.getDisplayName().equalsIgnoreCase(lower))
				.collect(Collectors.toList());
		if (!users.isEmpty())
			res = users.get(0);

		return res;
	}

	private static Member getUserFromID(String id, Guild guild) {
		try {
			return guild.getMemberById(Snowflake.of(Long.parseUnsignedLong(id))).block();
		} catch (Exception e) {
			//Ignore. Probably invalid ID.
			return null;
		}
	}

	public static ArrayList<Member> getUsers(ArrayList<String> userIds, Guild guild) {
		ArrayList<Member> users = new ArrayList<>();
		for (String u : userIds) {
			Member user = getUserFromID(u, guild);
			if (user != null)
				users.add(user);
		}
		return users;
	}
}