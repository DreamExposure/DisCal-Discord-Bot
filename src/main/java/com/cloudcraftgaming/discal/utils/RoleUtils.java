package com.cloudcraftgaming.discal.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.util.RequestBuffer;

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
		final IChannel debugging = guild.getClient().getChannelByID(267685015016570880L); // DEBUG
		toLookFor = GeneralUtils.trim(toLookFor);
		final String a = toLookFor; // DEBUG
		RequestBuffer.request(() -> debugging.sendMessage(String.format("toLookFor: --%s--", a))); // DEBUG
		final String lower = toLookFor.toLowerCase();
		RequestBuffer.request(() -> debugging.sendMessage(String.format("toLookFor.toLowerCase: --%s--", lower))); // DEBUG

		if (lower.matches("<@&[0-9]+>") || lower.matches("[0-9]+")) {
			RequestBuffer.request(() -> debugging.sendMessage(String.format("TO LOOK FOR MATCHES ROLE REGEX")));// DEBUG
			final String parse = toLookFor.replaceAll("[<@&>]", "");
			RequestBuffer.request(() -> debugging.sendMessage(String.format("PARSE: `%s`", parse))); // DEBUG
			IRole exists = guild.getRoleByID(Long.parseLong(toLookFor.replaceAll("[<@&>]", "")));
			RequestBuffer.request(() -> debugging.sendMessage(String.format("ROLE IS NULL? %s", exists == null))); // DEBUG
			if (exists != null) {
				RequestBuffer.request(() -> debugging.sendMessage(String.format("RETURNING ID %s WHICH IS EQUAL TO ROLE WITH NAME %s", exists.getStringID(), exists.getName()))); // DEBUG
				return exists.getLongID();
			}
		}

		RequestBuffer.request(() -> debugging.sendMessage(String.format("NOT A VALID ROLE PING OR ID", a))); // DEBUG

		List<IRole> roles = new ArrayList<>();
		List<IRole> rs = guild.getRoles();
		roles.addAll(rs.stream().filter(r -> r.getName().equalsIgnoreCase(lower)).collect(Collectors.toList()));
		roles.addAll(rs.stream().filter(r -> r.getName().toLowerCase().contains(lower)).collect(Collectors.toList()));
		final StringBuilder builder = new StringBuilder(); // DEBUG
		roles.forEach(r -> builder.append(r.getName()).append(", ")); // DEBUG
		RequestBuffer.request(() -> debugging.sendMessage(String.format("ROLE LIST: %s", builder.toString()))); // DEBUG
		if (!roles.isEmpty()) {
			RequestBuffer.request(() -> debugging.sendMessage(String.format("NOT EMPTY RETURNING ID %s WHICH IS EQUAL TO ROLE WITH NAME %s", roles.get(0).getStringID(), roles.get(0).getName()))); // DEBUG
			return roles.get(0).getLongID();
		}
		RequestBuffer.request(() -> debugging.sendMessage(String.format("RETURNING 0 (no role found)"))); // DEBUG

		return 0;
	}
}