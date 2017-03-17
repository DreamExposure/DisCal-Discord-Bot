package com.cloudcraftgaming.discal.internal.calendar.event;

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

    private boolean recur;
    private Recurrence recurrence;


    PreEvent(String _guildId) {
        guildId = _guildId;

        timeZone = "Unknown";
        recur = false;
        recurrence = new Recurrence();
    }

    //Getters
    public String getGuildId() {
        return guildId;
    }

    String getSummary() {
        return summary;
    }

    String getDescription() {
        return description;
    }

    EventDateTime getStartDateTime() {
        return startDateTime;
    }

    EventDateTime getEndDateTime() {
        return endDateTime;
    }

    EventDateTime getViewableStartDate() {
        return viewableStartDate;
    }

    EventDateTime getViewableEndDate() {
        return viewableEndDate;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public boolean shouldRecur() {
        return recur;
    }

    private Recurrence getRecurrence() {
        return recurrence;
    }

    //Setters
    public void setSummary(String _summary) {
        summary = _summary;
    }

    public void setDescription(String _description) {
        description = _description;
    }

    public void setStartDateTime(EventDateTime _startDateTime) {
        startDateTime = _startDateTime;
    }

    public void setEndDateTime(EventDateTime _endDateTime) {
        endDateTime = _endDateTime;
    }

    public void setViewableStartDate(EventDateTime _viewableStart) {
        viewableStartDate = _viewableStart;
    }

    public void setViewableEndDate(EventDateTime _viewableEnd) {
        viewableEndDate = _viewableEnd;
    }

    void setTimeZone(String _timeZone) {
        timeZone = _timeZone;
    }

    void setShouldRecur(boolean _recur) {
        recur = _recur;
    }

    //Booleans/Checkers
    public Boolean hasRequiredValues() {
        return startDateTime != null && endDateTime != null;
    }
}
