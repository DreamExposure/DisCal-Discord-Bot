package com.cloudcraftgaming.discal.internal.data;

/**
 * Created by Nova Fox on 3/26/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class GuildSettings {
    private final String guildID;

    private boolean externalCalendar;
    private String privateKey;

    private String encryptedAccessToken;
    private String encryptedRefreshToken;

    private String controlRole;
    private String discalChannel;

    private boolean patronGuild;
    private boolean devGuild;
    private Integer maxCalendars;

    public GuildSettings(String _guildId) {
        guildID = _guildId;

        externalCalendar = false;
        privateKey = "N/a";

        encryptedAccessToken = "N/a";
        encryptedRefreshToken = "N/a";

        controlRole = "everyone";
        discalChannel = "all";

        patronGuild = false;
        devGuild = false;
        maxCalendars = 1;
    }

    //Getters
    public String getGuildID() {
        return guildID;
    }

    public boolean useExternalCalendar() {
        return externalCalendar;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public String getEncryptedAccessToken() {
        return encryptedAccessToken;
    }

    public String getEncryptedRefreshToken() {
        return encryptedRefreshToken;
    }

    public String getControlRole() {
        return controlRole;
    }

    public String getDiscalChannel() {
        return discalChannel;
    }

    public boolean isPatronGuild() {
        return patronGuild;
    }

    public boolean isDevGuild() {
        return devGuild;
    }

    public Integer getMaxCalendars() {
        return maxCalendars;
    }

    //Setters
    public void setUseExternalCalendar(boolean _useExternal) {
        externalCalendar = _useExternal;
    }

    public void setPrivateKey(String _privateKey) {
        privateKey = _privateKey;
    }

    public void setEncryptedAccessToken(String _access) {
        encryptedAccessToken = _access;
    }

    public void setEncryptedRefreshToken(String _refresh) {
        encryptedRefreshToken = _refresh;
    }

    public void setControlRole(String _controlRole) {
        controlRole = _controlRole;
    }

    public void setDiscalChannel(String _discalChannel) {
        discalChannel = _discalChannel;
    }

    public void setPatronGuild(boolean _patronGuild) {
        patronGuild = _patronGuild;
    }

    public void setDevGuild(boolean _devGuild) {
        devGuild = _devGuild;
    }

    public void setMaxCalendars(Integer _maxCalendars) {
        maxCalendars = _maxCalendars;
    }
}