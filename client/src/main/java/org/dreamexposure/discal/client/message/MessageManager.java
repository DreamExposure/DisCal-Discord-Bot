package org.dreamexposure.discal.client.message;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.entity.User;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.core.spec.MessageEditSpec;
import org.dreamexposure.discal.core.file.ReadFile;
import org.dreamexposure.discal.core.logger.Logger;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.utils.GlobalConst;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author NovaFox161
 * Date Created: 9/8/2018
 * For Project: DisCal-Discord-Bot
 * Author Website: https://www.novamaday.com
 * Company Website: https://www.dreamexposure.org
 * Contact: nova@dreamexposure.org
 */
public class MessageManager {
	private static JSONObject langs;

	//Lang handling
	public static boolean reloadLangs() {
		try {
			langs = ReadFile.readAllLangFiles();
			return true;
		} catch (Exception e) {
			Logger.getLogger().exception(null, "Failed to reload lang files!", e, MessageManager.class);
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	public static List<String> getLangs() {

		return new ArrayList<String>(langs.keySet());
	}

	public static boolean isSupported(String _value) {
		JSONArray names = langs.names();
		for (int i = 0; i < names.length(); i++) {
			if (_value.equalsIgnoreCase(names.getString(i)))
				return true;
		}
		return false;
	}

	public static String getValidLang(String _value) {
		JSONArray names = langs.names();
		for (int i = 0; i < names.length(); i++) {
			if (_value.equalsIgnoreCase(names.getString(i)))
				return names.getString(i);
		}
		return "ENGLISH";
	}

	public static String getMessage(String key, GuildSettings settings) {
		JSONObject messages;

		if (settings.getLang() != null && langs.has(settings.getLang()))
			messages = langs.getJSONObject(settings.getLang());
		else
			messages = langs.getJSONObject("ENGLISH");

		if (messages.has(key))
			return messages.getString(key).replace("%lb%", GlobalConst.lineBreak);
		else
			return "***FAILSAFE MESSAGE*** MESSAGE NOT FOUND!! Message requested: " + key;
	}

	public static String getMessage(String key, String var, String replace, GuildSettings settings) {
		JSONObject messages;

		if (settings.getLang() != null && langs.has(settings.getLang()))
			messages = langs.getJSONObject(settings.getLang());
		else
			messages = langs.getJSONObject("ENGLISH");

		if (messages.has(key))
			return messages.getString(key).replace(var, replace).replace("%lb%", GlobalConst.lineBreak);
		else
			return "***FAILSAFE MESSAGE*** MESSAGE NOT FOUND!! Message requested: " + key;
	}

	//Message sending
	public static void sendMessageAsync(String message, TextChannel channel) {
		channel.createMessage(new MessageCreateSpec().setContent(message)).subscribe();
	}

	public static void sendMessageAsync(EmbedCreateSpec embed, TextChannel channel) {
		channel.createMessage(new MessageCreateSpec().setEmbed(embed)).subscribe();
	}

	public static void sendMessageAsync(String message, EmbedCreateSpec embed, TextChannel channel) {
		channel.createMessage(new MessageCreateSpec().setContent(message).setEmbed(embed)).subscribe();
	}

	public static void sendMessageAsync(String message, MessageCreateEvent event) {
		event.getMessage().getChannel().flatMap(c -> c.createMessage(new MessageCreateSpec().setContent(message))).subscribe();
	}

	public static void sendMessageAsync(EmbedCreateSpec embed, MessageCreateEvent event) {
		event.getMessage().getChannel().flatMap(c -> c.createMessage(new MessageCreateSpec().setEmbed(embed))).subscribe();
	}

	public static void sendMessageAsync(String message, EmbedCreateSpec embed, MessageCreateEvent event) {
		event.getMessage().getChannel().flatMap(c -> c.createMessage(new MessageCreateSpec().setContent(message).setEmbed(embed))).subscribe();
	}

	public static Message sendMessageSync(String message, MessageCreateEvent event) {
		return event.getMessage().getChannel().flatMap(c -> c.createMessage(new MessageCreateSpec().setContent(message))).block();
	}

	public static Message sendMessageSync(EmbedCreateSpec embed, MessageCreateEvent event) {
		return event.getMessage().getChannel().flatMap(c -> c.createMessage(new MessageCreateSpec().setEmbed(embed))).block();
	}

	public static Message sendMessageSync(String message, EmbedCreateSpec embed, MessageCreateEvent event) {
		return event.getMessage().getChannel().flatMap(c -> c.createMessage(new MessageCreateSpec().setContent(message).setEmbed(embed))).block();
	}

	public static Message sendMessageSync(String message, TextChannel channel) {
		return channel.createMessage(new MessageCreateSpec().setContent(message)).block();
	}

	public static Message sendMessageSync(EmbedCreateSpec embed, TextChannel channel) {
		return channel.createMessage(new MessageCreateSpec().setEmbed(embed)).block();
	}

	public static Message sendMessageSync(String message, EmbedCreateSpec embed, TextChannel channel) {
		return channel.createMessage(new MessageCreateSpec().setContent(message).setEmbed(embed)).block();
	}

	public static void sendDirectMessageAsync(String message, User user) {
		user.getPrivateChannel().flatMap(c -> c.createMessage(new MessageCreateSpec().setContent(message))).subscribe();
	}

	public static void sendDirectMessageAsync(EmbedCreateSpec embed, User user) {
		user.getPrivateChannel().flatMap(c -> c.createMessage(new MessageCreateSpec().setEmbed(embed))).subscribe();
	}

	public static void sendDirectMessageAsync(String message, EmbedCreateSpec embed, User user) {
		user.getPrivateChannel().flatMap(c -> c.createMessage(new MessageCreateSpec().setContent(message).setEmbed(embed))).subscribe();
	}

	public static Message sendDirectMessageSync(String message, User user) {
		return user.getPrivateChannel().flatMap(c -> c.createMessage(new MessageCreateSpec().setContent(message))).block();
	}

	public static Message sendDirectMessageSync(EmbedCreateSpec embed, User user) {
		return user.getPrivateChannel().flatMap(c -> c.createMessage(new MessageCreateSpec().setEmbed(embed))).block();
	}

	public static Message sendDirectMessageSync(String message, EmbedCreateSpec embed, User user) {
		return user.getPrivateChannel().flatMap(c -> c.createMessage(new MessageCreateSpec().setContent(message).setEmbed(embed))).block();
	}

	//Message editing
	public static void editMessage(String message, Message original) {
		original.edit(new MessageEditSpec().setContent(message)).subscribe();
	}

	public static void editMessage(String message, EmbedCreateSpec embed, Message original) {
		original.edit(new MessageEditSpec().setContent(message).setEmbed(embed)).subscribe();
	}

	public static void editMessage(String message, MessageCreateEvent event) {
		event.getMessage().edit(new MessageEditSpec().setContent(message)).subscribe();
	}

	public static void editMessage(String message, EmbedCreateSpec embed, MessageCreateEvent event) {
		event.getMessage().edit(new MessageEditSpec().setContent(message).setEmbed(embed)).subscribe();
	}

	//Message deleting
	public static void deleteMessage(Message message) {
		message.delete().subscribe();
	}

	public static void deleteMessage(MessageCreateEvent event) {
		event.getMessage().delete().subscribe();
	}
}