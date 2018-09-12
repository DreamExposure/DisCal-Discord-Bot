package org.dreamexposure.discal.core.object.calendar;

/**
 * Created by Nova Fox on 11/10/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
public class CalendarData {
	private final long guildId;
	private final int calendarNumber;

	private String calendarId;
	private String calendarAddress;

	private boolean external;

	public CalendarData(long _guildID, int _calendarNumber) {
		guildId = _guildID;
		calendarNumber = _calendarNumber;

		calendarId = "primary";
		calendarAddress = "primary";

		external = false;
	}

	//Getters
	public long getGuildId() {
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
}