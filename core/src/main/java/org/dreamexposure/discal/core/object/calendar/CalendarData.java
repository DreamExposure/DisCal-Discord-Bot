package org.dreamexposure.discal.core.object.calendar;

import org.json.JSONObject;

import discord4j.common.util.Snowflake;

/**
 * Created by Nova Fox on 11/10/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
public class CalendarData {
    public static CalendarData fromJson(final JSONObject json) {
        final Snowflake guildId = Snowflake.of(json.getString("guild_id"));
        final int calendarNumber = json.getInt("calendar_number");

        final String calendarId = json.getString("calendar_id");
        final String calendarAddress = json.getString("calendar_address");
        final boolean external = json.getBoolean("external");
        final int credentialId = json.optInt("credential_id", 0);

        return new CalendarData(guildId, calendarNumber, calendarId, calendarAddress, external, credentialId);
    }

    public static CalendarData fromData(final Snowflake gId, final int calNum, final String calId,
                                        final String calAddr, final boolean ext, final int credId) {
        return new CalendarData(gId, calNum, calId, calAddr, ext, credId);
    }

    public static CalendarData empty() {
        return new CalendarData(Snowflake.of(0), 0, "primary", "primary", false, 0);
    }

    private final Snowflake guildId;
    private final int calendarNumber;

    private final String calendarId;
    private final String calendarAddress;

    private final boolean external;

    private final int credentialId;

    private CalendarData(final Snowflake guildId, final int calendarNumber, final String calendarId,
                         final String calendarAddress, final boolean external, final int credentialId) {
        this.guildId = guildId;
        this.calendarNumber = calendarNumber;
        this.calendarId = calendarId;
        this.calendarAddress = calendarAddress;
        this.external = external;
        this.credentialId = credentialId;
    }

    //Getters
    public Snowflake getGuildId() {
        return this.guildId;
    }

    public int getCalendarNumber() {
        return this.calendarNumber;
    }

    public String getCalendarId() {
        return this.calendarId;
    }

    public String getCalendarAddress() {
        return this.calendarAddress;
    }

    public boolean isExternal() {
        return this.external;
    }

    public int getCredentialId() {
        return this.credentialId;
    }

    public JSONObject toJson() {
        final JSONObject json = new JSONObject();

        json.put("guild_id", this.guildId.asString());
        json.put("calendar_number", this.calendarNumber);
        json.put("calendar_id", this.calendarId);
        json.put("calendar_address", this.calendarAddress);
        json.put("external", this.external);
        json.put("credential_id", this.credentialId);

        return json;
    }
}