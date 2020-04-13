package org.dreamexposure.discal.core.object.announcement;

import discord4j.core.object.entity.Message;

/**
 * Created by Nova Fox on 11/10/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
public class AnnouncementCreatorResponse {
    private final boolean successful;
    private final Announcement announcement;
    private final Message creatorMessage;

    public AnnouncementCreatorResponse(boolean successful, Announcement announcement,
                                       Message creatorMessage) {
        this.successful = successful;
        this.announcement = announcement;
        this.creatorMessage = creatorMessage;
    }

    //Getters

    /**
     * Gets whether or not the creator was successful.
     *
     * @return Whether or not the creator was successful.
     */
    public boolean isSuccessful() {
        return successful;
    }

    /**
     * Gets the announcement involved.
     *
     * @return The Announcement involved.
     */
    public Announcement getAnnouncement() {
        return announcement;
    }

    public Message getCreatorMessage() {
        return creatorMessage;
    }
}