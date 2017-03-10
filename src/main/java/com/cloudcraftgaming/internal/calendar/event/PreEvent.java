package com.cloudcraftgaming.internal.calendar.event;

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


    PreEvent(String _guildId) {
        guildId = _guildId;

        timeZone = "Unknown";
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

    public EventDateTime getViewableStartDate() {
        return viewableStartDate;
    }

    public EventDateTime getViewableEndDate() {
        return viewableEndDate;
    }

    public String getTimeZone() {
        return timeZone;
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

    //Booleans/Checkers
    public Boolean hasRequiredValues() {
        return startDateTime != null && endDateTime != null;
    }
}
