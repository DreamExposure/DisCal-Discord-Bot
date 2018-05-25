package com.cloudcraftgaming.discal.api.message;

import com.cloudcraftgaming.discal.api.file.ReadFile;
import com.cloudcraftgaming.discal.api.object.GuildSettings;
import com.cloudcraftgaming.discal.api.utils.MessageUtils;
import com.cloudcraftgaming.discal.logger.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Nova Fox on 11/10/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
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
			Logger.getLogger().exception(null, "Failed to reload lang files!", e, MessageManager.class, true);
			return false;
		}
	}

	public static List<String> getLangs() {

		return new ArrayList<>(langs.keySet());
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

		return messages.getOrDefault(key, "***FAILSAFE MESSAGE*** MESSAGE NOT FOUND!! Message requested: " + key).replace("%lb%", MessageUtils.lineBreak);
	}

	public static String getMessage(String key, String var, String replace, GuildSettings settings) {
		Map<String, String> messages;

		if (settings.getLang() != null && langs.containsKey(settings.getLang())) {
			messages = langs.get(settings.getLang());
		} else {
			messages = langs.get("ENGLISH");
		}
		return messages.getOrDefault(key, "***FAILSAFE MESSAGE*** MESSAGE NOT FOUND!! Message requested: " + key).replace(var, replace).replace("%lb%", MessageUtils.lineBreak);
	}
}