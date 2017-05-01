package com.cloudcraftgaming.discal.internal.calendar.calendar;

import com.google.api.services.calendar.model.Calendar;
import sx.blah.discord.handle.obj.IMessage;

/**
 * Created by Nova Fox on 1/4/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class PreCalendar {
    private final long guildId;

    private String summary;
    private String description;
    private String timezone;

    private boolean editing;
    private String calendarId;

    private IMessage creatorMessage;

    /**
     * Creates a new PreCalendar for the Guild.
     * @param _guildId The ID of the guild.
     * @param _summary The summary/name of the calendar.
     */
    PreCalendar(long _guildId, String _summary) {
        guildId = _guildId;
        summary = _summary;

        editing = false;
    }

    PreCalendar(long _guildId, Calendar calendar) {
        guildId = _guildId;
        summary = calendar.getSummary();

        if (calendar.getDescription() != null) {
            description = calendar.getDescription();
        }
        if (calendar.getTimeZone() != null) {
            timezone = calendar.getTimeZone();
        }

        editing = false;
    }

    //Getters

    /**
     * Gets the ID of the guild this PreCalendar belongs to.
     * @return The ID of the guild this PreCalendar belongs to.
     */
    public long getGuildId() {
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

    public boolean isEditing() {
        return editing;
    }

    public String getCalendarId() {
        return calendarId;
    }

    public IMessage getCreatorMessage() {
        return creatorMessage;
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

    public void setEditing(boolean _editing) {
        editing = _editing;
    }

    public void setCalendarId(String _id) {
        calendarId = _id;
    }

    public void setCreatorMessage(IMessage _message) {
        creatorMessage = _message;
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