package org.dreamexposure.discal.web.handler;

import org.dreamexposure.discal.core.logger.Logger;
import org.dreamexposure.discal.core.object.BotSettings;
import org.dreamexposure.discal.core.utils.GlobalConst;
import org.json.JSONObject;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.http.HttpServletRequest;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@SuppressWarnings({"RedundantCast", "Duplicates", "WeakerAccess", "ConstantConditions"})
public class DiscordAccountHandler {
	private static DiscordAccountHandler instance;
	private static Timer timer;

	private HashMap<String, Map<String, Object>> discordAccounts = new HashMap<>();

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
	public Map<String, Object> getAccount(HttpServletRequest request) {
		if ((String) request.getSession(true).getAttribute("account") != null && discordAccounts.containsKey((String) request.getSession(true).getAttribute("account"))) {
			Map<String, Object> m = discordAccounts.get((String) request.getSession(true).getAttribute("account"));
			m.remove("last_use");
			m.put("last_use", System.currentTimeMillis());

			//Remove this in case it exists. A new one is generated when using the embed page anyway.
			m.remove("embed_key");

			return m;

		} else {
			//Not logged in...
			Map<String, Object> m = new HashMap<>();
			m.put("logged_in", false);
			m.put("bot_id", BotSettings.ID.get());
			m.put("year", LocalDate.now().getYear());
			m.put("redirect_uri", BotSettings.REDIR_URI.get());
			m.put("bot_invite", BotSettings.INVITE_URL.get());
			m.put("support_invite", BotSettings.SUPPORT_INVITE.get());
			m.put("api_url", BotSettings.API_URL.get());

			return m;
		}
	}

	public Map<String, Object> getEmbedAccount(HttpServletRequest request) {
		if ((String) request.getSession(true).getAttribute("account") != null && discordAccounts.containsKey((String) request.getSession(true).getAttribute("account"))) {
			Map<String, Object> m = discordAccounts.get((String) request.getSession(true).getAttribute("account"));
			m.remove("last_use");
			m.put("last_use", System.currentTimeMillis());

			if (!m.containsKey("embed_key")) {
				//Get and add read-only API key for embed page. Only good for one hour.
				try {
					OkHttpClient client = new OkHttpClient();
					RequestBody keyGrantRequestBody = RequestBody.create(GlobalConst.JSON, "");
					Request keyGrantRequest = new Request.Builder()
							.url(BotSettings.API_URL_INTERNAL.get() + "/v2/account/key/readonly/get")
							.header("Authorization", BotSettings.BOT_API_TOKEN.get())
							.post(keyGrantRequestBody)
							.build();
					Response keyGrantResponse = client.newCall(keyGrantRequest).execute();

					//Handle response...
					if (keyGrantResponse.isSuccessful()) {
						JSONObject keyGrantResponseBody = new JSONObject(keyGrantResponse.body().string());
						//API key received, map....
						m.put("embed_key", keyGrantResponseBody.getString("key"));
					} else {
						//Something didn't work... add invalid key that embed page is programmed to respond to.
						Logger.getLogger().debug("Embed Key Fail: " + keyGrantResponse.body().string(), true);
						m.put("embed_key", "internal_error");
					}
				} catch (Exception e) {
					//Something didn't work... add invalid key that embed page is programmed to respond to.
					Logger.getLogger().exception(null, "Embed Key get Failure", e, true, this.getClass());
					m.put("embed_key", "internal_error");
				}
			}

			return m;

		} else {
			//Not logged in...
			Map<String, Object> m = new HashMap<>();
			m.put("logged_in", false);
			m.put("bot_id", BotSettings.ID.get());
			m.put("year", LocalDate.now().getYear());
			m.put("redirect_uri", BotSettings.REDIR_URI.get());
			m.put("bot_invite", BotSettings.INVITE_URL.get());
			m.put("support_invite", BotSettings.SUPPORT_INVITE.get());
			m.put("api_url", BotSettings.API_URL.get());

			//Get and add read-only API key for embed page. Only good for one hour.
			try {
				OkHttpClient client = new OkHttpClient();
				RequestBody keyGrantRequestBody = RequestBody.create(GlobalConst.JSON, "");
				Request keyGrantRequest = new Request.Builder()
						.url(BotSettings.API_URL_INTERNAL.get() + "/v2/account/key/readonly/get")
						.header("Authorization", BotSettings.BOT_API_TOKEN.get())
						.post(keyGrantRequestBody)
						.build();
				Response keyGrantResponse = client.newCall(keyGrantRequest).execute();

				//Handle response...
				if (keyGrantResponse.isSuccessful()) {
					JSONObject keyGrantResponseBody = new JSONObject(keyGrantResponse.body().string());
					//API key received, map....
					m.put("embed_key", keyGrantResponseBody.getString("key"));
				} else {
					//Something didn't work... add invalid key that embed page is programmed to respond to.
					Logger.getLogger().debug("Embed Key Fail: " + keyGrantResponse.body().string(), true);
					m.put("embed_key", "internal_error");
				}
			} catch (Exception e) {
				//Something didn't work... add invalid key that embed page is programmed to respond to.
				Logger.getLogger().exception(null, "Embed Key get Failure", e, true, this.getClass());
				m.put("embed_key", "internal_error");
			}

			return m;
		}
	}


	//Functions
	public void addAccount(Map<String, Object> m, HttpServletRequest request) {
		discordAccounts.remove((String) request.getSession(true).getAttribute("account"));
		m.remove("last_use");
		m.put("last_use", System.currentTimeMillis());
		discordAccounts.put((String) request.getSession(true).getAttribute("account"), m);
	}

	public void removeAccount(HttpServletRequest request) {
		if ((String) request.getSession(true).getAttribute("account") != null && hasAccount(request))
			discordAccounts.remove((String) request.getSession(true).getAttribute("account"));
	}
	private void removeTimedOutAccounts() {
		long limit = Long.parseLong(BotSettings.TIME_OUT.get());
		final List<String> toRemove = new ArrayList<>();
		for (String id : discordAccounts.keySet()) {
			Map<String, Object> m = discordAccounts.get(id);
			long lastUse = (long) m.get("last_use");
			if (System.currentTimeMillis() - lastUse > limit)
				toRemove.remove(id); //Timed out, remove account info and require sign in.
		}

		for (String id : toRemove) {
			discordAccounts.remove(id);
		}
	}
}
