package com.cloudcraftgaming.discal.web.oauth;

import com.cloudcraftgaming.discal.Main;
import com.cloudcraftgaming.discal.api.object.BotSettings;
import com.cloudcraftgaming.discal.api.utils.GuildUtils;
import com.cloudcraftgaming.discal.web.handler.DiscordAccountHandler;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import org.json.JSONException;
import org.json.JSONObject;
import spark.Request;
import spark.Response;

import java.util.HashMap;
import java.util.Map;

import static spark.Spark.halt;

/**
 * Created by Nova Fox on 12/17/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
@SuppressWarnings({"ThrowableNotThrown", "unchecked"})
public class DiscordLoginHandler {
	public static String handleDiscordCode(Request request, Response response) {
		try {
			String code = request.queryParams("code");

			//POST request to discord for access...
			HttpResponse<JsonNode> httpResponse = Unirest.post("https://discordapp.com/api/v6/oauth2/token").header("Content-Type", "application/x-www-form-urlencoded").field("client_id", Main.client.getApplicationClientID()).field("client_secret", BotSettings.SECRET.get()).field("grant_type", "authorization_code").field("code", code).field("redirect_uri", "http://localhost:4567/account/login").asJson();

			JSONObject info = new JSONObject(httpResponse.getBody()).getJSONObject("object");

			//GET request for user info...
			HttpResponse<JsonNode> userDataResponse = Unirest.get("https://discordapp.com/api/v6/users/@me").header("Authorization", "Bearer " + info.getString("access_token")).asJson();

			JSONObject userInfo = new JSONObject(userDataResponse.getBody()).getJSONObject("object");

			//Saving session info and access info to memory until moved into the database...
			Map m = new HashMap();
			m.put("loggedIn", true);

			m.put("id", userInfo.getString("id"));
			m.put("username", userInfo.getString("username"));
			m.put("discrim", userInfo.getString("discriminator"));

			//Get guilds...
			m.put("guilds", GuildUtils.getGuilds(userInfo.getString("id")));

			DiscordAccountHandler.getHandler().addAccount(m, request.session().id());

			//Finally redirect to the dashboard seamlessly.
			response.redirect("/dashboard");
		} catch (JSONException e) {
			e.printStackTrace();
			response.redirect("/dashboard");
		} catch (Exception e) {
			e.printStackTrace();
			halt(500, "Internal Server Exception");
		}
		return response.body();
	}

	public static String handleLogout(Request request, Response response) {
		try {
			DiscordAccountHandler.getHandler().removeAccount(request.session().id());

			response.redirect("/");
		} catch (Exception e) {
			e.printStackTrace();
			halt(500, "Internal Server Exception");
		}
		return response.body();
	}
}