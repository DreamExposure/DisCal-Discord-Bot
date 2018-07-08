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
import spark.Request;
import spark.Response;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static spark.Spark.halt;

/**
 * Created by Nova Fox on 12/19/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
@SuppressWarnings({"ThrowableNotThrown", "unchecked"})
public class DiscordLoginHandler {

	public static String handleDiscordCode(Request request, Response response) {
		OkHttpClient client = new OkHttpClient();

		try {
			String code = request.queryParams("code");

			RequestBody body = new FormBody.Builder()
					.add("client_id", BotSettings.ID.get())
					.add("client_secret", BotSettings.SECRET.get())
					.add("grant_type", "authorization_code")
					.add("code", code)
					.add("redirect_uri", BotSettings.REDIR_URL.get())
					.build();

			okhttp3.Request httpRequest = new okhttp3.Request.Builder()
					.url("https://discordapp.com/api/v6/oauth2/token")
					.post(body)
					.header("Content-Type", "application/x-www-form-urlencoded")
					.build();

			//POST request to discord for access...
			/*
			HttpResponse<JsonNode> httpResponse = Unirest.post("https://discordapp.com/api/v6/oauth2/token").header("Content-Type", "application/x-www-form-urlencoded").field("client_id", BotSettings.ID.get()).field("client_secret", BotSettings.SECRET.get()).field("grant_type", "authorization_code").field("code", code).field("redirect_uri", BotSettings.REDIR_URL.get()).asJson();

			JSONObject info = new JSONObject(httpResponse.getBody()).getJSONObject("object");
			*/

			okhttp3.Response httpResponse = client.newCall(httpRequest).execute();

			@SuppressWarnings("ConstantConditions")
			JSONObject info = new JSONObject(httpResponse.body()).getJSONObject("object");

			if (info.has("access_token")) {
				//GET request for user info...
				okhttp3.Request userDataRequest = new okhttp3.Request.Builder()
						.url("https://discordapp.com/api/v6/users/@me")
						.header("Authorization", "Bearer " + info.getString("access_token"))
						.build();

				okhttp3.Response userDataResponse = client.newCall(userDataRequest).execute();

				/*
				HttpResponse<JsonNode> userDataResponse = Unirest.get("https://discordapp.com/api/v6/users/@me").header("Authorization", "Bearer " + info.getString("access_token")).asJson();

				JSONObject userInfo = new JSONObject(userDataResponse.getBody()).getJSONObject("object");

				 */

				@SuppressWarnings("ConstantConditions")
				JSONObject userInfo = new JSONObject(userDataResponse.body()).getJSONObject("object");

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

				DiscordAccountHandler.getHandler().addAccount(m, request.session().id());

				//Finally redirect to the dashboard seamlessly.
				response.redirect("/dashboard", 301);
			} else {
				//Token not provided. Authentication denied or errored... Redirect to dashboard so user knows auth failed.
				response.redirect("/dashboard", 301);
			}
		} catch (JSONException e) {
			Logger.getLogger().exception(null, "[WEB] JSON || Discord login failed!", e, DiscordLoginHandler.class, true);
			response.redirect("/dashboard", 301);
		} catch (Exception e) {
			Logger.getLogger().exception(null, "[WEB] Discord login failed!", e, DiscordLoginHandler.class, true);
			halt(500, "Internal Server Exception");
		}
		return response.body();
	}

	public static String handleLogout(Request request, Response response) {
		try {
			DiscordAccountHandler.getHandler().removeAccount(request.session().id());

			response.redirect("/", 301);
		} catch (Exception e) {
			Logger.getLogger().exception(null, "[WEB] Discord logout failed!", e, DiscordLoginHandler.class, true);
			halt(500, "Internal Server Exception");
		}
		return response.body();
	}
}