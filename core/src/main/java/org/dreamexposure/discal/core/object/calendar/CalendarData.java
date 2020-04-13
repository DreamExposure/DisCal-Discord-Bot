package org.dreamexposure.discal.core.object.calendar;

import org.json.JSONObject;

import discord4j.rest.util.Snowflake;

/**
 * Created by Nova Fox on 11/10/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
public class CalendarData {
    public static CalendarData fromJson(JSONObject json) {
        Snowflake guildId = Snowflake.of(json.getString("guild_id"));
        int calendarNumber = json.getInt("calendar_number");

        String calendarId = json.getString("calendar_id");
        String calendarAddress = json.getString("calendar_address");
        boolean external = json.getBoolean("external");

        return new CalendarData(guildId, calendarNumber, calendarId, calendarAddress, external);
    }

    public static CalendarData fromData(Snowflake gId, int calNum, String calId,
                                        String calAddr, boolean ext) {
        return new CalendarData(gId, calNum, calId, calAddr, ext);
    }

    public static CalendarData empty() {
        return new CalendarData(Snowflake.of(0), 1, "primary", "primary", false);
    }

    private final Snowflake guildId;
    private final int calendarNumber;

    private final String calendarId;
    private final String calendarAddress;

    private final boolean external;

    private CalendarData(Snowflake guildId, int calendarNumber, String calendarId,
                         String calendarAddress, boolean external) {
        this.guildId = guildId;
        this.calendarNumber = calendarNumber;
        this.calendarId = calendarId;
        this.calendarAddress = calendarAddress;
        this.external = external;
    }

    //Getters
    public Snowflake getGuildId() {
        return guildId;
    }

    public int getCalendarNumber() {
        return calendarNumber;
    }

    public String getCalendarId() {
        return calendarId;
    }

    public String getCalendarAddress() {
        return calendarAddress;
    }

    public boolean isExternal() {
        return external;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();

        json.put("guild_id", guildId.asString());
        json.put("calendar_number", calendarNumber);
        json.put("calendar_id", calendarId);
        json.put("calendar_address", calendarAddress);
        json.put("external", external);

        return json;
    }
}