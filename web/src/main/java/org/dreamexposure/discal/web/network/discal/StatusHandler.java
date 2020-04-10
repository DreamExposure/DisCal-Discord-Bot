package org.dreamexposure.discal.web.network.discal;

import org.dreamexposure.discal.core.logger.Logger;
import org.dreamexposure.discal.core.object.BotSettings;
import org.dreamexposure.discal.core.object.network.discal.NetworkInfo;
import org.dreamexposure.discal.core.utils.GlobalConst;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class StatusHandler {
	public static NetworkInfo getLatestStatusInfo() {
		try {
			OkHttpClient client = new OkHttpClient();

			RequestBody body = RequestBody.create(GlobalConst.JSON, "");
			Request request = new Request.Builder()
					.url(BotSettings.API_URL_INTERNAL.get() + "/v2/status/get")
					.post(body)
					.header("Authorization", BotSettings.BOT_API_TOKEN.get())
					.header("Content-Type", "application/json")
					.build();

			Response response = client.newCall(request).execute();

			if (response.code() == 200) {
				return new NetworkInfo().fromJson(new JSONObject(response.body().string()));
			}
			return new NetworkInfo(); //Just return an empty object, its fine...
		} catch (Exception e) {
			Logger.getLogger().exception("[STATUS REQUEST] Failed to get status", e, true, StatusHandler.class);

			return new NetworkInfo(); //Just return an empty object, its fine.
		}
	}
}
