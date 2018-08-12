package com.cloudcraftgaming.discal.web.handler;

import com.cloudcraftgaming.discal.api.DisCalAPI;
import com.cloudcraftgaming.discal.api.object.BotSettings;
import com.cloudcraftgaming.discal.api.object.web.WebGuild;
import com.cloudcraftgaming.discal.api.utils.GuildUtils;
import sx.blah.discord.handle.obj.IGuild;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.*;

/**
 * Created by Nova Fox on 12/19/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
@SuppressWarnings({"unchecked", "RedundantCast"})
public class DiscordAccountHandler {
	private static DiscordAccountHandler instance;
	private static Timer timer;

	private HashMap<String, Map> discordAccounts = new HashMap<>();

	//Instance handling
	private DiscordAccountHandler() {
	} //Prevent initialization

	public static DiscordAccountHandler getHandler() {
		if (instance == null)
			instance = new DiscordAccountHandler();

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
		if (timer != null)
			timer.cancel();
	}

	//Boolean/checkers
	public boolean hasAccount(HttpServletRequest request) {
		return discordAccounts.containsKey((String) request.getSession(true).getAttribute("account"));
	}

	//Getters
	public Map getAccount(HttpServletRequest request) {
		if ((String) request.getSession(true).getAttribute("account") != null && discordAccounts.containsKey((String) request.getSession(true).getAttribute("account"))) {
			Map m = discordAccounts.get((String) request.getSession(true).getAttribute("account"));
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

	public Map getAccountForGuildEmbed(HttpServletRequest request, String guildId) {
		if ((String) request.getSession(true).getAttribute("account") != null && discordAccounts.containsKey((String) request.getSession(true).getAttribute("account"))) {
			Map m = discordAccounts.get((String) request.getSession(true).getAttribute("account"));
			m.remove("lastUse");
			m.put("lastUse", System.currentTimeMillis());

			//Add guild for guild embed
			m.remove("embed");
			IGuild g = DisCalAPI.getAPI().getClient().getGuildByID(Long.valueOf(guildId));
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
			IGuild g = DisCalAPI.getAPI().getClient().getGuildByID(Long.valueOf(guildId));
			WebGuild wg = new WebGuild().fromGuild(g);

			m.put("embed", wg);
			return m;
		}
	}

	public Map findAccount(String userId) {
		for (Map m: discordAccounts.values()) {
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
	public void addAccount(Map m, HttpServletRequest request) {
		discordAccounts.remove((String) request.getSession(true).getAttribute("account"));
		m.remove("lastUse");
		m.put("lastUse", System.currentTimeMillis());
		discordAccounts.put((String) request.getSession(true).getAttribute("account"), m);
	}

	public void appendAccount(Map m, HttpServletRequest request) {
		if (discordAccounts.containsKey((String) request.getSession(true).getAttribute("account"))) {
			Map exist = discordAccounts.get((String) request.getSession(true).getAttribute("account"));
			exist.remove("lastUse");
			exist.put("lastUse", System.currentTimeMillis());
			exist.putAll(m);
		} else {
			discordAccounts.put((String) request.getSession(true).getAttribute("account"), m);
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

	public void removeAccount(HttpServletRequest request) {
		if ((String) request.getSession(true).getAttribute("account") != null && hasAccount(request)) {
			discordAccounts.remove((String) request.getSession(true).getAttribute("account"));
		}
	}

	private void removeTimedOutAccounts() {
		long limit = Long.valueOf(BotSettings.TIME_OUT.get());
		final List<String> toRemove = new ArrayList<>();
		for (String id: discordAccounts.keySet()) {
			Map m = discordAccounts.get(id);
			long lastUse = (long) m.get("lastUse");
			if (System.currentTimeMillis() - lastUse > limit)
				toRemove.remove(id); //Timed out, remove account info and require sign in.
		}

		for (String id : toRemove) {
			discordAccounts.remove(id);
		}
	}
}