package com.cloudcraftgaming.discal.internal.network.discordpw;

import com.cloudcraftgaming.discal.Main;
import com.cloudcraftgaming.discal.internal.email.EmailSender;
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
            try {
                Integer serverCount = Main.client.getGuilds().size();

                JSONObject json = new JSONObject().put("server_count", serverCount);

                HttpResponse<JsonNode> response = Unirest.post("http://bots.discord.pw/api/bots/265523588918935552/stats").header("Authorization", token).header("Content-Type", "application/json").body(json).asJson();

            } catch (Exception e) {
                //Handle issue.
                System.out.println("Failed to update Discord PW list metadata!");
                EmailSender.getSender().sendExceptionEmail(e, UpdateListData.class);
                e.printStackTrace();
            }
        }
    }
}