package org.dreamexposure.discal.web.network.discal;

import com.google.api.client.http.HttpStatusCodes;

import org.dreamexposure.discal.core.logger.LogFeed;
import org.dreamexposure.discal.core.logger.object.LogObject;
import org.dreamexposure.discal.core.object.BotSettings;
import org.dreamexposure.discal.core.object.network.discal.NetworkInfo;
import org.dreamexposure.discal.core.utils.GlobalConst;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class StatusHandler {
    public static NetworkInfo getLatestStatusInfo() {
        try {
            final Call.Factory client = new OkHttpClient();

            final RequestBody body = RequestBody.create(GlobalConst.JSON, "");
            final Request request = new Request.Builder()
                .url(BotSettings.API_URL_INTERNAL.get() + "/v2/status/get")
                .post(body)
                .header("Authorization", BotSettings.BOT_API_TOKEN.get())
                .header("Content-Type", "application/json")
                .build();

            final Response response = client.newCall(request).execute();

            if (response.code() == HttpStatusCodes.STATUS_CODE_OK) {
                return new NetworkInfo().fromJson(new JSONObject(response.body().string()));
            }
            return new NetworkInfo(); //Just return an empty object, its fine...
        } catch (final Exception e) {
            LogFeed.log(LogObject
                .forException("[STATUS REQUEST] Failed to get status", e, StatusHandler.class));

            return new NetworkInfo(); //Just return an empty object, its fine.
        }
    }
}
