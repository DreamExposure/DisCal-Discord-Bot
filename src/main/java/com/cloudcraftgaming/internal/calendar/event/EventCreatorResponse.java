package com.cloudcraftgaming.internal.calendar.event;

import com.google.api.services.calendar.model.Event;

/**
 * Created by Nova Fox on 1/4/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class EventCreatorResponse {
    private final Boolean successful;

    private Event event;

    public EventCreatorResponse(Boolean _successful) {
        successful = _successful;
    }

    public EventCreatorResponse(Boolean _successful, Event _event) {
        successful = _successful;
        event = _event;
    }

    //Getters
    public Boolean isSuccessful() {
        return successful;
    }

    public Event getEvent() {
        return event;
    }

    //Setters
    public void setEvent(Event _event) {
        event = _event;
    }
}