package org.dreamexposure.discal.core.object.network.discal;

import org.dreamexposure.discal.core.utils.GlobalConst;
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

    private String version;
    private String d4jVersion;

    private int connectedServers;
    private long lastKeepAlive;
    private String uptime;
    private double memUsed;

    //This stuff doesn't get published
    private String ipForRestart;
    private int portForRestart;
    private String pid;

    ConnectedClient() {
        this.clientIndex = -1;

        this.version = GlobalConst.version;
        this.d4jVersion = GlobalConst.d4jVersion;

        this.connectedServers = 0;
        this.lastKeepAlive = System.currentTimeMillis();

        this.uptime = "ERROR";
        this.memUsed = 0;
    }

    public ConnectedClient(final int _clientIndex) {
        this.clientIndex = _clientIndex;

        this.version = GlobalConst.version;
        this.d4jVersion = GlobalConst.d4jVersion;

        this.connectedServers = 0;
        this.lastKeepAlive = System.currentTimeMillis();

        this.uptime = "ERROR";
        this.memUsed = 0;
    }

    //Getters
    public int getClientIndex() {
        return this.clientIndex;
    }

    public String getVersion() {
        return this.version;
    }

    public String getD4JVersion() {
        return this.d4jVersion;
    }

    public int getConnectedServers() {
        return this.connectedServers;
    }

    public long getLastKeepAlive() {
        return this.lastKeepAlive;
    }

    public String getLastKeepAliveHumanReadable() {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

        return sdf.format(new Date(this.lastKeepAlive));
    }

    public String getUptime() {
        return this.uptime;
    }

    public double getMemUsed() {
        return this.memUsed;
    }

    public String getIpForRestart() {
        return this.ipForRestart;
    }

    public int getPortForRestart() {
        return this.portForRestart;
    }

    public String getPid() {
        return this.pid;
    }

    //Setters
    public void setClientIndex(final int clientIndex) {
        this.clientIndex = clientIndex;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    public void setD4JVersion(final String d4jVersion) {
        this.d4jVersion = d4jVersion;
    }

    public void setConnectedServers(final int _connectedServers) {
        this.connectedServers = _connectedServers;
    }

    public void setLastKeepAlive(final long _lastKeepAlive) {
        this.lastKeepAlive = _lastKeepAlive;
    }

    public void setUptime(final String _uptime) {
        this.uptime = _uptime;
    }

    public void setMemUsed(final double _mem) {
        this.memUsed = _mem;
    }

    public void setIpForRestart(final String _ip) {
        this.ipForRestart = _ip;
    }

    public void setPortForRestart(final int portForRestart) {
        this.portForRestart = portForRestart;
    }

    public void setPid(final String _pid) {
        this.pid = _pid;
    }

    public JSONObject toJson() {
        final JSONObject json = new JSONObject();

        json.put("index", this.clientIndex);
        json.put("version", this.version);
        json.put("d4j_version", this.d4jVersion);
        json.put("guilds", this.connectedServers);
        json.put("keep_alive", this.lastKeepAlive);
        json.put("uptime", this.uptime);
        json.put("memory", this.memUsed);

        return json;
    }

    public ConnectedClient fromJson(final JSONObject json) {
        this.clientIndex = json.getInt("index");
        this.version = json.getString("version");
        this.d4jVersion = json.getString("d4j_version");
        this.connectedServers = json.getInt("guilds");
        this.lastKeepAlive = json.getLong("keep_alive");
        this.uptime = json.getString("uptime");
        this.memUsed = json.getDouble("memory");

        return this;
    }
}