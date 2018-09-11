package org.dreamexposure.discal.core.object.calendar;

import com.google.api.services.calendar.model.Calendar;
import discord4j.core.object.entity.Message;

/**
 * Created by Nova Fox on 11/10/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
public class CalendarCreatorResponse {
	private final boolean successful;

	private boolean edited;
	private Message creatorMessage;
	private Calendar calendar;

	/**
	 * Creates a new response.
	 *
	 * @param _successful Whether or not the creation was successful.
	 */
	public CalendarCreatorResponse(boolean _successful) {
		successful = _successful;
	}

	/**
	 * Creates a new response.
	 *
	 * @param _successful Whether or not the creation was successful.
	 * @param _calendar   The calendar created.
	 */
	public CalendarCreatorResponse(boolean _successful, Calendar _calendar) {
		successful = _successful;
		calendar = _calendar;
	}

	//Getters

	/**
	 * Whether or not the creation was successful.
	 *
	 * @return <code>true</code> if successful, else <code>false</code>.
	 */
	public boolean isSuccessful() {
		return successful;
	}

	/**
	 * The calendar involved. Can be null.
	 *
	 * @return The calendar involved, may be null.
	 */
	public Calendar getCalendar() {
		return calendar;
	}

	public boolean isEdited() {
		return edited;
	}

	public Message getCreatorMessage() {
		return creatorMessage;
	}

	//Setters

	/**
	 * Sets the calendar involved.
	 *
	 * @param _calendar The calendar involved.
	 */
	public void setCalendar(Calendar _calendar) {
		calendar = _calendar;
	}

	public void setEdited(boolean _edit) {
		edited = _edit;
	}

	public void setCreatorMessage(Message msg) {
		creatorMessage = msg;
	}
}