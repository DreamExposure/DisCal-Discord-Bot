package com.cloudcraftgaming.discal.internal.data;

/**
 * Created by Nova Fox on 3/26/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class CalendarData {
    private final String guildId;
    private final Integer calendarNumber;

    private String calendarId;
    private String calendarAddress;

    public CalendarData(String _guildID, Integer _calendarNumber) {
        guildId = _guildID;
        calendarNumber = _calendarNumber;
    }

    //Getters
    public String getGuildId() {
        return guildId;
    }

    public Integer getCalendarNumber() {
        return calendarNumber;
    }

    public String getCalendarId() {
        return calendarId;
    }

    public String getCalendarAddress() {
        return calendarAddress;
    }

    //Setters
    public void setCalendarId(String _calendarId) {
        calendarId = _calendarId;
    }

    public void setCalendarAddress(String _calendarAddress) {
        calendarAddress = _calendarAddress;
    }
}