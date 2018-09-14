package org.dreamexposure.discal.server.handler;

import org.dreamexposure.discal.core.enums.network.CrossTalkReason;
import org.dreamexposure.discal.core.enums.network.DisCalRealm;
import org.dreamexposure.discal.core.logger.Logger;
import org.dreamexposure.discal.core.object.BotSettings;
import org.dreamexposure.discal.core.object.web.WebGuild;
import org.dreamexposure.discal.server.DisCalServer;
import org.dreamexposure.novautils.network.crosstalk.ServerSocketHandler;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

@SuppressWarnings({"unchecked", "RedundantCast", "Duplicates"})
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
		try {
			return discordAccounts.containsKey((String) request.getSession(true).getAttribute("account"));
		} catch (Exception e) {
			return false;
		}
	}

	//Getters
	public Map getAccount(HttpServletRequest request) {
		if ((String) request.getSession(true).getAttribute("account") != null && discordAccounts.containsKey((String) request.getSession(true).getAttribute("account"))) {
			Map m = discordAccounts.get((String) request.getSession(true).getAttribute("account"));
			m.remove("lastUse");
			m.put("lastUse", System.currentTimeMillis());

			m.remove("status");
			m.put("status", DisCalServer.getNetworkInfo());
			return m;
		} else {
			//Not logged in...
			Map m = new HashMap();
			m.put("loggedIn", false);
			m.put("client", BotSettings.ID.get());
			m.put("year", LocalDate.now().getYear());
			m.put("redirUri", BotSettings.REDIR_URI.get());
			m.put("status", DisCalServer.getNetworkInfo());
			return m;
		}
	}

	public Map getAccountForGuildEmbed(HttpServletRequest request, String guildId) {
		if ((String) request.getSession(true).getAttribute("account") != null && discordAccounts.containsKey((String) request.getSession(true).getAttribute("account"))) {
			Map m = discordAccounts.get((String) request.getSession(true).getAttribute("account"));
			m.remove("lastUse");
			m.put("lastUse", System.currentTimeMillis());
			m.remove("status");
			m.put("status", DisCalServer.getNetworkInfo());

			//Add guild for guild embed
			JSONObject requestBody = new JSONObject();
			requestBody.put("Reason", CrossTalkReason.GET.name());
			requestBody.put("Realm", DisCalRealm.WEBSITE_EMBED_CALENDAR);
			requestBody.put("Guild-Id", guildId);

			m.remove("embed");
			try {
				JSONObject data = ServerSocketHandler.sendAndReceive(requestBody);

				WebGuild wg = new WebGuild().fromJson(data.getJSONObject("Guild"));
				m.put("embed", wg);
			} catch (IOException e) {
				Logger.getLogger().exception(null, "[Embed] Failed to get guild!", e, this.getClass());

				m.put("embed", new WebGuild());
			}

			return m;
		} else {
			//Not logged in...
			Map m = new HashMap();
			m.put("loggedIn", false);
			m.put("client", BotSettings.ID.get());
			m.put("year", LocalDate.now().getYear());
			m.put("status", DisCalServer.getNetworkInfo());

			//Add guild for guild embed
			JSONObject requestBody = new JSONObject();
			requestBody.put("Reason", CrossTalkReason.GET.name());
			requestBody.put("Realm", DisCalRealm.WEBSITE_EMBED_CALENDAR);
			requestBody.put("Guild-Id", guildId);

			m.remove("embed");
			try {
				JSONObject data = ServerSocketHandler.sendAndReceive(requestBody);

				WebGuild wg = new WebGuild().fromJson(data.getJSONObject("Guild"));
				m.put("embed", wg);
			} catch (IOException e) {
				Logger.getLogger().exception(null, "[Embed] Failed to get guild!", e, this.getClass());

				m.put("embed", new WebGuild());
			}

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

	public void removeAccount(HttpServletRequest request) {
		if ((String) request.getSession(true).getAttribute("account") != null && hasAccount(request)) {
			discordAccounts.remove((String) request.getSession(true).getAttribute("account"));
		}
	}

	private void removeTimedOutAccounts() {
		long limit = Long.valueOf(BotSettings.TIME_OUT.get());
		final List<String> toRemove = new ArrayList<>();
		for (String id : discordAccounts.keySet()) {
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
