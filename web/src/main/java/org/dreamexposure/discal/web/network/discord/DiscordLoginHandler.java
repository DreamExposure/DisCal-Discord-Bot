package org.dreamexposure.discal.web.network.discord;

import org.dreamexposure.discal.core.enums.GoodTimezone;
import org.dreamexposure.discal.core.enums.announcement.AnnouncementType;
import org.dreamexposure.discal.core.enums.event.EventColor;
import org.dreamexposure.discal.core.logger.Logger;
import org.dreamexposure.discal.core.object.BotSettings;
import org.dreamexposure.discal.core.object.web.WebPartialGuild;
import org.dreamexposure.discal.core.utils.GlobalConst;
import org.dreamexposure.discal.web.handler.DiscordAccountHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@SuppressWarnings("ConstantConditions")
@RestController
public class DiscordLoginHandler {

	@GetMapping(value = "/account/login")
	public String handleDiscordCode(HttpServletRequest req, HttpServletResponse res, @RequestParam(value = "code") String code) throws IOException {
		OkHttpClient client = new OkHttpClient();

		try {
			//Handle getting discord account data....
			RequestBody body = new FormBody.Builder()
					.addEncoded("client_id", BotSettings.ID.get())
					.addEncoded("client_secret", BotSettings.SECRET.get())
					.addEncoded("grant_type", "authorization_code")
					.addEncoded("code", code)
					.addEncoded("redirect_uri", BotSettings.REDIR_URI.get())
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

				//We have the data we need, now map it, and request an API token for this session.

				//Saving session info and access info to memory...
				Map<String, Object> m = new HashMap<>();

				//The universal stuffs
				m.put("logged_in", true);
				m.put("client", BotSettings.ID.get());
				m.put("year", LocalDate.now().getYear());
				m.put("redirect_uri", BotSettings.REDIR_URI.get());
				m.put("bot_invite", BotSettings.INVITE_URL.get());
				m.put("support_invite", BotSettings.SUPPORT_INVITE.get());
				m.put("api_url", BotSettings.API_URL.get());

				//More universal stuff, but for the dashboard only
				m.put("good_timezones", GoodTimezone.values());
				m.put("announcement_types", AnnouncementType.values());
				m.put("event_colors", EventColor.values());

				//User info
				m.put("id", userInfo.getString("id"));
				m.put("username", userInfo.getString("username"));
				m.put("discrim", userInfo.getString("discriminator"));
				if (userInfo.has("avatar") && !userInfo.isNull("avatar")) {
					m.put("pfp", "https://cdn.discordapp.com/avatars/"
							+ userInfo.getString("id")
							+ "/"
							+ userInfo.getString("avatar")
							+ ".png");
				} else {
					m.put("pfp", "/assets/img/default/pfp.png");
				}

				//Guild stuffs
				List<WebPartialGuild> guilds = new ArrayList<>();
				for (int i = 0; i < jGuilds.length(); i++) {
					JSONObject jGuild = jGuilds.getJSONObject(i);

					long id = jGuild.getLong("id");
					String name = jGuild.getString("name");
					String icon;
					if (jGuild.has("icon") && !jGuild.isNull("icon")) {
						icon = "https://cdn.discordapp.com/icons/"
								+ id
								+ "/"
								+ jGuild.getString("icon")
								+ ".png";
					} else {
						icon = "/assets/img/default/guild-icon.png";
					}

					guilds.add(new WebPartialGuild(id, name, icon));
				}
				m.put("guilds", guilds);

				String newSessionId = UUID.randomUUID().toString();

				req.getSession(true).setAttribute("account", newSessionId);

				//Request temporary API key....
				RequestBody keyGrantRequestBody = RequestBody.create(GlobalConst.JSON, "");

				Request keyGrantRequest = new Request.Builder()
						.url(BotSettings.API_URL_INTERNAL.get() + "/v2/account/login")
						.header("Authorization", BotSettings.BOT_API_TOKEN.get())
						.post(keyGrantRequestBody)
						.build();

				Response keyGrantResponse = client.newCall(keyGrantRequest).execute();
				//Handle response...
				if (keyGrantResponse.isSuccessful()) {
					JSONObject keyGrantResponseBody = new JSONObject(keyGrantResponse.body().string());
					//API key received, map....
					m.put("key", keyGrantResponseBody.getString("key"));
					DiscordAccountHandler.getHandler().addAccount(m, req);
				} else {
					//Something didn't work... just redirect back to the login page....
					Logger.getLogger().debug("login issue: " + keyGrantResponse.body().string(), true);
					res.sendRedirect("/login");
					return "redirect:/login";
				}

				//Finally redirect to the dashboard seamlessly.
				res.sendRedirect("/dashboard");
				return "redirect:/dashboard";
			} else {
				//Token not provided. Authentication denied or errored... Redirect to login page so user knows auth failed.
				res.sendRedirect("/login");
				return "redirect:/login";
			}
		} catch (JSONException e) {
			Logger.getLogger().exception("[LOGIN-Discord] JSON || Discord login failed!", e, true, this.getClass());
			res.sendRedirect("/dashboard");
			return "redirect:/dashboard";
		} catch (Exception e) {
			Logger.getLogger().exception("[LOGIN-Discord] Discord login failed!", e, true, this.getClass());
			res.sendRedirect("/dashboard");
			return "redirect:/dashboard";
		}
	}

	@GetMapping("/logout")
	public String handleLogout(HttpServletRequest request, HttpServletResponse res) throws IOException {
		try {
			//Tell the API server the user has logged out and to delete the temporary key.
			OkHttpClient client = new OkHttpClient();

			Map<String, Object> map = DiscordAccountHandler.getHandler().getAccount(request);

			RequestBody logoutRequestBody = RequestBody.create(GlobalConst.JSON, "");

			Request logoutRequest = new Request.Builder()
					.url(BotSettings.API_URL_INTERNAL.get() + "/v2/account/logout")
					.header("Authorization", (String) map.get("key"))
					.post(logoutRequestBody)
					.build();

			//We don't need a return, if it fails, it will be auto-deleted anyway...
			client.newCall(logoutRequest).execute();

			DiscordAccountHandler.getHandler().removeAccount(request);
			request.getSession().invalidate();

			res.sendRedirect("/");
			return "redirect:/";
		} catch (Exception e) {
			Logger.getLogger().exception("[WEB] Discord logout failed!", e, true, DiscordLoginHandler.class);
			res.sendRedirect("/");
			return "redirect:/";
		}
	}
}