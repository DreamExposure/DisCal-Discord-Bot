package com.cloudcraftgaming.discal.internal.calendar.event;

import com.cloudcraftgaming.discal.database.DatabaseManager;
import com.cloudcraftgaming.discal.internal.calendar.CalendarAuth;
import com.cloudcraftgaming.discal.internal.data.CalendarData;
import com.cloudcraftgaming.discal.internal.data.EventData;
import com.cloudcraftgaming.discal.internal.data.GuildSettings;
import com.cloudcraftgaming.discal.utils.EventColor;
import com.cloudcraftgaming.discal.utils.ExceptionHandler;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import sx.blah.discord.handle.obj.IMessage;

import java.time.ZoneId;
import java.util.TimeZone;

/**
 * Created by Nova Fox on 1/3/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
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


    /**
     * Creates a new PreEvent for the specified Guild.
     * @param _guildId The ID of the guild.
     */
    PreEvent(long _guildId) {
        guildId = _guildId;
        eventId = "N/a";

        timeZone = "Unknown";

        color = EventColor.NONE;

        recur = false;
        recurrence = new Recurrence();

        eventData = new EventData(guildId);

        editing = false;
    }

    PreEvent(long _guildId, Event e) {
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
				DateTime vsd = new DateTime(e.getStart().getDateTime().getValue(), TimeZone.getTimeZone(ZoneId.of(cal.getTimeZone())).getOffset(e.getStart().getDateTime().getValue()));
				DateTime ved = new DateTime(e.getEnd().getDateTime().getValue(), TimeZone.getTimeZone(ZoneId.of(cal.getTimeZone())).getOffset(e.getEnd().getDateTime().getValue()));

				//Think that's it, just make the date stuffs... somehow...
				viewableStartDate = new EventDateTime().setDateTime(vsd);
				viewableEndDate = new EventDateTime().setDateTime(ved);
			} else {
				//Just Date
				DateTime vsd = new DateTime(e.getStart().getDate().getValue(), TimeZone.getTimeZone(ZoneId.of(cal.getTimeZone())).getOffset(e.getStart().getDate().getValue()));
				DateTime ved = new DateTime(e.getEnd().getDate().getValue(), TimeZone.getTimeZone(ZoneId.of(cal.getTimeZone())).getOffset(e.getEnd().getDate().getValue()));

				//Think that's it, just make the date stuffs... somehow...
				viewableStartDate = new EventDateTime().setDate(vsd);
				viewableEndDate = new EventDateTime().setDate(ved);
			}
		} else {
			//Almost definitely not correct, but we need something displayed here.
			viewableStartDate = e.getStart();
			viewableEndDate = e.getEnd();
		}

        eventData = DatabaseManager.getManager().getEventData(guildId, e.getId());

        editing = false;
    }

    //Getters
    /**
     *  Gets the ID of the guild who owns this PreEvent.
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
     * @return The event summary.
     */
    String getSummary() {
        return summary;
    }

    /**
     * Gets the description.
     * @return The description.
     */
    String getDescription() {
        return description;
    }

    /**
     * Gets the start date and time.
     * @return The start date and time.
     */
    public EventDateTime getStartDateTime() {
        return startDateTime;
    }

    /**
     * Gets the end date and time.
     * @return The end date and time.
     */
    public EventDateTime getEndDateTime() {
        return endDateTime;
    }

    /**
     * Gets the viewable start date and time.
     * @return The viewable start date and time.
     */
    EventDateTime getViewableStartDate() {
        return viewableStartDate;
    }

    /**
     * Gets the viewable end date and time.
     * @return The viewable end date and time.
     */
    EventDateTime getViewableEndDate() {
        return viewableEndDate;
    }

    /**
     * Gets the timezone of the event.
     * @return The timezone of the event.
     */
    public String getTimeZone() {
        return timeZone;
    }

    /**
     * Gets the valid Google Calendar color for this event.
     * @return The valid color for this event.
     */
    EventColor getColor() {
        return color;
    }

	public String getLocation() {
		return location;
	}

    /**
     * Gets whether or not the vent should recur.
     * @return <code>true</code> if recurring, otherwise <code>false</code>.
     */
    public boolean shouldRecur() {
        return recur;
    }

    /**
     * Gets the recurrence rules and info for the event.
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

    //Setters
    /**
     * Sets the summary of the event.
     * @param _summary The summary of the vent.
     */
    public void setSummary(String _summary) {
        summary = _summary;
    }

    /**
     * Sets the description of the event.
     * @param _description The description of the event.
     */
    public void setDescription(String _description) {
        description = _description;
    }

    /**
     * Sets the start date and time of the event.
     * @param _startDateTime The start date and time of the event.
     */
    public void setStartDateTime(EventDateTime _startDateTime) {
        startDateTime = _startDateTime;
    }

    /**
     * Sets the end date and time of the event.
     * @param _endDateTime The end date and time of the event.
     */
    public void setEndDateTime(EventDateTime _endDateTime) {
        endDateTime = _endDateTime;
    }

    /**
     * Sets the viewable start date and time of the event.
     * @param _viewableStart The viewable start date and time of the event.
     */
    public void setViewableStartDate(EventDateTime _viewableStart) {
        viewableStartDate = _viewableStart;
    }

    /**
     * Sets the viewable end date and time of the event.
     * @param _viewableEnd The viewable end date and time of the event.
     */
    public void setViewableEndDate(EventDateTime _viewableEnd) {
        viewableEndDate = _viewableEnd;
    }

    /**
     * Sets the timezone of the event.
     * @param _timeZone The timezone of the event.
     */
    void setTimeZone(String _timeZone) {
        timeZone = _timeZone;
    }

    /**
     * Sets the valid Google Calendar color for this event.
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

    //Booleans/Checkers
    /**
     * Whether or not the event has all required values to be created.
     * @return <code>true</code> if required values set, otherwise <code>false</code>.
     */
    public Boolean hasRequiredValues() {
        return startDateTime != null && endDateTime != null;
    }
}