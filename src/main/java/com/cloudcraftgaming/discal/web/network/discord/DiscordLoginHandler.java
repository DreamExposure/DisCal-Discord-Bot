package com.cloudcraftgaming.discal.web.network.discord;

import com.cloudcraftgaming.discal.api.enums.GoodTimezone;
import com.cloudcraftgaming.discal.api.enums.announcement.AnnouncementType;
import com.cloudcraftgaming.discal.api.enums.event.EventColor;
import com.cloudcraftgaming.discal.api.object.BotSettings;
import com.cloudcraftgaming.discal.api.utils.GuildUtils;
import com.cloudcraftgaming.discal.logger.Logger;
import com.cloudcraftgaming.discal.web.handler.DiscordAccountHandler;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Nova Fox on 12/19/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
@SuppressWarnings({"ThrowableNotThrown", "unchecked", "unused"})
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
				okhttp3.Request userDataRequest = new okhttp3.Request.Builder()
						.url("https://discordapp.com/api/v6/users/@me")
						.header("Authorization", "Bearer " + info.getString("access_token"))
						.build();

				okhttp3.Response userDataResponse = client.newCall(userDataRequest).execute();

				@SuppressWarnings("ConstantConditions")
				JSONObject userInfo = new JSONObject(userDataResponse.body().string());

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
				m.put("guilds", GuildUtils.getGuilds(userInfo.getString("id")));

				m.put("goodTz", GoodTimezone.values());
				m.put("anTypes", AnnouncementType.values());
				m.put("eventColors", EventColor.values());

				String newSessionId = UUID.randomUUID().toString();

				req.getSession(true).setAttribute("account", newSessionId);

				DiscordAccountHandler.getHandler().addAccount(m, newSessionId);

				//Finally redirect to the dashboard seamlessly.
				res.sendRedirect("/dashboard");
				return "redirect:/dashboard";
			} else {
				//Token not provided. Authentication denied or errored... Redirect to dashboard so user knows auth failed.
				res.sendRedirect("/dashboard");
				return "redirect:/dashboard";
			}
		} catch (JSONException e) {
			Logger.getLogger().exception(null, "[WEB] JSON || Discord login failed!", e, DiscordLoginHandler.class, true);
			res.sendRedirect("/dashboard");
			return "redirect:/dashboard";
		} catch (Exception e) {
			Logger.getLogger().exception(null, "[WEB] Discord login failed!", e, DiscordLoginHandler.class, true);
			res.sendRedirect("/dashboard");
			return "redirect:/dashboard";
		}
	}

	@GetMapping("/account/logout")
	public static String handleLogout(HttpServletRequest request, HttpServletResponse res) throws IOException {
		try {
			DiscordAccountHandler.getHandler().removeAccount((String) request.getSession(true).getAttribute("account"));

			res.sendRedirect("/");
			return "redirect:/";
		} catch (Exception e) {
			Logger.getLogger().exception(null, "[WEB] Discord logout failed!", e, DiscordLoginHandler.class, true);
			res.sendRedirect("/");
			return "redirect:/";
		}
	}
}