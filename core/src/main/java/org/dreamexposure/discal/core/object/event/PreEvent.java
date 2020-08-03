package org.dreamexposure.discal.core.object.event;

import com.google.api.services.calendar.model.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.enums.event.EventColor;
import org.dreamexposure.discal.core.logger.LogFeed;
import org.dreamexposure.discal.core.logger.object.LogObject;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.calendar.CalendarData;
import org.dreamexposure.discal.core.wrapper.google.CalendarWrapper;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;

/**
 * Created by Nova Fox on 11/10/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
public class PreEvent {
    private final Snowflake guildId;
    private final String eventId;

    private String summary;
    private String description;
    private EventDateTime startDateTime;
    private EventDateTime endDateTime;

    private String timeZone;

    private EventColor color;

    private String location;

    private boolean recur;
    private final Recurrence recurrence;

    private EventData eventData;

    private boolean editing;

    private Message creatorMessage;

    private long lastEdit;


    /**
     * Creates a new PreEvent for the specified Guild.
     *
     * @param _guildId The ID of the guild.
     */
    public PreEvent(final Snowflake _guildId) {
        this.guildId = _guildId;
        this.eventId = "N/a";

        this.timeZone = "Unknown";

        this.color = EventColor.NONE;

        this.recur = false;
        this.recurrence = new Recurrence();

        this.eventData = EventData.empty();

        this.editing = false;
        this.lastEdit = System.currentTimeMillis();
    }

    public PreEvent(final Snowflake _guildId, final Event e) {
        this.guildId = _guildId;
        this.eventId = e.getId();

        this.color = EventColor.fromNameOrHexOrID(e.getColorId());

        this.recurrence = new Recurrence();

        if (e.getRecurrence() != null && !e.getRecurrence().isEmpty()) {
            this.recur = true;
            this.recurrence.fromRRule(e.getRecurrence().get(0));
        }

        if (e.getSummary() != null)
            this.summary = e.getSummary();

        if (e.getDescription() != null)
            this.description = e.getDescription();

        if (e.getLocation() != null)
            this.location = e.getLocation();


        this.startDateTime = e.getStart();
        this.endDateTime = e.getEnd();

        //Here is where I need to fix the display times
        final GuildSettings settings = DatabaseManager.getSettings(this.guildId).block();
        //TODO: Support multiple calendars
        final CalendarData data = DatabaseManager.getMainCalendar(this.guildId).block();

        Calendar cal = null;
        try {
            cal = CalendarWrapper.getCalendar(data, settings).block();
        } catch (final Exception ex) {
            LogFeed.log(LogObject
                .forException("Failed to get proper data time for event!", ex, this.getClass()));
        }

        if (cal != null) {
            this.timeZone = cal.getTimeZone();
        } else {
            this.timeZone = "ERROR/Unknown";
        }

        this.eventData = DatabaseManager.getEventData(this.guildId, e.getId()).block();

        this.editing = false;
        this.lastEdit = System.currentTimeMillis();
    }

    //Getters

    /**
     * Gets the ID of the guild who owns this PreEvent.
     *
     * @return The ID of the guild who owns this PreEvent.
     */
    public Snowflake getGuildId() {
        return this.guildId;
    }

    public String getEventId() {
        return this.eventId;
    }

    /**
     * Gets the event summary.
     *
     * @return The event summary.
     */
    public String getSummary() {
        return this.summary;
    }

    /**
     * Gets the description.
     *
     * @return The description.
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Gets the start date and time.
     *
     * @return The start date and time.
     */
    public EventDateTime getStartDateTime() {
        return this.startDateTime;
    }

    /**
     * Gets the end date and time.
     *
     * @return The end date and time.
     */
    public EventDateTime getEndDateTime() {
        return this.endDateTime;
    }

    /**
     * Gets the timezone of the event.
     *
     * @return The timezone of the event.
     */
    public String getTimeZone() {
        return this.timeZone;
    }

    /**
     * Gets the valid Google Calendar color for this event.
     *
     * @return The valid color for this event.
     */
    public EventColor getColor() {
        return this.color;
    }

    public String getLocation() {
        return this.location;
    }

    /**
     * Gets whether or not the vent should recur.
     *
     * @return {@code true} if recurring, otherwise {@code false}.
     */
    public boolean shouldRecur() {
        return this.recur;
    }

    /**
     * Gets the recurrence rules and info for the event.
     *
     * @return The recurrence rules and info for the event.
     */
    public Recurrence getRecurrence() {
        return this.recurrence;
    }

    public EventData getEventData() {
        return this.eventData;
    }

    public boolean isEditing() {
        return this.editing;
    }

    public Message getCreatorMessage() {
        return this.creatorMessage;
    }

    public long getLastEdit() {
        return this.lastEdit;
    }

    //Setters

    /**
     * Sets the summary of the event.
     *
     * @param _summary The summary of the vent.
     */
    public void setSummary(final String _summary) {
        this.summary = _summary;
    }

    /**
     * Sets the description of the event.
     *
     * @param _description The description of the event.
     */
    public void setDescription(final String _description) {
        this.description = _description;
    }

    /**
     * Sets the start date and time of the event.
     *
     * @param _startDateTime The start date and time of the event.
     */
    public void setStartDateTime(final EventDateTime _startDateTime) {
        this.startDateTime = _startDateTime;
    }

    /**
     * Sets the end date and time of the event.
     *
     * @param _endDateTime The end date and time of the event.
     */
    public void setEndDateTime(final EventDateTime _endDateTime) {
        this.endDateTime = _endDateTime;
    }

    /**
     * Sets the timezone of the event.
     *
     * @param _timeZone The timezone of the event.
     */
    public void setTimeZone(final String _timeZone) {
        this.timeZone = _timeZone;
    }

    /**
     * Sets the valid Google Calendar color for this event.
     *
     * @param _color The valid color for this event.
     */
    public void setColor(final EventColor _color) {
        this.color = _color;
    }

    public void setLocation(final String _location) {
        this.location = _location;
    }

    /**
     * Sets whether or not the event should recur.
     *
     * @param _recur Whether or not the event should recur.
     */
    public void setShouldRecur(final boolean _recur) {
        this.recur = _recur;
    }

    public void setEventData(final EventData _data) {
        this.eventData = _data;
    }

    public void setEditing(final boolean _editing) {
        this.editing = _editing;
    }

    public void setCreatorMessage(final Message _creatorMessage) {
        this.creatorMessage = _creatorMessage;
    }

    public void setLastEdit(final long _lastEdit) {
        this.lastEdit = _lastEdit;
    }

    //Booleans/Checkers

    /**
     * Whether or not the event has all required values to be created.
     *
     * @return {@code true} if required values set, otherwise {@code false}.
     */
    public boolean hasRequiredValues() {
        return this.startDateTime != null && this.endDateTime != null;
    }
}