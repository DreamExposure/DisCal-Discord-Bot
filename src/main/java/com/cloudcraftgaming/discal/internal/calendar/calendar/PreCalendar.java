package com.cloudcraftgaming.discal.internal.calendar.calendar;

/**
 * Created by Nova Fox on 1/4/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class PreCalendar {
    private final String guildId;

    private String summary;
    private String description;
    private String timezone;

    /**
     * Creates a new PreCalendar for the Guild.
     * @param _guildId The ID of the guild.
     * @param _summary The summary/name of the calendar.
     */
    PreCalendar(String _guildId, String _summary) {
        guildId = _guildId;
        summary = _summary;
    }

    //Getters

    /**
     * Gets the ID of the guild this PreCalendar belongs to.
     * @return The ID of the guild this PreCalendar belongs to.
     */
    public String getGuildId() {
        return guildId;
    }

    /**
     * Gets the summary or name of the calendar.
     * @return The summary or name of the calendar.
     */
    String getSummary() {
        return summary;
    }

    /**
     * Gets the description of the calendar.
     * @return The description of the calendar.
     */
    String getDescription() {
        return description;
    }

    /**
     * Gets the Timezone of the calendar.
     * @return The Timezone of the calendar.
     */
    String getTimezone() {
        return timezone;
    }

    //Setters
    /**
     * Sets the summary/name of the calendar.
     * @param _summary The summary/name of the calendar.
     */
    public void setSummary(String _summary) {
        summary = _summary;
    }

    /**
     * Sets the description of the calendar.
     * @param _description The description of the calendar.
     */
    public void setDescription(String _description) {
        description = _description;
    }

    /**
     * Sets the timezone of the calendar.
     * @param _timezone The timezone of the calendar.
     */
    public void setTimezone(String _timezone) {
        timezone = _timezone;
    }

    //Booleans/Checkers

    /**
     * Checks if the calendar has all required data in order to be created.
     * @return <code>true</code> if required data set, otherwise <code>false</code>.
     */
    Boolean hasRequiredValues() {
        return summary != null && timezone != null;
    }
}