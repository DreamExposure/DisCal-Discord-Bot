package org.dreamexposure.discal.client.service;

import org.dreamexposure.discal.client.DisCalClient;
import org.dreamexposure.discal.core.logger.Logger;
import org.dreamexposure.novautils.network.pubsub.PubSubManager;
import org.joda.time.Interval;
import org.joda.time.Period;
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
@SuppressWarnings("Duplicates")
public class KeepAliveHandler {
	public static void startKeepAlive(final int seconds) {
		new Timer(true).scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				JSONObject data = new JSONObject();
				data.put("Reason", "Keep-Alive");
				data.put("Server-Count", DisCalClient.getClient().getGuilds().count().block());
				data.put("Mem-Used", usedMemory());
				data.put("Uptime", humanReadableUptime());
				//TODO: Add announcement count!!!

				PubSubManager.get().publish("DisCal/ToServer/KeepAlive", DisCalClient.clientId(), data);

				Logger.getLogger().debug("Sent keep alive to server.", false);
			}
		}, seconds * 1000, seconds * 1000);
	}

	private static double usedMemory() {
		long totalMemory = Runtime.getRuntime().totalMemory();
		long freeMemory = Runtime.getRuntime().freeMemory();
		double a = (totalMemory - freeMemory) / (double) (1024 * 1024);
		return (double) Math.round(a * 100) / 100;
	}

	private static String humanReadableUptime() {
		RuntimeMXBean mxBean = ManagementFactory.getRuntimeMXBean();
		Interval interval = new Interval(mxBean.getStartTime(), System.currentTimeMillis());
		Period period = interval.toPeriod();

		return String.format("%d months, %d days, %d hours, %d minutes, %d seconds%n", period.getMonths(), period.getDays(), period.getHours(), period.getMinutes(), period.getSeconds());
	}
}