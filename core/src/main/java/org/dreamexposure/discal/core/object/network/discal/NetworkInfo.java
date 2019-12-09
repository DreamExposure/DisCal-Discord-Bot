package org.dreamexposure.discal.core.object.network.discal;

import org.dreamexposure.discal.core.logger.Logger;
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
	private List<ConnectedClient> clients = new ArrayList<>();

	private int calCount;
	private int announcementCount;

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
		Logger.getLogger().status("Client Connected to Network", "Shard Index of Connected Client: " + client.getClientIndex());
	}

	public void removeClient(int clientIndex) {
		if (clientExists(clientIndex)) {
			clients.remove(getClient(clientIndex));
			Logger.getLogger().status("Client Disconnected to Network", "Shard Index of Disconnected Client: " + clientIndex);
		}
	}

	public int getTotalGuildCount() {
		int count = 0;
		for (ConnectedClient cc : clients) {
			count += cc.getConnectedServers();
		}

		return count;
	}

	public int getClientCount() {
		return clients.size();
	}

	public int getCalendarCount() {
		return this.calCount;
	}

	public int getAnnouncementCount() {
		return this.announcementCount;
	}

	public String getUptime() {
		RuntimeMXBean mxBean = ManagementFactory.getRuntimeMXBean();
		Interval interval = new Interval(mxBean.getStartTime(), System.currentTimeMillis());
		Period period = interval.toPeriod();

		return String.format("%d months, %d days, %d hours, %d minutes, %d seconds%n", period.getMonths(), period.getDays(), period.getHours(), period.getMinutes(), period.getSeconds());
	}

	//Setters

	public void setCalCount(int calCount) {
		this.calCount = calCount;
	}

	public void setAnnouncementCount(int announcementCount) {
		this.announcementCount = announcementCount;
	}

	public JSONObject toJson() {
		JSONObject json = new JSONObject();

		json.put("api_uptime", getUptime());
		json.put("announcements", getAnnouncementCount());
		json.put("total_guilds", getTotalGuildCount());
		json.put("calendars", getCalendarCount());

		JSONArray jClients = new JSONArray();
		for (ConnectedClient c : clients)
			jClients.put(c.toJson());

		json.put("clients", jClients);

		return json;
	}
}