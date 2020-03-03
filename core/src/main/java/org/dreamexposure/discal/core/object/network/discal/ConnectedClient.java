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
	private int clientIndex;

	private int connectedServers;
	private long lastKeepAlive;
	private String uptime;
	private double memUsed;

	//This stuff doesn't get published
	private String ipForRestart;
	private int portForRestart;
	private String pid;

	public ConnectedClient() {
		clientIndex = -1;

		connectedServers = 0;
		lastKeepAlive = System.currentTimeMillis();

		uptime = "ERROR";
		memUsed = 0;
	}

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

	public String getIpForRestart() {
		return ipForRestart;
	}

	public int getPortForRestart() {
		return portForRestart;
	}

	public String getPid() {
		return pid;
	}

	//Setters
	public void setClientIndex(int clientIndex) {
		this.clientIndex = clientIndex;
	}

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

	public void setIpForRestart(String _ip) {
		ipForRestart = _ip;
	}

	public void setPortForRestart(int portForRestart) {
		this.portForRestart = portForRestart;
	}

	public void setPid(String _pid) {
		pid = _pid;
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

	public ConnectedClient fromJson(JSONObject json) {
		clientIndex = json.getInt("index");
		connectedServers = json.getInt("guilds");
		lastKeepAlive = json.getLong("keep_alive");
		uptime = json.getString("uptime");
		memUsed = json.getDouble("memory");

		return this;
	}
}