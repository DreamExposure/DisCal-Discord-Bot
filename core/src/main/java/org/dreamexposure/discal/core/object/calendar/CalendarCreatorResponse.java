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

    public CalendarCreatorResponse(final boolean successful, final Calendar calendar, final Message message,
                                   final boolean edited) {
        this.successful = successful;
        this.calendar = calendar;
        this.creatorMessage = message;
        this.edited = edited;
    }

    //Getters

    /**
     * Whether or not the creation was successful.
     *
     * @return {@code true} if successful, else {@code false}.
     */
    public boolean isSuccessful() {
        return this.successful;
    }

    /**
     * The calendar involved. Can be null.
     *
     * @return The calendar involved, may be null.
     */
    public Calendar getCalendar() {
        return this.calendar;
    }

    public boolean isEdited() {
        return this.edited;
    }

    public Message getCreatorMessage() {
        return this.creatorMessage;
    }
}