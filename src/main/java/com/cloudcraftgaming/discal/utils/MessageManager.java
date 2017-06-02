package com.cloudcraftgaming.discal.utils;

import com.cloudcraftgaming.discal.internal.data.GuildSettings;
import com.cloudcraftgaming.discal.internal.file.ReadFile;

import java.util.ArrayList;
import java.util.List;
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
		langs = ReadFile.readAllLangFiles();
	}

	public static boolean reloadLangs() {
		try {
			langs = ReadFile.readAllLangFiles();
			return true;
		} catch (Exception e) {
			ExceptionHandler.sendException(null, "Failed to reload lang files!", e, MessageManager.class);
			return false;
		}
	}

	public static List<String> getLangs() {
		List<String> allLangs = new ArrayList<>();

		allLangs.addAll(langs.keySet());

		return allLangs;
	}

	public static boolean isSupported(String _value) {
		for (String l : langs.keySet()) {
			if (l.equalsIgnoreCase(_value)) {
				return true;
			}
		}
		return false;
	}

	public static String getValidLang(String _value) {
		for (String l : langs.keySet()) {
			if (l.equalsIgnoreCase(_value)) {
				return l;
			}
		}
		return "ENGLISH";
	}


	public static String getMessage(String key, GuildSettings settings) {
		Map<String, String> messages;

		if (settings.getLang() != null && langs.containsKey(settings.getLang())) {
			messages = langs.get(settings.getLang());
		} else {
			messages = langs.get("ENGLISH");
		}

		return messages.getOrDefault(key, "***FAILSAFE MESSAGE*** MESSAGE NOT FOUND!! Message requested: " + key).replace("%lb%", Message.lineBreak);
	}

	public static String getMessage(String key, String var, String replace, GuildSettings settings) {
		Map<String, String> messages;

		if (settings.getLang() != null && langs.containsKey(settings.getLang())) {
			messages = langs.get(settings.getLang());
		} else {
			messages = langs.get("ENGLISH");
		}
		return messages.getOrDefault(key, "***FAILSAFE MESSAGE*** MESSAGE NOT FOUND!! Message requested: " + key).replace(var, replace).replace("%lb%", Message.lineBreak);
	}
}