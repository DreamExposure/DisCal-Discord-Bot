package org.dreamexposure.discal.core.utils;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.Role;
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
public class RoleUtils {
	public static Role getRoleFromMention(String mention, MessageCreateEvent event) {
		for (Role r : event.getMessage().getGuild().block().getRoles().toIterable()) {
			if (mention.equalsIgnoreCase("<@&" + r.getId().toString() + ">") || mention.equalsIgnoreCase("<@&!" + r.getId().toString() + ">"))
				return r;
		}
		return null;
	}

	public static Role getRoleFromID(String id, MessageCreateEvent event) {
		for (Role r : event.getMessage().getGuild().block().getRoles().toIterable()) {
			if (id.equals(r.getId().toString()) || id.equals(r.getName()))
				return r;
		}
		return null;
	}

	public static Role getRoleFromID(String id, Guild guild) {
		for (Role r : guild.getRoles().toIterable()) {
			if (id.equalsIgnoreCase(r.getId().toString()) || id.equals(r.getName()))
				return r;
		}
		return null;
	}

	public static boolean roleExists(String id, MessageCreateEvent event) {
		for (Role r : event.getMessage().getGuild().block().getRoles().toIterable()) {
			if (id.equals(r.getId().toString()))
				return true;
		}
		return false;
	}

	public static boolean roleExists(String id, Guild guild) {
		for (Role r : guild.getRoles().toIterable()) {
			if (id.equals(r.getId().toString()))
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
			Role exists = guild.getRoleById(Snowflake.of(Long.parseLong(toLookFor.replaceAll("[<@&>]", "")))).block();
			if (exists != null)
				return exists.getId();
		}


		List<Role> roles = new ArrayList<>();
		roles.addAll(guild.getRoles().toStream().filter(r -> r.getName().equalsIgnoreCase(lower)).collect(Collectors.toList()));
		roles.addAll(guild.getRoles().toStream().filter(r -> r.getName().toLowerCase().contains(lower)).collect(Collectors.toList()));
		if (!roles.isEmpty())
			return roles.get(0).getId();

		return null;
	}
}