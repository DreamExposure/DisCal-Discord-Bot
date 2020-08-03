package org.dreamexposure.discal.core.object;

import org.dreamexposure.discal.core.calendar.CalendarAuth;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import discord4j.common.util.Snowflake;

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

    private int credentialsId;

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

    public GuildSettings(final Snowflake _guildId) {
        this.guildID = _guildId;

        this.externalCalendar = false;
        this.privateKey = "N/a";

        this.credentialsId = new Random().nextInt(CalendarAuth.credentialsCount());

        this.encryptedAccessToken = "N/a";
        this.encryptedRefreshToken = "N/a";

        this.controlRole = "everyone";
        this.discalChannel = "all";

        this.simpleAnnouncements = false;
        this.lang = "ENGLISH";
        this.prefix = "!";

        this.patronGuild = false;
        this.devGuild = false;
        this.maxCalendars = 1;

        this.twelveHour = true;
    }

    //Getters
    public Snowflake getGuildID() {
        return this.guildID;
    }

    public boolean useExternalCalendar() {
        return this.externalCalendar;
    }

    public String getPrivateKey() {
        return this.privateKey;
    }

    public int getCredentialsId() {
        return this.credentialsId;
    }

    public String getEncryptedAccessToken() {
        return this.encryptedAccessToken;
    }

    public String getEncryptedRefreshToken() {
        return this.encryptedRefreshToken;
    }

    public String getControlRole() {
        return this.controlRole;
    }

    public String getDiscalChannel() {
        return this.discalChannel;
    }

    public boolean usingSimpleAnnouncements() {
        return this.simpleAnnouncements;
    }

    public String getLang() {
        return this.lang;
    }

    public String getPrefix() {
        return this.prefix;
    }

    public boolean isPatronGuild() {
        return this.patronGuild;
    }

    public boolean isDevGuild() {
        return this.devGuild;
    }

    public int getMaxCalendars() {
        return this.maxCalendars;
    }

    public boolean useTwelveHour() {
        return this.twelveHour;
    }

    public boolean isBranded() {
        return this.branded;
    }

    public ArrayList<String> getDmAnnouncements() {
        return this.dmAnnouncements;
    }

    @SuppressWarnings("Duplicates")
    public String getDmAnnouncementsString() {
        StringBuilder users = new StringBuilder();
        int i = 0;
        for (final String sub : this.dmAnnouncements) {
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
    public void setUseExternalCalendar(final boolean _useExternal) {
        this.externalCalendar = _useExternal;
    }

    public void setPrivateKey(final String _privateKey) {
        this.privateKey = _privateKey;
    }

    public void setCredentialsId(final int credentialsId) {
        this.credentialsId = credentialsId;
    }

    public void setEncryptedAccessToken(final String _access) {
        this.encryptedAccessToken = _access;
    }

    public void setEncryptedRefreshToken(final String _refresh) {
        this.encryptedRefreshToken = _refresh;
    }

    public void setControlRole(final String _controlRole) {
        this.controlRole = _controlRole;
    }

    public void setDiscalChannel(final String _discalChannel) {
        this.discalChannel = _discalChannel;
    }

    public void setSimpleAnnouncements(final boolean _simpleAnnouncements) {
        this.simpleAnnouncements = _simpleAnnouncements;
    }

    public void setLang(final String _lang) {
        this.lang = _lang;
    }

    public void setPrefix(final String _prefix) {
        this.prefix = _prefix;
    }

    public void setPatronGuild(final boolean _patronGuild) {
        this.patronGuild = _patronGuild;
    }

    public void setDevGuild(final boolean _devGuild) {
        this.devGuild = _devGuild;
    }

    public void setMaxCalendars(final Integer _maxCalendars) {
        this.maxCalendars = _maxCalendars;
    }

    public void setTwelveHour(final boolean _twelveHour) {
        this.twelveHour = _twelveHour;
    }

    public void setBranded(final boolean _branded) {
        this.branded = _branded;
    }

    public void setDmAnnouncementsFromString(final String userList) {
        final String[] subs = userList.split(",");
        Collections.addAll(this.dmAnnouncements, subs);
    }

    public JSONObject toJson() {
        final JSONObject data = new JSONObject();

        data.put("guild_id", this.guildID.asString());
        data.put("external_calendar", this.externalCalendar);
        data.put("private_key", this.privateKey);
        data.put("credentials_id", this.credentialsId);
        data.put("access_token", this.encryptedAccessToken);
        data.put("refresh_token", this.encryptedRefreshToken);
        data.put("control_role", this.controlRole);
        data.put("discal_channel", this.discalChannel);
        data.put("simple_announcements", this.simpleAnnouncements);
        data.put("lang", this.lang);
        data.put("prefix", this.prefix);
        data.put("patron_guild", this.patronGuild);
        data.put("dev_guild", this.devGuild);
        data.put("max_calendars", this.maxCalendars);
        data.put("twelve_hour", this.twelveHour);
        data.put("branded", this.branded);

        return data;
    }

    public JSONObject toJsonSecure() {
        final JSONObject data = new JSONObject();

        data.put("guild_id", this.guildID.asString());
        data.put("external_calendar", this.externalCalendar);
        data.put("control_role", this.controlRole);
        data.put("discal_channel", this.discalChannel);
        data.put("simple_announcement", this.simpleAnnouncements);
        data.put("lang", this.lang);
        data.put("prefix", this.prefix);
        data.put("patron_guild", this.patronGuild);
        data.put("dev_guild", this.devGuild);
        data.put("max_calendars", this.maxCalendars);
        data.put("twelve_hour", this.twelveHour);
        data.put("branded", this.branded);

        return data;
    }

    public GuildSettings fromJson(final JSONObject data) {
        this.guildID = Snowflake.of(data.getString("guild_id"));
        this.externalCalendar = data.getBoolean("external_calendar");
        this.privateKey = data.getString("private_key");
        this.credentialsId = data.getInt("credentials_id");
        this.encryptedAccessToken = data.getString("access_token");
        this.encryptedRefreshToken = data.getString("refresh_token");
        this.controlRole = data.getString("control_role");
        this.discalChannel = data.getString("discal_channel");
        this.simpleAnnouncements = data.getBoolean("simple_announcement");
        this.lang = data.getString("lang");
        this.prefix = data.getString("prefix");
        this.patronGuild = data.getBoolean("patron_guild");
        this.devGuild = data.getBoolean("dev_guild");
        this.maxCalendars = data.getInt("max_calendars");
        this.twelveHour = data.getBoolean("twelve_hour");
        this.branded = data.getBoolean("branded");

        return this;
    }

    public GuildSettings fromJsonSecure(final JSONObject data) {
        this.guildID = Snowflake.of(data.getString("guild_id"));
        this.externalCalendar = data.getBoolean("external_calendar");
        //credentialsId = data.getInt("credentials_id");
        //privateKey = data.getString("PrivateKey");
        //encryptedAccessToken = data.getString("AccessToken");
        //encryptedRefreshToken = data.getString("RefreshToken");
        this.controlRole = data.getString("control_role");
        this.discalChannel = data.getString("discal_channel");
        this.simpleAnnouncements = data.getBoolean("simple_announcement");
        this.lang = data.getString("lang");
        this.prefix = data.getString("prefix");
        this.patronGuild = data.getBoolean("patron_guild");
        this.devGuild = data.getBoolean("dev_guild");
        this.maxCalendars = data.getInt("max_calendars");
        this.twelveHour = data.getBoolean("twelve_hours");
        this.branded = data.getBoolean("branded");

        return this;
    }
}