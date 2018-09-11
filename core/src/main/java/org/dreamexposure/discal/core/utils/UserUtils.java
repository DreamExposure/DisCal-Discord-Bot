package org.dreamexposure.discal.core.utils;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.util.Snowflake;

import java.util.ArrayList;
import java.util.List;

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


	public static Snowflake getUserID(String toLookFor, MessageCreateEvent m) {
		return getUserID(toLookFor, m.getGuild().block());
	}

	/**
	 * Grabs a user from a string
	 *
	 * @param toLookFor The String to look with
	 * @param guild     The guild
	 * @return The user if found, null otherwise
	 */
	@SuppressWarnings("OptionalGetWithoutIsPresent")
	public static Snowflake getUserID(String toLookFor, Guild guild) {
		toLookFor = GeneralUtils.trim(toLookFor);
		final String lower = toLookFor.toLowerCase();
		if (lower.matches("@!?[0-9]+") || lower.matches("[0-9]+")) {
			final String parse = toLookFor.replaceAll("[<@!>]", "");

			String finalToLookFor = toLookFor;
			Member exists = guild.getMembers().filter(m -> m.getId().asString().equalsIgnoreCase(finalToLookFor.replaceAll("[<@!>]", ""))).blockLast();
			if (exists != null)
				return exists.getId();
		}


		List<Member> users = new ArrayList<>();


		for (Member m : guild.getMembers().toIterable()) {
			if (m.getUsername().equalsIgnoreCase(lower))
				users.add(m);
			if (m.getUsername().toLowerCase().contains(lower))
				users.add(m);
			if ((m.getUsername() + "#" + m.getDiscriminator()).equalsIgnoreCase(lower))
				users.add(m);
			if (m.getDiscriminator().equalsIgnoreCase(lower))
				users.add(m);
			if (m.getDisplayName().equalsIgnoreCase(lower))
				users.add(m);
			if (m.getDisplayName().toLowerCase().equalsIgnoreCase(lower))
				users.add(m);
			if (m.getNickname().get().equalsIgnoreCase(lower))
				users.add(m);
			if (m.getNickname().get().toLowerCase().contains(lower))
				users.add(m);
		}


		if (!users.isEmpty())
			return users.get(0).getId();

		return Snowflake.of(0);
	}

	public static Member getUser(String toLookFor, Message m, Guild guild) {
		toLookFor = toLookFor.trim();
		final String lower = toLookFor.toLowerCase();

		Member res = null;

		if (m != null && m.getUserMentions().count().block() > 0)
			res = m.getUserMentions().blockFirst().asMember(guild.getId()).block();

		if (toLookFor.matches("<@!?[0-9]+>")) {
			Member u = getUserFromID(toLookFor.replaceAll("[^0-9]", ""), guild);
			if (u != null)
				return u;
		}

		return guild.getClient().getMemberById(guild.getId(), getUserID(toLookFor, guild)).block();
	}

	private static Member getUserFromID(String id, Guild guild) {
		try {
			return guild.getClient().getMemberById(guild.getId(), Snowflake.of(id)).block();
		} catch (Exception e) {
			//Ignore. Probably invalid ID.
			return null;
		}
	}

	public static ArrayList<User> getUsers(ArrayList<String> userIds, Guild guild) {
		ArrayList<User> users = new ArrayList<>();
		for (String u : userIds) {
			User user = getUserFromID(u, guild);
			if (user != null)
				users.add(user);
		}
		return users;
	}
}