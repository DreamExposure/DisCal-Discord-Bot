package com.cloudcraftgaming.internal.data;

import sx.blah.discord.api.IDiscordClient;

/**
 * Created by Nova Fox on 1/2/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class BotData {
    private final String guildId;

    private String calendarId;

    public BotData(String _guildId) {
        guildId = _guildId;

        calendarId = "primary";
    }

    public BotData(String _guildId, IDiscordClient client) {
        guildId = _guildId;

        calendarId = "primary";
    }

    //Getters
    public String getGuildId() {
        return guildId;
    }

    public String getCalendarId() {
        return calendarId;
    }

    //Setters
    public void setCalendarId(String _calendarId) {
        calendarId = _calendarId;
    }
}