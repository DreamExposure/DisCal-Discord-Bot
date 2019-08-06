package org.dreamexposure.discal.server.handler;

import org.dreamexposure.discal.core.logger.Logger;
import org.dreamexposure.discal.core.object.BotSettings;
import org.dreamexposure.discal.core.object.network.discal.ConnectedClient;
import org.dreamexposure.discal.core.object.web.WebGuild;
import org.dreamexposure.discal.core.utils.GlobalConst;
import org.dreamexposure.discal.server.DisCalServer;
import org.json.JSONObject;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@SuppressWarnings({"unchecked", "RedundantCast", "Duplicates", "WeakerAccess", "unused", "ConstantConditions"})
public class DiscordAccountHandler {
	private static DiscordAccountHandler instance;
	private static Timer timer;

	private HashMap<String, Map> discordAccounts = new HashMap<>();
	private HashMap<String, Map> embedMaps = new HashMap<>();

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

	public boolean hasEmbedMap(HttpServletRequest request) {
		try {
			return embedMaps.containsKey((String) request.getSession(true).getAttribute("embed"));
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

			//Remove from embed map just in case...
			removeEmbedMap(request);

			return m;

		} else {
			//Not logged in...
			Map m = new HashMap();
			m.put("loggedIn", false);
			m.put("client", BotSettings.ID.get());
			m.put("year", LocalDate.now().getYear());
			m.put("redirUri", BotSettings.REDIR_URI.get());
			m.put("status", DisCalServer.getNetworkInfo());

			//Remove from embed map just in case...
			removeEmbedMap(request);

			return m;
		}
	}

	public Map getEmbedMap(HttpServletRequest request) {
		return embedMaps.get((String) request.getSession(true).getAttribute("embed"));
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
			requestBody.put("guild_id", guildId);

			m.remove("embed");
			try {
				OkHttpClient client = new OkHttpClient.Builder()
					.connectTimeout(1, TimeUnit.SECONDS)
					.build();
				RequestBody httpRequestBody = RequestBody.create(GlobalConst.JSON, requestBody.toString());

				for (ConnectedClient cc : DisCalServer.getNetworkInfo().getClients()) {

					try {
						Request httpRequest = new Request.Builder()
							.url("https://" + BotSettings.COM_SUB_DOMAIN.get() + cc.getClientIndex() + ".discalbot.com/api/v1/com/website/embed/calendar")
							.post(httpRequestBody)
							.header("Content-Type", "application/json")
							.header("Authorization", Credentials.basic(BotSettings.COM_USER.get(), BotSettings.COM_PASS.get()))
							.build();

						Response response = client.newCall(httpRequest).execute();

						if (response.code() == 200) {
							JSONObject responseBody = new JSONObject(response.body().string());

							WebGuild wg = new WebGuild().fromJson(responseBody.getJSONObject("guild"));
							m.put("embed", wg);
							break; //We got the info, no need to request from the rest
						} else if (response.code() >= 500) {
							//Client must be down... lets remove it...
							DisCalServer.getNetworkInfo().removeClient(cc.getClientIndex());
						}
					} catch (Exception e) {
						Logger.getLogger().exception(null, "Client response error", e, true, DiscordAccountHandler.class);
						//Remove client to be on the safe side. If client is still up, it'll be re-added on the next keepalive
						DisCalServer.getNetworkInfo().removeClient(cc.getClientIndex());

					}
				}
			} catch (Exception e) {
				Logger.getLogger().exception(null, "[Embed] Failed to get guild!", e, true, this.getClass());

				m.put("embed", new WebGuild());
			}

			//Add to embed map...
			UUID embedKey = UUID.randomUUID();
			request.getSession(true).setAttribute("embed", embedKey.toString());
			embedMaps.put(embedKey.toString(), m);

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
			requestBody.put("guild_id", guildId);

			m.remove("embed");
			try {
				OkHttpClient client = new OkHttpClient.Builder()
					.connectTimeout(1, TimeUnit.SECONDS)
					.build();
				RequestBody httpRequestBody = RequestBody.create(GlobalConst.JSON, requestBody.toString());

				for (ConnectedClient cc : DisCalServer.getNetworkInfo().getClients()) {

					try {
						Request httpRequest = new Request.Builder()
							.url("https://" + BotSettings.COM_SUB_DOMAIN.get() + cc.getClientIndex() + ".discalbot.com/api/v1/com/website/embed/calendar")
							.post(httpRequestBody)
							.header("Content-Type", "application/json")
							.header("Authorization", Credentials.basic(BotSettings.COM_USER.get(), BotSettings.COM_PASS.get()))
							.build();

						Response response = client.newCall(httpRequest).execute();

						if (response.code() == 200) {
							JSONObject responseBody = new JSONObject(response.body().string());

							WebGuild wg = new WebGuild().fromJson(responseBody.getJSONObject("guild"));
							m.put("embed", wg);
							break; //We got the info, no need to request from the rest
						} else if (response.code() >= 500) {
							//Client must be down... lets remove it...
							DisCalServer.getNetworkInfo().removeClient(cc.getClientIndex());
						}
					} catch (Exception e) {
						Logger.getLogger().exception(null, "Client response error", e, true, DiscordAccountHandler.class);
						//Remove client to be on the safe side. If client is still up, it'll be re-added on the next keepalive
						DisCalServer.getNetworkInfo().removeClient(cc.getClientIndex());

					}
				}
			} catch (Exception e) {
				Logger.getLogger().exception(null, "[Embed] Failed to get guild!", e, true, this.getClass());

				m.put("embed", new WebGuild());
			}

			//Add to embed map...
			UUID embedKey = UUID.randomUUID();
			request.getSession(true).setAttribute("embed", embedKey.toString());
			embedMaps.put(embedKey.toString(), m);

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
		if ((String) request.getSession(true).getAttribute("account") != null && hasAccount(request))
			discordAccounts.remove((String) request.getSession(true).getAttribute("account"));
	}

	public void removeEmbedMap(HttpServletRequest request) {
		if ((String) request.getSession(true).getAttribute("embed") != null && hasEmbedMap(request))
			embedMaps.remove((String) request.getSession(true).getAttribute("embed"));
	}

	private void removeTimedOutAccounts() {
		long limit = Long.parseLong(BotSettings.TIME_OUT.get());
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
