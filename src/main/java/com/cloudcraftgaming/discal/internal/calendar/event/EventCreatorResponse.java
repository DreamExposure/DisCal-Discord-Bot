package com.cloudcraftgaming.discal.internal.calendar.event;

import com.google.api.services.calendar.model.Event;
import sx.blah.discord.handle.obj.IMessage;

/**
 * Created by Nova Fox on 1/4/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class EventCreatorResponse {
    private final Boolean successful;

    private IMessage creatorMessage;

    private Event event;
    private boolean edited;

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
        edited = false;
    }

    //Getters
    /**
     * Whether or not the creator was successful.
     * @return <code>true</code> if successful, else <code>false</code>.
     */
    public Boolean isSuccessful() {
        return successful;
    }

    public IMessage getCreatorMessage() {
    	return creatorMessage;
	}

	public boolean isEdited() {
    	return edited;
	}

    /**
     * Gets the event that was created.
     * @return The event that was created.
     */
    public Event getEvent() {
        return event;
    }

    //Setters
	public void setCreatorMessage(IMessage _creatorMessage) {
    	creatorMessage = _creatorMessage;
	}

    /**
     * Sets the event that was created.
     * @param _event The event that was created.
     */
    public void setEvent(Event _event) {
        event = _event;
    }

    public void setEdited(boolean _edited) {
    	edited = _edited;
	}
}