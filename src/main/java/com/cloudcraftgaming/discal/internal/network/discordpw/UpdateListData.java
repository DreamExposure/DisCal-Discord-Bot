package com.cloudcraftgaming.discal.internal.network.discordpw;

import com.cloudcraftgaming.discal.Main;
import com.cloudcraftgaming.discal.internal.network.discordpw.json.Stats;
import com.google.gson.Gson;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

/**
 * Created by Nova Fox on 1/13/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class UpdateListData {
    private static String token = "N/a";

    /**
     * Initiates the data updater with a valid token.
     * @param _token Private API token.
     */
    public static void init(String _token) {
        token = _token;
    }

    /**
     * Updates the site meta on bots.discord.pw
     */
    public static void updateSiteBotMeta() {
        if (!token.equalsIgnoreCase("N/a")) {
            HttpClient httpClient = HttpClientBuilder.create().build();

            try {
                HttpPost request = new HttpPost("http://bots.discord.pw/api/bots/265523588918935552/stats");

                String serverCount = String.valueOf(Main.client.getGuilds().size());

                Stats stats = new Stats();
                stats.server_count = serverCount;

                request.setHeader("Authorization", token);
                String json = new Gson().toJson(stats);
                request.setEntity(new StringEntity(json));
                httpClient.execute(request);
            } catch (Exception e) {
                //Handle issue.
                System.out.println("Failed to update Discord PW list metadata!");
                e.printStackTrace();
            }
        }
    }
}