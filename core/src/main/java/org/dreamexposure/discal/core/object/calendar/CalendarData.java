package org.dreamexposure.discal.core.object.calendar;

import org.json.JSONObject;

import discord4j.core.object.util.Snowflake;

/**
 * Created by Nova Fox on 11/10/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
public class CalendarData {
	private Snowflake guildId;
	private int calendarNumber;

	private String calendarId;
	private String calendarAddress;

	private boolean external;

	public CalendarData(Snowflake _guildID, int _calendarNumber) {
		guildId = _guildID;
		calendarNumber = _calendarNumber;

		calendarId = "primary";
		calendarAddress = "primary";

		external = false;
	}

	public CalendarData() {

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

	//Setters
	public void setCalendarId(String _calendarId) {
		calendarId = _calendarId;
	}

	public void setCalendarAddress(String _calendarAddress) {
		calendarAddress = _calendarAddress;
	}

	public void setExternal(boolean _external) {
		external = _external;
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

	public CalendarData fromJson(JSONObject json) {
		guildId = Snowflake.of(json.getString("guild_id"));
		calendarNumber = json.getInt("calendar_number");

		calendarId = json.getString("calendar_id");
		calendarAddress = json.getString("calendar_address");
		external = json.getBoolean("external");

		return this;
	}
}