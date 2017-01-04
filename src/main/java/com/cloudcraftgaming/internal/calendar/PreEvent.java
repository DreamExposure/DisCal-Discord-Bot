package com.cloudcraftgaming.internal.calendar;

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
    private EventDateTime dateTime;


    public PreEvent(String _guildId, String _eventName) {
        guildId = _guildId;
        eventName = _eventName;
    }

    //Getters
    public String getGuildId() {
        return guildId;
    }

    public String getEventName() {
        return eventName;
    }

    public String getSummery() {
        return summery;
    }

    public String getDescription() {
        return description;
    }

    public EventDateTime getDateTime() {
        return dateTime;
    }

    //Setters
    public void setSummery(String _summery) {
        summery = _summery;
    }

    public void setDescription(String _description) {
        description = _description;
    }

    public void setDateTime(EventDateTime _dateTime) {
        dateTime = _dateTime;
    }
}
