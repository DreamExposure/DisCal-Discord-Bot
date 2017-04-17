package com.cloudcraftgaming.discal.utils;

import com.cloudcraftgaming.discal.Main;
import com.cloudcraftgaming.discal.database.DatabaseManager;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

/**
 * Created by Nova Fox on 4/14/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class MessageManager {
	@SuppressWarnings("unchecked")
	public static String getMessage(String key, MessageReceivedEvent event) {
		Language lang = DatabaseManager.getManager().getSettings(event.getMessage().getGuild().getID()).getLang();
		InputStream in = MessageManager.class.getResourceAsStream("/languages/" + lang.name() + ".json");
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));

		HashMap<String, String> messages = Main.gson.fromJson(reader, HashMap.class);

		return messages.getOrDefault(key, "***FAILSAFE MESSAGE*** MESSAGE NOT FOUND!!");
	}
}