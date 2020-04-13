package org.dreamexposure.discal.core.object;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

import discord4j.rest.util.Snowflake;

/**
 * Created by Nova Fox on 11/10/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
@SuppressWarnings("DuplicatedCode")
public class GuildSettings {
    private Snowflake guildID;

    private boolean externalCalendar;
    private String privateKey;

    private String encryptedAccessToken;
    private String encryptedRefreshToken;

    private String controlRole;
    private String discalChannel;

    private boolean simpleAnnouncements;
    private String lang;
    private String prefix;

    private boolean patronGuild;
    private boolean devGuild;
    private int maxCalendars;

    private boolean twelveHour;
    private boolean branded;

    private final ArrayList<String> dmAnnouncements = new ArrayList<>();

    public GuildSettings(Snowflake _guildId) {
        guildID = _guildId;

        externalCalendar = false;
        privateKey = "N/a";

        encryptedAccessToken = "N/a";
        encryptedRefreshToken = "N/a";

        controlRole = "everyone";
        discalChannel = "all";

        simpleAnnouncements = false;
        lang = "ENGLISH";
        prefix = "!";

        patronGuild = false;
        devGuild = false;
        maxCalendars = 1;

        twelveHour = true;
    }

    //Getters
    public Snowflake getGuildID() {
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

    public boolean usingSimpleAnnouncements() {
        return simpleAnnouncements;
    }

    public String getLang() {
        return lang;
    }

    public String getPrefix() {
        return prefix;
    }

    public boolean isPatronGuild() {
        return patronGuild;
    }

    public boolean isDevGuild() {
        return devGuild;
    }

    public int getMaxCalendars() {
        return maxCalendars;
    }

    public boolean useTwelveHour() {
        return twelveHour;
    }

    public boolean isBranded() {
        return branded;
    }

    public ArrayList<String> getDmAnnouncements() {
        return dmAnnouncements;
    }

    @SuppressWarnings("Duplicates")
    public String getDmAnnouncementsString() {
        StringBuilder users = new StringBuilder();
        int i = 0;
        for (String sub : dmAnnouncements) {
            if (i == 0) {
                users = new StringBuilder(sub);
            } else {
                users.append(",").append(sub);
            }
            i++;
        }
        return users.toString();
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

    public void setSimpleAnnouncements(boolean _simpleAnnouncements) {
        simpleAnnouncements = _simpleAnnouncements;
    }

    public void setLang(String _lang) {
        lang = _lang;
    }

    public void setPrefix(String _prefix) {
        prefix = _prefix;
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

    public void setTwelveHour(boolean _twelveHour) {
        twelveHour = _twelveHour;
    }

    public void setBranded(boolean _branded) {
        branded = _branded;
    }

    public void setDmAnnouncementsFromString(String userList) {
        String[] subs = userList.split(",");
        Collections.addAll(dmAnnouncements, subs);
    }

    public JSONObject toJson() {
        JSONObject data = new JSONObject();

        data.put("guild_id", guildID.asString());
        data.put("external_calendar", externalCalendar);
        data.put("private_key", privateKey);
        data.put("access_token", encryptedAccessToken);
        data.put("refresh_token", encryptedRefreshToken);
        data.put("control_role", controlRole);
        data.put("discal_channel", discalChannel);
        data.put("simple_announcements", simpleAnnouncements);
        data.put("lang", lang);
        data.put("prefix", prefix);
        data.put("patron_guild", patronGuild);
        data.put("dev_guild", devGuild);
        data.put("max_calendars", maxCalendars);
        data.put("twelve_hour", twelveHour);
        data.put("branded", branded);

        return data;
    }

    public JSONObject toJsonSecure() {
        JSONObject data = new JSONObject();

        data.put("guild_id", guildID.asString());
        data.put("external_calendar", externalCalendar);
        data.put("control_role", controlRole);
        data.put("discal_channel", discalChannel);
        data.put("simple_announcement", simpleAnnouncements);
        data.put("lang", lang);
        data.put("prefix", prefix);
        data.put("patron_guild", patronGuild);
        data.put("dev_guild", devGuild);
        data.put("max_calendars", maxCalendars);
        data.put("twelve_hour", twelveHour);
        data.put("branded", branded);

        return data;
    }

    public GuildSettings fromJson(JSONObject data) {
        guildID = Snowflake.of(data.getString("guild_id"));
        externalCalendar = data.getBoolean("external_calendar");
        privateKey = data.getString("private_key");
        encryptedAccessToken = data.getString("access_token");
        encryptedRefreshToken = data.getString("refresh_token");
        controlRole = data.getString("control_role");
        discalChannel = data.getString("discal_channel");
        simpleAnnouncements = data.getBoolean("simple_announcement");
        lang = data.getString("lang");
        prefix = data.getString("prefix");
        patronGuild = data.getBoolean("patron_guild");
        devGuild = data.getBoolean("dev_guild");
        maxCalendars = data.getInt("max_calendars");
        twelveHour = data.getBoolean("twelve_hour");
        branded = data.getBoolean("branded");

        return this;
    }

    public GuildSettings fromJsonSecure(JSONObject data) {
        guildID = Snowflake.of(data.getString("guild_id"));
        externalCalendar = data.getBoolean("external_calendar");
        //privateKey = data.getString("PrivateKey");
        //encryptedAccessToken = data.getString("AccessToken");
        //encryptedRefreshToken = data.getString("RefreshToken");
        controlRole = data.getString("control_role");
        discalChannel = data.getString("discal_channel");
        simpleAnnouncements = data.getBoolean("simple_announcement");
        lang = data.getString("lang");
        prefix = data.getString("prefix");
        patronGuild = data.getBoolean("patron_guild");
        devGuild = data.getBoolean("dev_guild");
        maxCalendars = data.getInt("max_calendars");
        twelveHour = data.getBoolean("twelve_hours");
        branded = data.getBoolean("branded");

        return this;
    }
}