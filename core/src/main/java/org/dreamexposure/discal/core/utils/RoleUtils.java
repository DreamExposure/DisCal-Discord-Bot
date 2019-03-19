package org.dreamexposure.discal.core.utils;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.Role;
import discord4j.core.object.util.Snowflake;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nova Fox on 3/29/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
@SuppressWarnings("ConstantConditions")
public class RoleUtils {
	public static Role getRoleFromID(String id, MessageCreateEvent event) {
		for (Role r : event.getMessage().getGuild().block().getRoles().toIterable()) {
			if (id.equals(r.getId().asString()) || id.equals(r.getName()))
				return r;
		}
		return null;
	}

	public static boolean roleExists(String id, MessageCreateEvent event) {
		for (Role r : event.getMessage().getGuild().block().getRoles().toIterable()) {
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

	public static Snowflake getRole(String toLookFor, Message m) {
		return getRole(toLookFor, m.getGuild().block());
	}

	public static Snowflake getRole(String toLookFor, Guild guild) {
		toLookFor = GeneralUtils.trim(toLookFor);
		final String lower = toLookFor.toLowerCase();
		if (lower.matches("@&[0-9]+") || lower.matches("[0-9]+")) {
			Role exists = guild.getRoleById(Snowflake.of(Long.parseLong(toLookFor.replaceAll("[<@&>]", "")))).onErrorResume(e -> Mono.empty()).block();
			if (exists != null)
				return exists.getId();
		}


		List<Role> roles = new ArrayList<>();

		roles.addAll(guild.getRoles().filter(r -> r.getName().equalsIgnoreCase(lower)).collectList().block());
		roles.addAll(guild.getRoles().filter(r -> r.getName().toLowerCase().contains(lower)).collectList().block());

		if (!roles.isEmpty())
			return roles.get(0).getId();

		return null;
	}
}