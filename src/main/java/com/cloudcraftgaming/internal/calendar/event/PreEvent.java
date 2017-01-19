package com.cloudcraftgaming.internal.calendar.event;

import com.google.api.services.calendar.model.EventDateTime;

/**
 * Created by Nova Fox on 1/3/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class PreEvent {
    private final String guildId;
    private final String eventName;

    private String summery;
    private String description;
    private EventDateTime startDateTime;
    private EventDateTime endDateTime;

    private String timeZone;


    PreEvent(String _guildId, String _eventName) {
        guildId = _guildId;
        eventName = _eventName;

        timeZone = "Unknown";
    }

    //Getters
    public String getGuildId() {
        return guildId;
    }

    public String getEventName() {
        return eventName;
    }

    String getSummery() {
        return summery;
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

    String getTimeZone() {
        return timeZone;
    }

    //Setters
    public void setSummery(String _summery) {
        summery = _summery;
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

    void setTimeZone(String _timeZone) {
        timeZone = _timeZone;
    }

    //Booleans/Checkers
    public Boolean hasRequiredValues() {
        return startDateTime != null && endDateTime != null;
    }
}
