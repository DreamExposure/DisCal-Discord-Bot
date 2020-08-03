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
    private final Event event;
    private final Message creatorMessage;
    private final boolean edited;

    public EventCreatorResponse(final boolean successful, final Event event, final Message creatorMessage,
                                final boolean edited) {
        this.successful = successful;
        this.event = event;
        this.creatorMessage = creatorMessage;
        this.edited = edited;
    }

    //Getters

    /**
     * Whether or not the creator was successful.
     *
     * @return {@code true} if successful, else {@code false}.
     */
    public boolean isSuccessful() {
        return this.successful;
    }

    public Message getCreatorMessage() {
        return this.creatorMessage;
    }

    public boolean isEdited() {
        return this.edited;
    }

    /**
     * Gets the event that was created.
     *
     * @return The event that was created.
     */
    public Event getEvent() {
        return this.event;
    }
}