package org.dreamexposure.discal.server.network.topgg;

import org.discordbots.api.client.DiscordBotListAPI;
import org.dreamexposure.discal.core.object.BotSettings;
import org.dreamexposure.discal.core.object.network.discal.ConnectedClient;
import org.dreamexposure.discal.core.utils.GlobalConst;
import org.dreamexposure.discal.server.DisCalServer;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

public class UpdateTopStats {
    private static DiscordBotListAPI api;
    private static Timer timer;

    public static void init() {
        if ("true".equalsIgnoreCase(BotSettings.UPDATE_SITES.get())) {

            api = new DiscordBotListAPI.Builder().token(BotSettings.TOP_GG_TOKEN.get()).build();

            timer = new Timer(true);
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    updateStats();
                }
            }, GlobalConst.oneHourMs);
        }
    }

    public static void shutdown() {
        if (timer != null)
            timer.cancel();
    }

    private static void updateStats() {
        if (api != null) {
            List<Integer> guildsOnShard = DisCalServer.getNetworkInfo().getClients()
                .stream()
                .map(ConnectedClient::getConnectedServers)
                .collect(Collectors.toList());

            api.setStats(guildsOnShard);
        }
    }
}
