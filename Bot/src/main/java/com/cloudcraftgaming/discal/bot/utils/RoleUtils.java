package com.cloudcraftgaming.discal.bot.utils;

import com.cloudcraftgaming.discal.api.utils.GeneralUtils;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Nova Fox on 3/29/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class RoleUtils {
	public static IRole getRoleFromMention(String mention, MessageReceivedEvent event) {
		for (IRole r : event.getMessage().getGuild().getRoles()) {
			if (mention.equalsIgnoreCase("<@&" + r.getStringID() + ">") || mention.equalsIgnoreCase("<@&!" + r.getStringID() + ">")) {
				return r;
			}
		}
		return null;
	}

	public static IRole getRoleFromID(String id, MessageReceivedEvent event) {
		for (IRole r : event.getMessage().getGuild().getRoles()) {
			if (id.equals(r.getStringID()) || id.equals(r.getName())) {
				return r;
			}
		}
		return null;
	}

	public static IRole getRoleFromID(String id, IGuild guild) {
		for (IRole r : guild.getRoles()) {
			if (id.equalsIgnoreCase(r.getStringID()) || id.equals(r.getName())) {
				return r;
			}
		}
		return null;
	}

	public static boolean roleExists(String id, MessageReceivedEvent event) {
		for (IRole r : event.getMessage().getGuild().getRoles()) {
			if (id.equals(r.getStringID())) {
				return true;
			}
		}
		return false;
	}

	public static String getRoleNameFromID(String id, MessageReceivedEvent event) {
		IRole role = getRoleFromID(id, event);
		if (role != null) {
			return role.getName();
		} else {
			return "ERROR";
		}
	}


	public static long getRole(String toLookFor, IMessage m) {
		return getRole(toLookFor, m.getGuild());
	}

	public static long getRole(String toLookFor, IGuild guild) {
		toLookFor = GeneralUtils.trim(toLookFor);
		final String lower = toLookFor.toLowerCase();
		if (lower.matches("@&[0-9]+") || lower.matches("[0-9]+")) {
			final String parse = toLookFor.replaceAll("[<@&>]", "");
			IRole exists = guild.getRoleByID(Long.parseLong(toLookFor.replaceAll("[<@&>]", "")));
			if (exists != null) {
				return exists.getLongID();
			}
		}


		List<IRole> roles = new ArrayList<>();
		List<IRole> rs = guild.getRoles();
		roles.addAll(rs.stream().filter(r -> r.getName().equalsIgnoreCase(lower)).collect(Collectors.toList()));
		roles.addAll(rs.stream().filter(r -> r.getName().toLowerCase().contains(lower)).collect(Collectors.toList()));
		if (!roles.isEmpty()) {
			return roles.get(0).getLongID();
		}

		return 0;
	}
}