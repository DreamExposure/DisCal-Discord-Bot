package com.cloudcraftgaming.discal.api.object.event;

import com.cloudcraftgaming.discal.api.calendar.CalendarAuth;
import com.cloudcraftgaming.discal.api.database.DatabaseManager;
import com.cloudcraftgaming.discal.api.enums.event.EventColor;
import com.cloudcraftgaming.discal.api.object.GuildSettings;
import com.cloudcraftgaming.discal.api.object.calendar.CalendarData;
import com.cloudcraftgaming.discal.api.utils.ExceptionHandler;
import com.cloudcraftgaming.discal.api.utils.TimeUtils;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import sx.blah.discord.handle.obj.IMessage;

/**
 * Created by Nova Fox on 11/10/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
public class PreEvent {
	private final long guildId;
	private final String eventId;

	private String summary;
	private String description;
	private EventDateTime startDateTime;
	private EventDateTime endDateTime;

	private EventDateTime viewableStartDate;
	private EventDateTime viewableEndDate;

	private String timeZone;

	private EventColor color;

	private String location;

	private boolean recur;
	private Recurrence recurrence;

	private EventData eventData;

	private boolean editing;

	private IMessage creatorMessage;

	private long lastEdit;


	/**
	 * Creates a new PreEvent for the specified Guild.
	 *
	 * @param _guildId The ID of the guild.
	 */
	public PreEvent(long _guildId) {
		guildId = _guildId;
		eventId = "N/a";

		timeZone = "Unknown";

		color = EventColor.NONE;

		recur = false;
		recurrence = new Recurrence();

		eventData = new EventData(guildId);

		editing = false;
		lastEdit = System.currentTimeMillis();
	}

	public PreEvent(long _guildId, Event e) {
		guildId = _guildId;
		eventId = e.getId();

		color = EventColor.fromNameOrHexOrID(e.getColorId());

		recurrence = new Recurrence();

		if (e.getRecurrence() != null && e.getRecurrence().size() > 0) {
			recur = true;
			recurrence.fromRRule(e.getRecurrence().get(0));
		}

		if (e.getSummary() != null) {
			summary = e.getSummary();
		}
		if (e.getDescription() != null) {
			description = e.getDescription();
		}
		if (e.getLocation() != null) {
			location = e.getLocation();
		}

		startDateTime = e.getStart();
		endDateTime = e.getEnd();

		//Here is where I need to fix the display times
		GuildSettings settings = DatabaseManager.getManager().getSettings(guildId);
		//TODO: Support multiple calendars
		CalendarData data = DatabaseManager.getManager().getMainCalendar(guildId);

		Calendar cal = null;
		try {
			if (settings.useExternalCalendar()) {
				cal = CalendarAuth.getCalendarService(settings).calendars().get(data.getCalendarAddress()).execute();
			} else {
				cal = CalendarAuth.getCalendarService().calendars().get(data.getCalendarAddress()).execute();
			}
		} catch (Exception ex) {
			ExceptionHandler.sendException(null, "Failed to get proper date time for event!", ex, this.getClass());
		}

		if (cal != null) {

			//Check if either DateTime or just Date...
			if (e.getStart().getDateTime() != null) {
				//DateTime
				viewableStartDate = new EventDateTime().setDateTime(new DateTime(TimeUtils.applyTimeZoneOffset(e.getStart().getDateTime().getValue(), cal.getTimeZone())));
				viewableEndDate = new EventDateTime().setDateTime(new DateTime(TimeUtils.applyTimeZoneOffset(e.getEnd().getDateTime().getValue(), cal.getTimeZone())));
			} else {
				//Just Date
				viewableStartDate = new EventDateTime().setDate(new DateTime(TimeUtils.applyTimeZoneOffset(e.getStart().getDate().getValue(), cal.getTimeZone())));
				viewableEndDate = new EventDateTime().setDate(new DateTime(TimeUtils.applyTimeZoneOffset(e.getEnd().getDate().getValue(), cal.getTimeZone())));
			}
		} else {
			//Almost definitely not correct, but we need something displayed here.
			viewableStartDate = e.getStart();
			viewableEndDate = e.getEnd();
		}

		eventData = DatabaseManager.getManager().getEventData(guildId, e.getId());

		editing = false;
		lastEdit = System.currentTimeMillis();
	}

	//Getters

	/**
	 * Gets the ID of the guild who owns this PreEvent.
	 *
	 * @return The ID of the guild who owns this PreEvent.
	 */
	public long getGuildId() {
		return guildId;
	}

	public String getEventId() {
		return eventId;
	}

	/**
	 * Gets the event summary.
	 *
	 * @return The event summary.
	 */
	public String getSummary() {
		return summary;
	}

	/**
	 * Gets the description.
	 *
	 * @return The description.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Gets the start date and time.
	 *
	 * @return The start date and time.
	 */
	public EventDateTime getStartDateTime() {
		return startDateTime;
	}

	/**
	 * Gets the end date and time.
	 *
	 * @return The end date and time.
	 */
	public EventDateTime getEndDateTime() {
		return endDateTime;
	}

	/**
	 * Gets the viewable start date and time.
	 *
	 * @return The viewable start date and time.
	 */
	public EventDateTime getViewableStartDate() {
		return viewableStartDate;
	}

	/**
	 * Gets the viewable end date and time.
	 *
	 * @return The viewable end date and time.
	 */
	public EventDateTime getViewableEndDate() {
		return viewableEndDate;
	}

	/**
	 * Gets the timezone of the event.
	 *
	 * @return The timezone of the event.
	 */
	public String getTimeZone() {
		return timeZone;
	}

	/**
	 * Gets the valid Google Calendar color for this event.
	 *
	 * @return The valid color for this event.
	 */
	public EventColor getColor() {
		return color;
	}

	public String getLocation() {
		return location;
	}

	/**
	 * Gets whether or not the vent should recur.
	 *
	 * @return <code>true</code> if recurring, otherwise <code>false</code>.
	 */
	public boolean shouldRecur() {
		return recur;
	}

	/**
	 * Gets the recurrence rules and info for the event.
	 *
	 * @return The recurrence rules and info for the event.
	 */
	public Recurrence getRecurrence() {
		return recurrence;
	}

	public EventData getEventData() {
		return eventData;
	}

	public boolean isEditing() {
		return editing;
	}

	public IMessage getCreatorMessage() {
		return creatorMessage;
	}

	public long getLastEdit() {
		return lastEdit;
	}

	//Setters

	/**
	 * Sets the summary of the event.
	 *
	 * @param _summary The summary of the vent.
	 */
	public void setSummary(String _summary) {
		summary = _summary;
	}

	/**
	 * Sets the description of the event.
	 *
	 * @param _description The description of the event.
	 */
	public void setDescription(String _description) {
		description = _description;
	}

	/**
	 * Sets the start date and time of the event.
	 *
	 * @param _startDateTime The start date and time of the event.
	 */
	public void setStartDateTime(EventDateTime _startDateTime) {
		startDateTime = _startDateTime;
	}

	/**
	 * Sets the end date and time of the event.
	 *
	 * @param _endDateTime The end date and time of the event.
	 */
	public void setEndDateTime(EventDateTime _endDateTime) {
		endDateTime = _endDateTime;
	}

	/**
	 * Sets the viewable start date and time of the event.
	 *
	 * @param _viewableStart The viewable start date and time of the event.
	 */
	public void setViewableStartDate(EventDateTime _viewableStart) {
		viewableStartDate = _viewableStart;
	}

	/**
	 * Sets the viewable end date and time of the event.
	 *
	 * @param _viewableEnd The viewable end date and time of the event.
	 */
	public void setViewableEndDate(EventDateTime _viewableEnd) {
		viewableEndDate = _viewableEnd;
	}

	/**
	 * Sets the timezone of the event.
	 *
	 * @param _timeZone The timezone of the event.
	 */
	public void setTimeZone(String _timeZone) {
		timeZone = _timeZone;
	}

	/**
	 * Sets the valid Google Calendar color for this event.
	 *
	 * @param _color The valid color for this event.
	 */
	public void setColor(EventColor _color) {
		color = _color;
	}

	public void setLocation(String _location) {
		location = _location;
	}

	/**
	 * Sets whether or not the event should recur.
	 *
	 * @param _recur Whether or not the event should recur.
	 */
	public void setShouldRecur(boolean _recur) {
		recur = _recur;
	}

	public void setEventData(EventData _data) {
		eventData = _data;
	}

	public void setEditing(boolean _editing) {
		editing = _editing;
	}

	public void setCreatorMessage(IMessage _creatorMessage) {
		creatorMessage = _creatorMessage;
	}

	public void setLastEdit(long _lastEdit) {
		lastEdit = _lastEdit;
	}

	//Booleans/Checkers

	/**
	 * Whether or not the event has all required values to be created.
	 *
	 * @return <code>true</code> if required values set, otherwise <code>false</code>.
	 */
	public Boolean hasRequiredValues() {
		return startDateTime != null && endDateTime != null;
	}
}