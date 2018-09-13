package org.dreamexposure.discal.core.object.network.discal;

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
	private final String clientHostname;
	private final int clientPort;

	private int connectedServers;
	private long lastKeepAlive;
	private String uptime;
	private double memUsed;

	public ConnectedClient(int _clientIndex, String _clientHostname, int _clientPort) {
		clientIndex = _clientIndex;
		clientHostname = _clientHostname;
		clientPort = _clientPort;

		connectedServers = 0;
		lastKeepAlive = System.currentTimeMillis();

		uptime = "ERROR";
		memUsed = 0;
	}

	//Getters
	public int getClientIndex() {
		return clientIndex;
	}

	public String getClientHostname() {
		return clientHostname;
	}

	public int getClientPort() {
		return clientPort;
	}

	public int getConnectedServers() {
		return connectedServers;
	}

	public long getLastKeepAlive() {
		return lastKeepAlive;
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
}