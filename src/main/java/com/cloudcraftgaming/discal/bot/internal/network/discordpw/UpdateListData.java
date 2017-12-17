package com.cloudcraftgaming.discal.bot.internal.network.discordpw;

import com.cloudcraftgaming.discal.Main;
import com.cloudcraftgaming.discal.api.object.BotSettings;
import com.cloudcraftgaming.discal.api.utils.ExceptionHandler;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import org.json.JSONObject;

/**
 * Created by Nova Fox on 1/13/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class UpdateListData {

	/**
	 * Initiates the data updater with a valid token.
	 *
	 */
	public static void init() {
	}

	/**
	 * Updates the site meta on bots.discord.pw
	 */
	public static void updateSiteBotMeta() {
		try {
			Integer serverCount = Main.client.getGuilds().size();

			JSONObject json = new JSONObject().put("server_count", serverCount);

			HttpResponse<JsonNode> response = Unirest.post("https://bots.discord.pw/api/bots/265523588918935552/stats").header("Authorization", BotSettings.PW_TOKEN.get()).header("Content-Type", "application/json").body(json).asJson();
		} catch (Exception e) {
			//Handle issue.
			System.out.println("Failed to update Discord PW list metadata!");
			ExceptionHandler.sendException(null, "Failed to update Discord PW list.", e, UpdateListData.class);
			e.printStackTrace();
		}
	}
}