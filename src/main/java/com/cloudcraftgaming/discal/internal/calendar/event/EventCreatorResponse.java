package com.cloudcraftgaming.discal.internal.calendar.event;

import com.google.api.services.calendar.model.Event;

/**
 * Created by Nova Fox on 1/4/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class EventCreatorResponse {
    private final Boolean successful;

    private Event event;

    /**
     * Creates a new Response.
     * @param _successful Whether or not the Creator was successful.
     */
    EventCreatorResponse(Boolean _successful) {
        successful = _successful;
    }

    /**
     * Creates a new Response.
     * @param _successful Whether or not the Creator was successful.
     * @param _event The Event that was created.
     */
    EventCreatorResponse(Boolean _successful, Event _event) {
        successful = _successful;
        event = _event;
    }

    //Getters
    /**
     * Whether or not the creator was successful.
     * @return <code>true</code> if successful, else <code>false</code>.
     */
    public Boolean isSuccessful() {
        return successful;
    }

    /**
     * Gets the event that was created.
     * @return The event that was created.
     */
    public Event getEvent() {
        return event;
    }

    //Setters
    /**
     * Sets the event that was created.
     * @param _event The event that was created.
     */
    public void setEvent(Event _event) {
        event = _event;
    }
}