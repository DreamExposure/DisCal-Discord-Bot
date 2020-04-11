package org.dreamexposure.discal.server.network.discordpw;

import org.dreamexposure.discal.core.logger.LogFeed;
import org.dreamexposure.discal.core.logger.object.LogObject;
import org.dreamexposure.discal.core.object.BotSettings;
import org.dreamexposure.discal.core.utils.GlobalConst;
import org.dreamexposure.discal.server.DisCalServer;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Nova Fox on 1/13/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class UpdateDisPwData {
	private static Timer timer;

	public static void init() {
		if (BotSettings.UPDATE_SITES.get().equalsIgnoreCase("true")) {
			timer = new Timer(true);
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					updateSiteBotMeta();
				}
			}, 60 * 60 * 1000);
		}
	}

	public static void shutdown() {
		if (timer != null)
			timer.cancel();
	}

	private static void updateSiteBotMeta() {
		try {

			JSONObject json = new JSONObject().put("server_count", DisCalServer.getNetworkInfo().getTotalGuildCount());

			OkHttpClient client = new OkHttpClient();

			RequestBody body = RequestBody.create(GlobalConst.JSON, json.toString());
			Request request = new Request.Builder()
					.url("https://bots.discord.pw/api/bots/265523588918935552/stats")
					.post(body)
					.header("Authorization", BotSettings.PW_TOKEN.get())
					.header("Content-Type", "application/json")
					.build();

			Response response = client.newCall(request).execute();

			if (response.code() == 200)
				LogFeed.log(LogObject.forDebug("Successfully updated Discord PW list"));
		} catch (Exception e) {
			//Handle issue.
			System.out.println("Failed to update Discord PW list metadata!");
			LogFeed.log(LogObject
					.forException("Failed to update Discord PW list", e, UpdateDisPwData.class));
			e.printStackTrace();
		}
	}
}