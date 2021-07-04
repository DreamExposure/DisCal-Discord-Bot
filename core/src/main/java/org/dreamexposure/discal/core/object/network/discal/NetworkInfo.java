package org.dreamexposure.discal.core.object.network.discal;

import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.logger.LogFeed;
import org.dreamexposure.discal.core.logger.object.LogObject;
import org.dreamexposure.discal.core.utils.GlobalVal;
import org.dreamexposure.discal.core.utils.JsonUtil;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.Comparator;
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
@Component
public class NetworkInfo {
    private final List<ConnectedClient> clients = new ArrayList<>();

    private int calCount;
    private int announcementCount;
    private int guildCount;

    private String uptime;
    private String pid;


    public Mono<Void> update() {
        return DatabaseManager.INSTANCE.getCalendarCount()
            .doOnNext(i -> this.calCount = i)
            .then(DatabaseManager.INSTANCE.getAnnouncementCount())
            .doOnNext(i -> this.announcementCount = i)
            .then();
    }

    //Getters
    public List<ConnectedClient> getClients() {
        return new ArrayList<>(this.clients);
    }

    public boolean doesClientExist(final int clientIndex) {
        for (final ConnectedClient cc : this.clients) {
            if (cc.getClientIndex() == clientIndex)
                return true;
        }
        return false;
    }

    public ConnectedClient getClient(final int clientIndex) {
        for (final ConnectedClient cc : this.clients) {
            if (cc.getClientIndex() == clientIndex)
                return cc;
        }
        return null;
    }

    public void addClient(final ConnectedClient client) {
        this.clients.add(client);
        this.clients.sort(Comparator.comparingInt(ConnectedClient::getClientIndex));

        LogFeed.log(LogObject
            .forStatus("Client Connected to Network",
                "Shard index of connected client: " + client.getClientIndex()));
    }

    public void updateClient(ConnectedClient client) {
        if (this.doesClientExist(client.getClientIndex()))
            this.clients.remove(this.getClient(client.getClientIndex()));
        this.clients.add(client);
        this.clients.sort(Comparator.comparingInt(ConnectedClient::getClientIndex));
    }

    public void removeClient(final int clientIndex) {
        if (this.doesClientExist(clientIndex)) {
            this.clients.remove(this.getClient(clientIndex));
            LogFeed.log(LogObject
                .forStatus("Client Disconnected from Network",
                    "Shard Index of Disconnected Client: " + clientIndex));
        }
    }

    public void removeClient(final int clientIndex, final String reason) {
        if (this.doesClientExist(clientIndex)) {
            this.clients.remove(this.getClient(clientIndex));
            LogFeed.log(LogObject
                .forStatus("Client Disconnected from Network | Index: " + clientIndex, reason));
        }
    }

    public String getVersion() {
        return GlobalVal.getVersion();
    }

    public String getD4JVersion() {
        return GlobalVal.getD4jVersion();
    }

    public int getTotalGuildCount() {
        int count = 0;
        for (final ConnectedClient cc : this.clients) {
            count += cc.getConnectedServers();
        }

        this.guildCount = count;
        return this.guildCount;
    }

    public int getClientCount() {
        return this.clients.size();
    }

    public int getExpectedClientCount() {
        if (!clients.isEmpty())
            return clients.get(0).getExpectedClientCount();
        else
            return -1;
    }

    public int getCalendarCount() {
        return this.calCount;
    }

    public int getAnnouncementCount() {
        return this.announcementCount;
    }

    public String getUptimeLatest() {
        final RuntimeMXBean mxBean = ManagementFactory.getRuntimeMXBean();
        final Interval interval = new Interval(mxBean.getStartTime(), System.currentTimeMillis());
        final Period period = interval.toPeriod();


        this.uptime = String.format("%d months, %d days, %d hours, %d minutes, %d seconds%n", period.getMonths(), period.getDays(), period.getHours(), period.getMinutes(), period.getSeconds());

        return this.uptime;
    }

    public String getUptime() {
        return this.uptime;
    }

    public String getPid() {
        return this.pid;
    }

    //Setters
    public void setCalCount(final int calCount) {
        this.calCount = calCount;
    }

    public void setAnnouncementCount(final int announcementCount) {
        this.announcementCount = announcementCount;
    }

    public void setPid(final String pid) {
        this.pid = pid;
    }

    public JSONObject toJson() {
        final JSONObject json = new JSONObject();

        json.put("api_version", this.getVersion());
        json.put("d4j_version", this.getD4JVersion());
        json.put("api_uptime", this.getUptimeLatest());
        json.put("api_pid", this.getPid());
        json.put("announcements", this.getAnnouncementCount());
        json.put("total_guilds", this.getTotalGuildCount());
        json.put("calendars", this.getCalendarCount());

        final JSONArray jClients = new JSONArray();
        for (final ConnectedClient c : this.clients)
            jClients.put(JsonUtil.INSTANCE.encodeToJSON(ConnectedClient.class, c));

        json.put("clients", jClients);

        return json;
    }

    public NetworkInfo fromJson(final JSONObject json) {
        this.uptime = json.getString("api_uptime");
        this.pid = json.getString("api_pid");
        this.announcementCount = json.getInt("announcements");
        this.guildCount = json.getInt("total_guilds");
        this.calCount = json.getInt("calendars");

        final JSONArray jClients = json.getJSONArray("clients");

        for (int i = 0; i < jClients.length(); i++)
            this.clients.add(JsonUtil.INSTANCE.decodeFromJSON(ConnectedClient.class, jClients.getJSONObject(i)));

        return this;
    }
}
