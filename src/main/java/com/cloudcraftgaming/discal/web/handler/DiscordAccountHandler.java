package com.cloudcraftgaming.discal.web.handler;

import com.cloudcraftgaming.discal.api.object.BotSettings;
import com.cloudcraftgaming.discal.api.utils.GuildUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Nova Fox on 12/19/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
@SuppressWarnings("unchecked")
public class DiscordAccountHandler {
	private static DiscordAccountHandler instance;

	private HashMap<String, Map> discordAccounts = new HashMap<>();

	//Instance handling
	private DiscordAccountHandler() {
	} //Prevent initialization

	public static DiscordAccountHandler getHandler() {
		if (instance == null) {
			instance = new DiscordAccountHandler();
		}
		return instance;
	}

	//Boolean/checkers
	public boolean hasAccount(String sessionId) {
		return discordAccounts.containsKey(sessionId);
	}

	//Getters
	public Map getAccount(String sessionId) {
		if (discordAccounts.containsKey(sessionId)) {
			return discordAccounts.get(sessionId);
		} else {
			//Not logged in...
			Map m = new HashMap();
			m.put("loggedIn", false);
			m.put("client", BotSettings.ID.get());
			return m;
		}
	}

	public Map findAccount(String userId) {
		for (Map m : discordAccounts.values()) {
			if (m.containsKey("id")) {
				if (m.get("id").equals(userId)) {
					return m;
				}
			}
		}
		return null;
	}

	public int accountCount() {
		return discordAccounts.size();
	}

	//Functions
	public void addAccount(Map m, String sessionId) {
		if (discordAccounts.containsKey(sessionId)) {
			discordAccounts.remove(sessionId);
		}
		discordAccounts.put(sessionId, m);
	}

	public void appendAccount(Map m, String sessionId) {
		if (discordAccounts.containsKey(sessionId)) {
			Map exist = discordAccounts.get(sessionId);
			exist.putAll(m);
		} else {
			discordAccounts.put(sessionId, m);
		}
	}

	public void updateAccount(String userId) {
		Map m = findAccount(userId);
		if (m != null) {
			m.remove("guilds");
			m.put("guilds", GuildUtils.getGuilds(userId));
		}
	}

	public void removeAccount(String sessionId) {
		if (hasAccount(sessionId)) {
			discordAccounts.remove(sessionId);
		}
	}
}