package org.dreamexposure.discal.core.object.event;

import com.google.api.services.calendar.model.Event;
import discord4j.core.object.entity.Message;

/**
 * Created by Nova Fox on 11/10/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
public class EventCreatorResponse {
	private final boolean successful;

	private Message creatorMessage;

	private Event event;
	private boolean edited;

	/**
	 * Creates a new Response.
	 *
	 * @param _successful Whether or not the Creator was successful.
	 */
	public EventCreatorResponse(boolean _successful) {
		successful = _successful;
	}

	/**
	 * Creates a new Response.
	 *
	 * @param _successful Whether or not the Creator was successful.
	 * @param _event      The Event that was created.
	 */
	public EventCreatorResponse(boolean _successful, Event _event) {
		successful = _successful;
		event = _event;
		edited = false;
	}

	//Getters

	/**
	 * Whether or not the creator was successful.
	 *
	 * @return <code>true</code> if successful, else <code>false</code>.
	 */
	public boolean isSuccessful() {
		return successful;
	}

	public Message getCreatorMessage() {
		return creatorMessage;
	}

	public boolean isEdited() {
		return edited;
	}

	/**
	 * Gets the event that was created.
	 *
	 * @return The event that was created.
	 */
	public Event getEvent() {
		return event;
	}

	//Setters
	public void setCreatorMessage(Message _creatorMessage) {
		creatorMessage = _creatorMessage;
	}

	/**
	 * Sets the event that was created.
	 *
	 * @param _event The event that was created.
	 */
	public void setEvent(Event _event) {
		event = _event;
	}

	public void setEdited(boolean _edited) {
		edited = _edited;
	}
}