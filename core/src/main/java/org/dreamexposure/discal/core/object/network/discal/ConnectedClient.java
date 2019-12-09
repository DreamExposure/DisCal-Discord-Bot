package org.dreamexposure.discal.core.object.network.discal;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author NovaFox161
 * Date Created: 9/8/2018
 * For Project: DisCal-Discord-Bot
 * Author Website: https://www.novamaday.com
 * Company Website: https://www.dreamexposure.org
 * Contact: nova@dreamexposure.org
 */
public class ConnectedClient {
	private final int clientIndex;

	private int connectedServers;
	private long lastKeepAlive;
	private String uptime;
	private double memUsed;

	public ConnectedClient(int _clientIndex) {
		clientIndex = _clientIndex;

		connectedServers = 0;
		lastKeepAlive = System.currentTimeMillis();

		uptime = "ERROR";
		memUsed = 0;
	}

	//Getters
	public int getClientIndex() {
		return clientIndex;
	}

	public int getConnectedServers() {
		return connectedServers;
	}

	public long getLastKeepAlive() {
		return lastKeepAlive;
	}

	public String getLastKeepAliveHumanReadable() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

		return sdf.format(new Date(lastKeepAlive));
	}

	public String getUptime() {
		return uptime;
	}

	public double getMemUsed() {
		return memUsed;
	}

	//Setters
	public void setConnectedServers(int _connectedServers) {
		connectedServers = _connectedServers;
	}

	public void setLastKeepAlive(long _lastKeepAlive) {
		lastKeepAlive = _lastKeepAlive;
	}

	public void setUptime(String _uptime) {
		uptime = _uptime;
	}

	public void setMemUsed(double _mem) {
		memUsed = _mem;
	}

	public JSONObject toJson() {
		JSONObject json = new JSONObject();

		json.put("index", clientIndex);
		json.put("guilds", connectedServers);
		json.put("keep_alive", lastKeepAlive);
		json.put("uptime", uptime);
		json.put("memory", memUsed);

		return json;
	}
}