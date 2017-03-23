package com.cloudcraftgaming.discal.internal.calendar.event;

import com.cloudcraftgaming.discal.utils.EventColor;
import com.google.api.services.calendar.model.EventDateTime;

/**
 * Created by Nova Fox on 1/3/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class PreEvent {
    private final String guildId;

    private String summary;
    private String description;
    private EventDateTime startDateTime;
    private EventDateTime endDateTime;

    private EventDateTime viewableStartDate;
    private EventDateTime viewableEndDate;

    private String timeZone;

    private EventColor color;

    private boolean recur;
    private Recurrence recurrence;


    /**
     * Creates a new PreEvent for the specified Guild.
     * @param _guildId The ID of the guild.
     */
    PreEvent(String _guildId) {
        guildId = _guildId;

        timeZone = "Unknown";

        color = EventColor.BLUE;

        recur = false;
        recurrence = new Recurrence();
    }

    //Getters
    /**
     *  Gets the ID of the guild who owns this PreEvent.
     * @return The ID of the guild who owns this PreEvent.
     */
    public String getGuildId() {
        return guildId;
    }

    /**
     * Gets the event summary.
     * @return The event summary.
     */
    String getSummary() {
        return summary;
    }

    /**
     * Gets the description.
     * @return The description.
     */
    String getDescription() {
        return description;
    }

    /**
     * Gets the start date and time.
     * @return The start date and time.
     */
    public EventDateTime getStartDateTime() {
        return startDateTime;
    }

    /**
     * Gets the end date and time.
     * @return The end date and time.
     */
    public EventDateTime getEndDateTime() {
        return endDateTime;
    }

    /**
     * Gets the viewable start date and time.
     * @return The viewable start date and time.
     */
    EventDateTime getViewableStartDate() {
        return viewableStartDate;
    }

    /**
     * Gets the viewable end date and time.
     * @return The viewable end date and time.
     */
    EventDateTime getViewableEndDate() {
        return viewableEndDate;
    }

    /**
     * Gets the timezone of the event.
     * @return The timezone of the event.
     */
    public String getTimeZone() {
        return timeZone;
    }

    /**
     * Gets the valid Google Calendar color for this event.
     * @return The valid color for this event.
     */
    EventColor getColor() {
        return color;
    }

    /**
     * Gets whether or not the vent should recur.
     * @return <code>true</code> if recurring, otherwise <code>false</code>.
     */
    public boolean shouldRecur() {
        return recur;
    }

    /**
     * Gets the recurrence rules and info for the event.
     * @return The recurrence rules and info for the event.
     */
    private Recurrence getRecurrence() {
        return recurrence;
    }

    //Setters
    /**
     * Sets the summary of the event.
     * @param _summary The summary of the vent.
     */
    public void setSummary(String _summary) {
        summary = _summary;
    }

    /**
     * Sets the description of the event.
     * @param _description The description of the event.
     */
    public void setDescription(String _description) {
        description = _description;
    }

    /**
     * Sets the start date and time of the event.
     * @param _startDateTime The start date and time of the event.
     */
    public void setStartDateTime(EventDateTime _startDateTime) {
        startDateTime = _startDateTime;
    }

    /**
     * Sets the end date and time of the event.
     * @param _endDateTime The end date and time of the event.
     */
    public void setEndDateTime(EventDateTime _endDateTime) {
        endDateTime = _endDateTime;
    }

    /**
     * Sets the viewable start date and time of the event.
     * @param _viewableStart The viewable start date and time of the event.
     */
    public void setViewableStartDate(EventDateTime _viewableStart) {
        viewableStartDate = _viewableStart;
    }

    /**
     * Sets the viewable end date and time of the event.
     * @param _viewableEnd The viewable end date and time of the event.
     */
    public void setViewableEndDate(EventDateTime _viewableEnd) {
        viewableEndDate = _viewableEnd;
    }

    /**
     * Sets the timezone of the event.
     * @param _timeZone The timezone of the event.
     */
    void setTimeZone(String _timeZone) {
        timeZone = _timeZone;
    }

    /**
     * Sets the valid Google Calendar color for this event.
     * @param _color The valid color for this event.
     */
    public void setColor(EventColor _color) {
        color = _color;
    }

    /**
     * Sets whether or not the event should recur.
     * @param _recur Whether or not the event should recur.
     */
    void setShouldRecur(boolean _recur) {
        recur = _recur;
    }

    //Booleans/Checkers
    /**
     * Whether or not the event has all required values to be created.
     * @return <code>true</code> if required values set, otherwise <code>false</code>.
     */
    public Boolean hasRequiredValues() {
        return startDateTime != null && endDateTime != null;
    }
}
