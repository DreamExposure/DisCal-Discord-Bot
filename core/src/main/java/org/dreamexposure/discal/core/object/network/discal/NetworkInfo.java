package org.dreamexposure.discal.core.object.network.discal;

import org.dreamexposure.discal.core.logger.LogFeed;
import org.dreamexposure.discal.core.logger.object.LogObject;
import org.dreamexposure.discal.core.object.BotSettings;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.List;

/**
 * @author NovaFox161
 * Date Created: 9/8/2018
 * For Project: DisCal-Discord-Bot
 * Author Website: https://www.novamaday.com
 * Company Website: https://www.dreamexposure.org
 * Contact: nova@dreamexposure.org
 */
@SuppressWarnings("Duplicates")
public class NetworkInfo {
    private final List<ConnectedClient> clients = new ArrayList<>();

    private int calCount;
    private int announcementCount;
    private int guildCount;

    private String uptime;
    private String pid;

    //Getters
    public List<ConnectedClient> getClients() {
        return new ArrayList<>(clients);
    }

    public boolean clientExists(int clientIndex) {
        for (ConnectedClient cc : clients) {
            if (cc.getClientIndex() == clientIndex)
                return true;
        }
        return false;
    }

    public ConnectedClient getClient(int clientIndex) {
        for (ConnectedClient cc : clients) {
            if (cc.getClientIndex() == clientIndex)
                return cc;
        }
        return null;
    }

    public void addClient(ConnectedClient client) {
        clients.add(client);
        LogFeed.log(LogObject
                .forStatus("Client Connected to Network",
                        "Shard index of connected client: " + client.getClientIndex()));
    }

    public void removeClient(int clientIndex) {
        if (clientExists(clientIndex)) {
            clients.remove(getClient(clientIndex));
            LogFeed.log(LogObject
                    .forStatus("Client Disconnected from Network",
                            "Shard Index of Disconnected Client: " + clientIndex));
        }
    }

    public void removeClient(int clientIndex, String reason) {
        if (clientExists(clientIndex)) {
            clients.remove(getClient(clientIndex));
            LogFeed.log(LogObject
                    .forStatus("Client Disconnected from Network | Index: " + clientIndex, reason));
        }
    }

    public int getTotalGuildCount() {
        int count = 0;
        for (ConnectedClient cc : clients) {
            count += cc.getConnectedServers();
        }

        guildCount = count;
        return guildCount;
    }

    public int getClientCount() {
        return clients.size();
    }

    public int getExpectedClientCount() {
        return Integer.parseInt(BotSettings.SHARD_COUNT.get());
    }

    public int getCalendarCount() {
        return this.calCount;
    }

    public int getAnnouncementCount() {
        return this.announcementCount;
    }

    public String getUptimeLatest() {
        RuntimeMXBean mxBean = ManagementFactory.getRuntimeMXBean();
        Interval interval = new Interval(mxBean.getStartTime(), System.currentTimeMillis());
        Period period = interval.toPeriod();


        uptime = String.format("%d months, %d days, %d hours, %d minutes, %d seconds%n", period.getMonths(), period.getDays(), period.getHours(), period.getMinutes(), period.getSeconds());

        return uptime;
    }

    public String getUptime() {
        return uptime;
    }

    public String getPid() {
        return pid;
    }

    //Setters

    public void setCalCount(int calCount) {
        this.calCount = calCount;
    }

    public void setAnnouncementCount(int announcementCount) {
        this.announcementCount = announcementCount;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();

        json.put("api_uptime", getUptimeLatest());
        json.put("api_pid", getPid());
        json.put("announcements", getAnnouncementCount());
        json.put("total_guilds", getTotalGuildCount());
        json.put("calendars", getCalendarCount());

        JSONArray jClients = new JSONArray();
        for (ConnectedClient c : clients)
            jClients.put(c.toJson());

        json.put("clients", jClients);

        return json;
    }

    public NetworkInfo fromJson(JSONObject json) {
        uptime = json.getString("api_uptime");
        pid = json.getString("api_pid");
        announcementCount = json.getInt("announcements");
        guildCount = json.getInt("total_guilds");
        calCount = json.getInt("calendars");

        JSONArray jClients = json.getJSONArray("clients");
        for (int i = 0; i < jClients.length(); i++)
            clients.add(new ConnectedClient().fromJson(jClients.getJSONObject(i)));

        return this;
    }
}