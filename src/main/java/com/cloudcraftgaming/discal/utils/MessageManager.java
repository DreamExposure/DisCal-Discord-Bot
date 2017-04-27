package com.cloudcraftgaming.discal.utils;

import com.cloudcraftgaming.discal.Main;
import com.cloudcraftgaming.discal.database.DatabaseManager;
import com.cloudcraftgaming.discal.internal.data.GuildSettings;
import com.cloudcraftgaming.discal.internal.file.ReadFile;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Nova Fox on 4/14/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
@SuppressWarnings("unchecked")
public class MessageManager {
	private static Map<String, Map<String, String>> langs;

	public static void loadLangs() {
		try {
			langs = ReadFile.readAllLangFiles();
		} catch (Exception e) {
			ExceptionHandler.sendException(null, "Failed to load lang files!", e, MessageManager.class);
		}
	}

	public static void reloadLangs() {
		try {
			langs = ReadFile.readAllLangFiles();
		} catch (Exception e) {
			ExceptionHandler.sendException(null, "Failed to reload lang files!", e, MessageManager.class);
		}
	}



	public static String getMessage(String key, MessageReceivedEvent event) {
		try {
			Language lang = DatabaseManager.getManager().getSettings(event.getMessage().getGuild().getID()).getLang();
			InputStream in = MessageManager.class.getClassLoader().getResourceAsStream("languages/" + lang.name() + ".json");
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));

			HashMap<String, String> messages = Main.gson.fromJson(reader, HashMap.class);

			return messages.getOrDefault(key, "***FAILSAFE MESSAGE*** MESSAGE NOT FOUND!!").replaceAll("%lb%", Message.lineBreak);
		} catch (Exception e) {
			ExceptionHandler.sendException(event.getMessage().getAuthor(), "Fuck you messages", e, MessageManager.class);
		}
		return "***MESSAGES BROKE*** I'm working so hard, please understand :/";
	}

	public static String getMessage(String key, String var, String replace, MessageReceivedEvent event) {
		try {
			Language lang = DatabaseManager.getManager().getSettings(event.getMessage().getGuild().getID()).getLang();
			InputStream in = MessageManager.class.getResourceAsStream("languages/" + lang.name() + ".json");
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));

			HashMap<String, String> messages = Main.gson.fromJson(reader, HashMap.class);

			return messages.getOrDefault(key, "***FAILSAFE MESSAGE*** MESSAGE NOT FOUND!!").replaceAll(var, replace).replaceAll("%lb%", Message.lineBreak);
		} catch (Exception e) {
			ExceptionHandler.sendException(event.getMessage().getAuthor(), "More damn errors", e, MessageManager.class);
		}
		return "***MESSAGES BROKE*** I know. Im sorry. Please. I am sorry.";
	}

	public static String getMessage(String key, String guildId) {
		Language lang = DatabaseManager.getManager().getSettings(guildId).getLang();
		InputStream in = MessageManager.class.getResourceAsStream("/languages/" + lang.name() + ".json");
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));

		HashMap<String, String> messages = Main.gson.fromJson(reader, HashMap.class);

		return messages.getOrDefault(key, "***FAILSAFE MESSAGE*** MESSAGE NOT FOUND!!").replaceAll("%lb%", Message.lineBreak);
	}

	public static String getMessage(String key, String var, String replace, String guildId) {
		Language lang = DatabaseManager.getManager().getSettings(guildId).getLang();
		InputStream in = MessageManager.class.getResourceAsStream("/languages/" + lang.name() + ".json");
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));

		HashMap<String, String> messages = Main.gson.fromJson(reader, HashMap.class);

		return messages.getOrDefault(key, "***FAILSAFE MESSAGE*** MESSAGE NOT FOUND!!").replaceAll(var, replace).replaceAll("%lb%", Message.lineBreak);
	}

	public static String getMessage(String key, GuildSettings settings) {
		InputStream in = MessageManager.class.getResourceAsStream("/languages/" + settings.getLang().name() + ".json");
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));

		HashMap<String, String> messages = Main.gson.fromJson(reader, HashMap.class);

		return messages.getOrDefault(key, "***FAILSAFE MESSAGE*** MESSAGE NOT FOUND!!").replaceAll("%lb%", Message.lineBreak);
	}

	public static String getMessage(String key, String var, String replace, GuildSettings settings) {
		InputStream in = MessageManager.class.getResourceAsStream("/languages/" + settings.getLang().name() + ".json");
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));

		HashMap<String, String> messages = Main.gson.fromJson(reader, HashMap.class);

		return messages.getOrDefault(key, "***FAILSAFE MESSAGE*** MESSAGE NOT FOUND!!").replaceAll(var, replace).replaceAll("%lb%", Message.lineBreak);
	}
}