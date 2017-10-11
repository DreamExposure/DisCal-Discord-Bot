package com.cloudcraftgaming.discal.object.calendar;

import com.google.api.services.calendar.model.Calendar;
import sx.blah.discord.handle.obj.IMessage;

/**
 * Created by Nova Fox on 1/4/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class CalendarCreatorResponse {
	private final Boolean successful;

	private boolean edited;
	private IMessage creatorMessage;
	private Calendar calendar;

	/**
	 * Creates a new response.
	 *
	 * @param _successful Whether or not the creation was successful.
	 */
	public CalendarCreatorResponse(Boolean _successful) {
		successful = _successful;
	}

	/**
	 * Creates a new response.
	 *
	 * @param _successful Whether or not the creation was successful.
	 * @param _calendar   The calendar created.
	 */
	public CalendarCreatorResponse(Boolean _successful, Calendar _calendar) {
		successful = _successful;
		calendar = _calendar;
	}

	//Getters

	/**
	 * Whether or not the creation was successful.
	 *
	 * @return <code>true</code> if successful, else <code>false</code>.
	 */
	public Boolean isSuccessful() {
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

	public IMessage getCreatorMessage() {
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

	public void setCreatorMessage(IMessage msg) {
		creatorMessage = msg;
	}
}