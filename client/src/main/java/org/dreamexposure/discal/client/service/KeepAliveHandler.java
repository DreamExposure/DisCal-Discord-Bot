package org.dreamexposure.discal.client.service;

import org.dreamexposure.discal.client.DisCalClient;
import org.dreamexposure.discal.core.object.BotSettings;
import org.dreamexposure.novautils.network.crosstalk.ClientSocketHandler;
import org.json.JSONObject;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author NovaFox161
 * Date Created: 9/8/2018
 * For Project: DisCal-Discord-Bot
 * Author Website: https://www.novamaday.com
 * Company Website: https://www.dreamexposure.org
 * Contact: nova@dreamexposure.org
 */
@SuppressWarnings("ConstantConditions")
public class KeepAliveHandler {
	public static void startKeepAlive(final int seconds) {
		new Timer(true).scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				JSONObject data = new JSONObject();
				data.put("Reason", "Keep-Alive");
				data.put("Server-Count", DisCalClient.getClient().getGuilds().size());
				data.put("Mem-Used", usedMemory());
				data.put("Uptime", getUptime());
				//TODO: Add announcement count!!!

				ClientSocketHandler.sendToServer(Integer.valueOf(BotSettings.SHARD_INDEX.get()), data);
			}
		}, seconds * 1000, seconds * 1000);
	}

	private static double usedMemory() {
		long totalMemory = Runtime.getRuntime().totalMemory();
		long freeMemory = Runtime.getRuntime().freeMemory();
		return (double) (totalMemory - freeMemory) / (double) (1024 * 1024);
	}

	private static long getUptime() {
		RuntimeMXBean mxBean = ManagementFactory.getRuntimeMXBean();
		return mxBean.getUptime();
	}
}