package org.dreamexposure.discal.server.network.dbotsgg;

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

public class UpdateDBotsData {
    private static Timer timer;

    public static void init() {
        if ("true".equalsIgnoreCase(BotSettings.UPDATE_SITES.get())) {
            timer = new Timer(true);
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    updateSiteData();
                }
            }, GlobalConst.oneHourMs);
        }
    }

    public static void shutdown() {
        if (timer != null) {
            timer.cancel();
        }
    }

    private static void updateSiteData() {
        try {
            final JSONObject json = new JSONObject()
                .put("guildCount", DisCalServer.getNetworkInfo().getTotalGuildCount())
                .put("shardCount", DisCalServer.getNetworkInfo().getExpectedClientCount());

            OkHttpClient client = new OkHttpClient();

            RequestBody body = RequestBody.create(GlobalConst.JSON, json.toString());
            Request request = new Request.Builder()
                .url("https://discord.bots.gg/api/v1/bots/265523588918935552/stats")
                .post(body)
                .header("Authorization", BotSettings.D_BOTS_GG_TOKEN.get())
                .header("Content-Type", "application/json")
                .build();

            Response response = client.newCall(request).execute();

            if (response.code() != GlobalConst.STATUS_SUCCESS)
                LogFeed.log(LogObject.forDebug("Failed to update DBots.gg stats", "Body: " +
                    (response.body() != null ? response.body().string() : "null")));
        } catch (Exception e) {
            LogFeed.log(LogObject.forException("Failed to update DBots.gg stats", e, UpdateDBotsData.class));
        }
    }
}
