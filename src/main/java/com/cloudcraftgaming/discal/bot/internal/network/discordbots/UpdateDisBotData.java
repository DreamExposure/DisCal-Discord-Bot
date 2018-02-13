package com.cloudcraftgaming.discal.bot.internal.network.discordbots;

import com.cloudcraftgaming.discal.Main;
import com.cloudcraftgaming.discal.api.object.BotSettings;
import org.discordbots.api.client.DiscordBotListAPI;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Nova Fox on 2/13/18.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
public class UpdateDisBotData {
	private static DiscordBotListAPI api;
	private static Timer timer;

	public static void init() {
		api = new DiscordBotListAPI.Builder().token(BotSettings.DBO_TOKEN.get()).build();

		timer = new Timer(true);
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				updateStats();
			}
		}, 60 * 60 * 1000);
	}

	public static void shutdown() {
		timer.cancel();
	}

	private static void updateStats() {
		api.setStats(BotSettings.ID.get(), Main.client.getGuilds().size());
	}
}