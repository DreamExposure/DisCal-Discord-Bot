package com.cloudcraftgaming.discal.web.handler;

import com.cloudcraftgaming.discal.Main;
import com.cloudcraftgaming.discal.api.object.BotSettings;
import com.cloudcraftgaming.discal.api.object.web.WebGuild;
import com.cloudcraftgaming.discal.api.utils.GuildUtils;
import sx.blah.discord.handle.obj.IGuild;

import java.time.LocalDate;
import java.util.*;

/**
 * Created by Nova Fox on 12/19/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
@SuppressWarnings("unchecked")
public class DiscordAccountHandler {
	private static DiscordAccountHandler instance;
	private static Timer timer;

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

	public void init() {
		if (BotSettings.RUN_API.get().equalsIgnoreCase("true")) {
			timer = new Timer(true);
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					removeTimedOutAccounts();
				}
			}, 60 * 30 * 1000);
		}
	}

	public void shutdown() {
		if (timer != null) {
			timer.cancel();
		}
	}

	//Boolean/checkers
	public boolean hasAccount(String sessionId) {
		return discordAccounts.containsKey(sessionId);
	}

	//Getters
	public Map getAccount(String sessionId) {
		if (discordAccounts.containsKey(sessionId)) {
			Map m = discordAccounts.get(sessionId);
			m.remove("lastUse");
			m.put("lastUse", System.currentTimeMillis());
			return m;
		} else {
			//Not logged in...
			Map m = new HashMap();
			m.put("loggedIn", false);
			m.put("client", BotSettings.ID.get());
			m.put("year", LocalDate.now().getYear());
			m.put("redirUri", BotSettings.REDIR_URI.get());
			return m;
		}
	}

	public Map getAccountForGuildEmbed(String sessionId, String guildId) {
		if (discordAccounts.containsKey(sessionId)) {
			Map m = discordAccounts.get(sessionId);
			m.remove("lastUse");
			m.put("lastUse", System.currentTimeMillis());

			//Add guild for guild embed
			m.remove("embed");
			IGuild g = Main.client.getGuildByID(Long.valueOf(guildId));
			WebGuild wg = new WebGuild().fromGuild(g);

			m.put("embed", wg);

			return m;
		} else {
			//Not logged in...
			Map m = new HashMap();
			m.put("loggedIn", false);
			m.put("client", BotSettings.ID.get());
			m.put("year", LocalDate.now().getYear());

			//Add guild for guild embed
			IGuild g = Main.client.getGuildByID(Long.valueOf(guildId));
			WebGuild wg = new WebGuild().fromGuild(g);

			m.put("embed", wg);
			return m;
		}
	}

	public Map findAccount(String userId) {
		for (Map m : discordAccounts.values()) {
			if (m.containsKey("id")) {
				if (m.get("id").equals(userId)) {
					m.remove("lastUse");
					m.put("lastUse", System.currentTimeMillis());
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
		discordAccounts.remove(sessionId);
		m.remove("lastUse");
		m.put("lastUse", System.currentTimeMillis());
		discordAccounts.put(sessionId, m);
	}

	public void appendAccount(Map m, String sessionId) {
		if (discordAccounts.containsKey(sessionId)) {
			Map exist = discordAccounts.get(sessionId);
			exist.remove("lastUse");
			exist.put("lastUse", System.currentTimeMillis());
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
			m.remove("lastUse");
			m.put("lastUse", System.currentTimeMillis());
		}
	}

	public void removeAccount(String sessionId) {
		if (hasAccount(sessionId)) {
			discordAccounts.remove(sessionId);
		}
	}

	private void removeTimedOutAccounts() {
		long limit = Long.valueOf(BotSettings.TIME_OUT.get());
		final List<String> toRemove = new ArrayList<>();
		for (String id : discordAccounts.keySet()) {
			Map m = discordAccounts.get(id);
			long lastUse = (long) m.get("lastUse");
			if (System.currentTimeMillis() - lastUse > limit) {
				//Timed out, remove account info and require sign in.
				toRemove.remove(id);
			}
		}

		for (String id : toRemove) {
			discordAccounts.remove(id);
		}
	}
}