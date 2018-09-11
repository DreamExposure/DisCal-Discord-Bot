package org.dreamexposure.discal.core.utils;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.Role;
import discord4j.core.object.util.Snowflake;

/**
 * Created by Nova Fox on 3/29/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
@SuppressWarnings("ConstantConditions")
public class RoleUtils {
	public static Role getRoleFromMention(String mention, MessageCreateEvent event) {
		for (Role r : event.getMessage().getGuild().block().getRoles().toIterable()) {
			if (mention.equalsIgnoreCase("<@&" + r.getId().asString() + ">") || mention.equalsIgnoreCase("<@&!" + r.getId().asString() + ">"))
				return r;
		}
		return null;
	}

	public static Role getRoleFromID(String id, MessageCreateEvent event) {
		for (Role r : event.getMessage().getGuild().block().getRoles().toIterable()) {
			if (id.equals(r.getId().asString()) || id.equals(r.getName()))
				return r;
		}
		return null;
	}

	public static Role getRoleFromID(String id, Guild guild) {
		for (Role r : guild.getRoles().toIterable()) {
			if (id.equalsIgnoreCase(r.getId().asString()) || id.equals(r.getName()))
				return r;
		}
		return null;
	}

	public static boolean roleExists(String id, MessageCreateEvent event) {
		for (Role r : event.getGuild().block().getRoles().toIterable()) {
			if (id.equals(r.getId().asString()))
				return true;
		}
		return false;
	}

	public static boolean roleExists(String id, Guild guild) {
		for (Role r : guild.getRoles().toIterable()) {
			if (id.equals(r.getId().asString()))
				return true;
		}
		return false;
	}

	public static String getRoleNameFromID(String id, MessageCreateEvent event) {
		Role role = getRoleFromID(id, event);
		if (role != null)
			return role.getName();
		else
			return "ERROR";
	}

	public static String getRoleNameFromID(String id, Guild guild) {
		Role role = getRoleFromID(id, guild);
		if (role != null)
			return role.getName();
		else
			return "ERROR";
	}

	public static Snowflake getRole(String toLookFor, Message m) {
		return getRole(toLookFor, m.getGuild().block());
	}

	public static Snowflake getRole(String toLookFor, Guild guild) {
		toLookFor = GeneralUtils.trim(toLookFor);
		final String lower = toLookFor.toLowerCase();
		if (lower.matches("@&[0-9]+") || lower.matches("[0-9]+")) {
			final String parse = toLookFor.replaceAll("[<@&>]", "");
			Role exists = getRoleFromID((toLookFor.replaceAll("[<@&>]", "")), guild);
			if (exists != null)
				return exists.getId();
		}

		return getRoleFromID(toLookFor, guild).getId();
	}

	public static Role getRoleFromSearch(String toLookFor, Guild guild) {
		toLookFor = GeneralUtils.trim(toLookFor);
		final String lower = toLookFor.toLowerCase();
		if (lower.matches("@&[0-9]+") || lower.matches("[0-9]+")) {
			final String parse = toLookFor.replaceAll("[<@&>]", "");
			Role exists = getRoleFromID((toLookFor.replaceAll("[<@&>]", "")), guild);
			if (exists != null)
				return exists;
		}

		return getRoleFromID(toLookFor, guild);
	}
}