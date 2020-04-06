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
	private final boolean edited;
	private final Message creatorMessage;
	private final Calendar calendar;

	public CalendarCreatorResponse(boolean successful, Calendar calendar, Message message,
								   boolean edited) {
		this.successful = successful;
		this.calendar = calendar;
		this.creatorMessage = message;
		this.edited = edited;
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
}