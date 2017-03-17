package com.cloudcraftgaming.discal.internal.data;

/**
 * Created by Nova Fox on 1/2/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class BotData {
    private final String guildId;

    private String calendarId;
    private String calendarAddress;
    private String controlRole;
    private String channel;

    public BotData(String _guildId) {
        guildId = _guildId;

        calendarId = "primary";
        calendarAddress = "primary";
        controlRole = "everyone";
        channel = "all";
    }

    //Getters
    public String getGuildId() {
        return guildId;
    }

    public String getCalendarId() {
        return calendarId;
    }

    public String getCalendarAddress() {
        return calendarAddress;
    }

    public String getControlRole() {
        return controlRole;
    }

    public String getChannel() {
        return channel;
    }

    //Setters
    public void setCalendarId(String _calendarId) {
        calendarId = _calendarId;
    }

    public void setCalendarAddress(String _calendarAddress) {
        calendarAddress = _calendarAddress;
    }

    public void setControlRole(String _controlRole) {
        controlRole = _controlRole;
    }

    public void setChannel(String _channel) {
        channel = _channel;
    }
}