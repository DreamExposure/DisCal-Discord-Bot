package com.cloudcraftgaming.discal.module.announcement;

/**
 * Created by Nova Fox on 3/4/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class AnnouncementCreatorResponse {
    private final Boolean successful;

    private Announcement announcement;

    AnnouncementCreatorResponse(Boolean _successful) {
        successful = _successful;
    }

    AnnouncementCreatorResponse(Boolean _successful, Announcement _announcement) {
        successful = _successful;
        announcement = _announcement;
    }

    //Getters
    public Boolean isSuccessful() {
        return successful;
    }

    public Announcement getAnnouncement() {
        return announcement;
    }

    //Setters
    public void setAnnouncement(Announcement _announcement) {
        announcement = _announcement;
    }
}