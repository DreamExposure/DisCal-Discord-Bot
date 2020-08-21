package org.dreamexposure.discal.core.object.calendar;

import com.google.api.services.calendar.model.Calendar;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;

/**
 * Created by Nova Fox on 11/10/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
public class PreCalendar {
    private final Snowflake guildId;

    private String summary;
    private String description;
    private String timezone;

    private boolean editing;
    private String calendarId;
    private CalendarData calendarData;

    private Message creatorMessage;

    private long lastEdit;

    /**
     * Creates a new PreCalendar for the Guild.
     *
     * @param _guildId The ID of the guild.
     * @param _summary The summary/name of the calendar.
     */
    public PreCalendar(final Snowflake _guildId, final String _summary) {
        this.guildId = _guildId;
        this.summary = _summary;

        this.editing = false;

        this.lastEdit = System.currentTimeMillis();
    }

    public PreCalendar(final Snowflake _guildId, final Calendar calendar) {
        this.guildId = _guildId;
        this.summary = calendar.getSummary();

        if (calendar.getDescription() != null)
            this.description = calendar.getDescription();

        if (calendar.getTimeZone() != null)
            this.timezone = calendar.getTimeZone();


        this.editing = false;

        this.lastEdit = System.currentTimeMillis();
    }

    //Getters

    /**
     * Gets the ID of the guild this PreCalendar belongs to.
     *
     * @return The ID of the guild this PreCalendar belongs to.
     */
    public Snowflake getGuildId() {
        return this.guildId;
    }

    /**
     * Gets the summary or name of the calendar.
     *
     * @return The summary or name of the calendar.
     */
    public String getSummary() {
        return this.summary;
    }

    /**
     * Gets the description of the calendar.
     *
     * @return The description of the calendar.
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Gets the Timezone of the calendar.
     *
     * @return The Timezone of the calendar.
     */
    public String getTimezone() {
        return this.timezone;
    }

    public boolean isEditing() {
        return this.editing;
    }

    public String getCalendarId() {
        return this.calendarId;
    }

    public CalendarData getCalendarData() {
        return this.calendarData;
    }

    public Message getCreatorMessage() {
        return this.creatorMessage;
    }

    public long getLastEdit() {
        return this.lastEdit;
    }

    //Setters

    /**
     * Sets the summary/name of the calendar.
     *
     * @param _summary The summary/name of the calendar.
     */
    public void setSummary(final String _summary) {
        this.summary = _summary;
    }

    /**
     * Sets the description of the calendar.
     *
     * @param _description The description of the calendar.
     */
    public void setDescription(final String _description) {
        this.description = _description;
    }

    /**
     * Sets the timezone of the calendar.
     *
     * @param _timezone The timezone of the calendar.
     */
    public void setTimezone(final String _timezone) {
        this.timezone = _timezone;
    }

    public void setEditing(final boolean _editing) {
        this.editing = _editing;
    }

    public void setCalendarId(final String _id) {
        this.calendarId = _id;
    }

    public void setCalendarData(final CalendarData calendarData) {
        this.calendarData = calendarData;
    }

    public void setCreatorMessage(final Message _message) {
        this.creatorMessage = _message;
    }

    public void setLastEdit(final long _lastEdit) {
        this.lastEdit = _lastEdit;
    }

    //Booleans/Checkers

    /**
     * Checks if the calendar has all required data in order to be created.
     *
     * @return {@code true} if required data set, otherwise {@code false}.
     */
    public boolean hasRequiredValues() {
        return this.summary != null && this.timezone != null;
    }
}