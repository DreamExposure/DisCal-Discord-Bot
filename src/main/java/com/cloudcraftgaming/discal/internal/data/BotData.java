package com.cloudcraftgaming.discal.internal.data;

/**
 * Created by Nova Fox on 1/2/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
@Deprecated
public class BotData {
    private final String guildId;

    private String calendarId;
    private String calendarAddress;
    private String controlRole;
    private String channel;

    /**
     * Creates a new BotData object for the specific Guild.
     * <br>
     *     This will NOT get data from the Db, you must do that manually.
     * @param _guildId The ID of the guild this data is for.
     */
    public BotData(String _guildId) {
        guildId = _guildId;

        calendarId = "primary";
        calendarAddress = "primary";
        controlRole = "everyone";
        channel = "all";
    }

    //Getters
    /**
     * Gets the ID of the guild the data is for.
     * @return The ID of the guild the data is for.
     */
    public String getGuildId() {
        return guildId;
    }

    /**
     * Gets the ID of the calendar this guild owns.
     * @return The ID of the calendar this guild owns.
     */
    public String getCalendarId() {
        return calendarId;
    }

    /**
     * Gets the Address of the calendar this guild owns.
     * @return The Address of the calendar this guild owns.
     */
    public String getCalendarAddress() {
        return calendarAddress;
    }

    /**
     * Gets the ID of the role needed to control the bot in this guild.
     * @return The ID of the role needed to control the bot in this guild.
     */
    public String getControlRole() {
        return controlRole;
    }

    /**
     * Gets the ID of the channel the bot can respond in within this guild.
     * @return The ID of the channel the bot can respond in within this guild.
     */
    public String getChannel() {
        return channel;
    }

    //Setters
    /**
     * Sets the ID of the calendar this guild owns.
     * @param _calendarId The ID of the calendar this guild owns.
     */
    public void setCalendarId(String _calendarId) {
        calendarId = _calendarId;
    }

    /**
     * Sets the address of the calendar this guild owns.
     * @param _calendarAddress The address of the calendar this guild owns.
     */
    public void setCalendarAddress(String _calendarAddress) {
        calendarAddress = _calendarAddress;
    }

    /**
     * Sets the ID of the role needed to control the bot in this guild.
     * @param _controlRole The ID of the role needed to control the bot in this guild.
     */
    public void setControlRole(String _controlRole) {
        controlRole = _controlRole;
    }

    /**
     * Sets the ID of the channel DisCal can respond in within this guild.
     * @param _channel The ID of the channel DisCal can respond in within this guild.
     */
    public void setChannel(String _channel) {
        channel = _channel;
    }
}