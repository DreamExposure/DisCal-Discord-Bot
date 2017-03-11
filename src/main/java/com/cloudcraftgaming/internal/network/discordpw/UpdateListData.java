package com.cloudcraftgaming.internal.network.discordpw;

import com.cloudcraftgaming.Main;
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

    public static void init(String _token) {
        token = _token;
    }

    public static void updateSiteBotMeta() {
        if (!token.equalsIgnoreCase("N/a")) {
            HttpClient httpClient = HttpClientBuilder.create().build();

            try {
                HttpPost request = new HttpPost("https://bots.discord.pw/api/bots/:265523588918935552/stats");

                String serverCount = String.valueOf(Main.client.getGuilds().size());
                StringEntity params = new StringEntity("metadata={\"server_count\": " + serverCount + " //} ");
                request.setHeader("Authorization", token);
                request.setEntity(params);
                httpClient.execute(request);
            } catch (Exception e) {
                //Handle issue.
                System.out.println("Failed to update Discord PW list metadata!");
                e.printStackTrace();
            }
        }
    }
}