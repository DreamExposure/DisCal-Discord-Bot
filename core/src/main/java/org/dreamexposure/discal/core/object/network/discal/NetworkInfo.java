package org.dreamexposure.discal.core.object.network.discal;

import org.dreamexposure.discal.Application;
import org.dreamexposure.discal.GitProperty;
import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.utils.JsonUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.dreamexposure.discal.core.utils.GlobalVal.getSTATUS;

/**
 * @author NovaFox161
 * Date Created: 9/8/2018
 * For Project: DisCal-Discord-Bot
 * Author Website: https://www.novamaday.com
 * Company Website: https://www.dreamexposure.org
 * Contact: nova@dreamexposure.org
 */
@SuppressWarnings("Duplicates")
//FIXME: Remove
@Deprecated
@Component
public class NetworkInfo {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    private final List<ConnectedClient> clients = new CopyOnWriteArrayList<>();

    private int calCount;
    private int announcementCount;
    private int guildCount;

    private String uptime;
    private UUID instanceId;


    public Mono<Void> update() {
        return DatabaseManager.INSTANCE.getCalendarCount()
            .doOnNext(i -> this.calCount = i)
            .then(DatabaseManager.INSTANCE.getAnnouncementCount())
            .doOnNext(i -> this.announcementCount = i)
            .then();
    }

    //Getters
    public List<ConnectedClient> getClients() {
        return new CopyOnWriteArrayList<>(this.clients);
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

        LOGGER.info(getSTATUS(), "Client connected to network | Index: " + client.getClientIndex());
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

            LOGGER.warn(getSTATUS(), "Client disconnected from network | Index: " + clientIndex);
        }
    }

    public void removeClient(final int clientIndex, final String reason) {
        if (this.doesClientExist(clientIndex)) {
            this.clients.remove(this.getClient(clientIndex));

            LOGGER.warn(getSTATUS(), "Client disconnected from network | Index: " + clientIndex + " | Reason: " + reason);
        }
    }

    public String getVersion() {
        return GitProperty.DISCAL_VERSION.getValue();
    }

    public String getD4JVersion() {
        return GitProperty.DISCAL_VERSION_D4J.getValue();
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

    public String getUptime() {
        return this.uptime;
    }

    public UUID getInstanceId() {
        return this.instanceId;
    }

    //Setters
    public void setCalCount(final int calCount) {
        this.calCount = calCount;
    }

    public void setAnnouncementCount(final int announcementCount) {
        this.announcementCount = announcementCount;
    }

    public void setInstanceId(final UUID instanceId) {
        this.instanceId = instanceId;
    }

    public JSONObject toJson() {
        final JSONObject json = new JSONObject();

        json.put("api_version", this.getVersion());
        json.put("d4j_version", this.getD4JVersion());
        json.put("api_uptime", Application.getHumanReadableUptime());
        json.put("api_instance_id", this.instanceId);
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
        this.instanceId = UUID.fromString(json.getString("api_instance_id"));
        this.announcementCount = json.getInt("announcements");
        this.guildCount = json.getInt("total_guilds");
        this.calCount = json.getInt("calendars");

        final JSONArray jClients = json.getJSONArray("clients");

        for (int i = 0; i < jClients.length(); i++)
            this.clients.add(JsonUtil.INSTANCE.decodeFromJSON(ConnectedClient.class, jClients.getJSONObject(i)));

        return this;
    }
}
