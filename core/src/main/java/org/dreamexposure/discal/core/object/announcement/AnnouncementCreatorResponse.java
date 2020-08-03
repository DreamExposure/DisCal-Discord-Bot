package org.dreamexposure.discal.core.object.announcement;

/**
 * Created by Nova Fox on 11/10/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
public class AnnouncementCreatorResponse {
    private final boolean successful;
    private final Announcement announcement;

    public AnnouncementCreatorResponse(final boolean successful, final Announcement announcement) {
        this.successful = successful;
        this.announcement = announcement;
    }

    //Getters

    /**
     * Gets whether or not the creator was successful.
     *
     * @return Whether or not the creator was successful.
     */
    public boolean isSuccessful() {
        return this.successful;
    }

    /**
     * Gets the announcement involved.
     *
     * @return The Announcement involved.
     */
    public Announcement getAnnouncement() {
        return this.announcement;
    }
}