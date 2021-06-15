package org.dreamexposure.discal.client.service;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.dreamexposure.discal.client.DisCalClient;
import org.dreamexposure.discal.core.logger.LogFeed;
import org.dreamexposure.discal.core.logger.object.LogObject;
import org.dreamexposure.discal.core.object.BotSettings;
import org.dreamexposure.discal.core.utils.GlobalConst;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.json.JSONObject;
import org.springframework.boot.system.ApplicationPid;

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
    @SuppressWarnings("MagicNumber")
    public static void startKeepAlive(final int seconds) {
        new Timer(true).scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    final JSONObject data = new JSONObject();
                    data.put("index", Integer.parseInt(BotSettings.SHARD_INDEX.get()));

                    if (DisCalClient.getClient() != null)
                        data.put("guilds", DisCalClient.getClient().getGuilds().count().block());
                    else
                        data.put("guilds", 0);
                    data.put("memory", usedMemory());
                    data.put("uptime", humanReadableUptime());
                    data.put("version", GlobalConst.version);
                    data.put("d4j_version", GlobalConst.d4jVersion);
                    //TODO: Add announcement count!!!

                    //Network handling data
                    data.put("pid", new ApplicationPid().toString());
                    data.put("ip", BotSettings.RESTART_IP.get());
                    data.put("port", Integer.parseInt(BotSettings.RESTART_PORT.get()));

                    final OkHttpClient client = new OkHttpClient();

                    final RequestBody body = RequestBody.create(GlobalConst.JSON, data.toString());
                    final Request request = new Request.Builder()
                        .url(BotSettings.API_URL.get() + "/v2/status/keep-alive")
                        .post(body)
                        .header("Authorization", BotSettings.BOT_API_TOKEN.get())
                        .header("Content-Type", "application/json")
                        .build();

                    client.newCall(request).execute().close();
                } catch (final Exception e) {
                    LogFeed.log(LogObject
                        .forException("[Heart Beat]", "Failed to send keep-alive", e,
                            this.getClass()));
                }
            }
        }, seconds * 1000L, seconds * 1000L);
    }

    private static double usedMemory() {
        final long totalMemory = Runtime.getRuntime().totalMemory();
        final long freeMemory = Runtime.getRuntime().freeMemory();
        final double a = (totalMemory - freeMemory) / (double) (1024 * 1024);
        return (double) Math.round(a * 100) / 100;
    }

    private static String humanReadableUptime() {
        final RuntimeMXBean mxBean = ManagementFactory.getRuntimeMXBean();
        final Interval interval = new Interval(mxBean.getStartTime(), System.currentTimeMillis());
        final Period period = interval.toPeriod();

        return String.format("%d months, %d days, %d hours, %d minutes, %d seconds%n", period.getMonths(), period.getDays(), period.getHours(), period.getMinutes(), period.getSeconds());
    }
}
