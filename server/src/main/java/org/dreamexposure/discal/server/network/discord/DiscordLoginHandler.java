package org.dreamexposure.discal.server.network.discord;

import okhttp3.*;
import org.dreamexposure.discal.core.enums.GoodTimezone;
import org.dreamexposure.discal.core.enums.announcement.AnnouncementType;
import org.dreamexposure.discal.core.enums.event.EventColor;
import org.dreamexposure.discal.core.enums.network.CrossTalkReason;
import org.dreamexposure.discal.core.enums.network.DisCalRealm;
import org.dreamexposure.discal.core.logger.Logger;
import org.dreamexposure.discal.core.object.BotSettings;
import org.dreamexposure.discal.core.object.network.discal.ConnectedClient;
import org.dreamexposure.discal.core.object.web.WebGuild;
import org.dreamexposure.discal.server.DisCalServer;
import org.dreamexposure.discal.server.handler.DiscordAccountHandler;
import org.dreamexposure.novautils.network.crosstalk.ServerSocketHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

/**
 * Created by Nova Fox on 12/19/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
@SuppressWarnings({"unchecked", "unused", "ConstantConditions"})
@RestController
public class DiscordLoginHandler {

	@GetMapping("/account/login")
	public static String handleDiscordCode(HttpServletRequest req, HttpServletResponse res, @RequestParam(value = "code") String code) throws IOException {
		OkHttpClient client = new OkHttpClient();

		try {
			RequestBody body = new FormBody.Builder()
					.addEncoded("client_id", BotSettings.ID.get())
					.addEncoded("client_secret", BotSettings.SECRET.get())
					.addEncoded("grant_type", "authorization_code")
					.addEncoded("code", code)
					.addEncoded("redirect_uri", BotSettings.REDIR_URL.get())
					.build();

			okhttp3.Request httpRequest = new okhttp3.Request.Builder()
					.url("https://discordapp.com/api/v6/oauth2/token")
					.post(body)
					.header("Content-Type", "application/x-www-form-urlencoded")
					.build();

			//POST request to discord for access...
			okhttp3.Response httpResponse = client.newCall(httpRequest).execute();

			@SuppressWarnings("ConstantConditions")
			JSONObject info = new JSONObject(httpResponse.body().string());

			if (info.has("access_token")) {
				//GET request for user info...
				Request userDataRequest = new Request.Builder()
						.url("https://discordapp.com/api/v6/users/@me")
						.header("Authorization", "Bearer " + info.getString("access_token"))
						.build();

				Response userDataResponse = client.newCall(userDataRequest).execute();

				Request userGuildsRequest = new Request.Builder()
					.url("https://discordapp.com/api/v6/users/@me/guilds")
					.header("Authorization", "Bearer " + info.getString("access_token"))
					.build();

				Response userGuildsResponse = client.newCall(userGuildsRequest).execute();

				JSONObject userInfo = new JSONObject(userDataResponse.body().string());
				JSONArray jGuilds = new JSONArray(userGuildsResponse.body().string());

				//Get list of guild IDs.
				JSONArray servers = new JSONArray();
				for (int i = 0; i < jGuilds.length(); i++) {
					servers.put(jGuilds.getJSONObject(i).getLong("id"));
				}

				//Saving session info and access info to memory until moved into the database...
				Map m = new HashMap();
				m.put("loggedIn", true);
				m.put("client", BotSettings.ID.get());
				m.put("year", LocalDate.now().getYear());
				m.put("redirUri", BotSettings.REDIR_URI.get());

				m.put("id", userInfo.getString("id"));
				m.put("username", userInfo.getString("username"));
				m.put("discrim", userInfo.getString("discriminator"));

				//Get guilds...
				List<WebGuild> guilds = new ArrayList<>();
				for (ConnectedClient csd : DisCalServer.getNetworkInfo().getClients()) {
					JSONObject requestData = new JSONObject();

					requestData.put("Reason", CrossTalkReason.GET.name());
					requestData.put("Realm", DisCalRealm.WEBSITE_DASHBOARD_DEFAULTS);
					requestData.put("Member-Id", m.get("id") + "");
					requestData.put("Guilds", servers);

					JSONObject responseData = ServerSocketHandler.sendAndReceive(requestData, csd.getClientHostname(), csd.getClientPort());

					JSONArray guildsData = responseData.getJSONArray("Guilds");
					for (int i = 0; i < guildsData.length(); i++) {
						guilds.add(new WebGuild().fromJson(guildsData.getJSONObject(i)));
					}
				}

				m.put("guilds", guilds);

				m.put("goodTz", GoodTimezone.values());
				m.put("anTypes", AnnouncementType.values());
				m.put("eventColors", EventColor.values());

				String newSessionId = UUID.randomUUID().toString();

				req.getSession(true).setAttribute("account", newSessionId);

				DiscordAccountHandler.getHandler().addAccount(m, req);

				//Finally redirect to the dashboard seamlessly.
				res.sendRedirect("/dashboard");
				return "redirect:/dashboard";
			} else {
				//Token not provided. Authentication denied or errored... Redirect to dashboard so user knows auth failed.
				res.sendRedirect("/dashboard");
				return "redirect:/dashboard";
			}
		} catch (JSONException e) {
			Logger.getLogger().exception(null, "[WEB] JSON || Discord login failed!", e, DiscordLoginHandler.class);
			res.sendRedirect("/dashboard");
			return "redirect:/dashboard";
		} catch (Exception e) {
			Logger.getLogger().exception(null, "[WEB] Discord login failed!", e, DiscordLoginHandler.class);
			res.sendRedirect("/dashboard");
			return "redirect:/dashboard";
		}
	}

	@GetMapping("/account/logout")
	public static String handleLogout(HttpServletRequest request, HttpServletResponse res) throws IOException {
		try {
			DiscordAccountHandler.getHandler().removeAccount(request);
			request.getSession().invalidate();

			res.sendRedirect("/");
			return "redirect:/";
		} catch (Exception e) {
			Logger.getLogger().exception(null, "[WEB] Discord logout failed!", e, DiscordLoginHandler.class);
			res.sendRedirect("/");
			return "redirect:/";
		}
	}
}