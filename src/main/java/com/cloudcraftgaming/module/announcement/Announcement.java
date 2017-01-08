package com.cloudcraftgaming.module.announcement;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by Nova Fox on 1/7/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class Announcement {
    private final UUID announcementId;
    private final String guildId;

    private final ArrayList<String> subscribers = new ArrayList<>();

    private String announcementChannelId;
    private int hoursBefore;
    private int minutesBefore;

    /**
     * Use this constructor when creating NEW announcements!!!
     * @param _guildId The ID of the Guild this announcement belongs to.
     */
    public Announcement(String _guildId) {
        guildId = _guildId;
        announcementId = UUID.randomUUID();
    }

    /**
     * Use this constructor when retrieving date from the database!!!
     * @param _announcementId The ID of the announcement object.
     * @param _guildId The ID of the guild the announcement belongs to.
     */
    public Announcement(UUID _announcementId, String _guildId) {
        announcementId = _announcementId;
        guildId = _guildId;
    }

    //Getters
    public UUID getAnnouncementId() {
        return announcementId;
    }

    public String getGuildId() {
        return guildId;
    }

    public String getAnnouncementChannelId() {
        return announcementChannelId;
    }

    public ArrayList<String> getSubscribers() {
        return subscribers;
    }

    public int getHoursBefore() {
        return hoursBefore;
    }

    public int getMinutesBefore() {
        return minutesBefore;
    }

    //Setters
    public void setAnnouncementChannelId(String _announcementChannelId) {
        announcementChannelId = _announcementChannelId;
    }

    public void setHoursBefore(Integer _hoursBefore) {
        hoursBefore = _hoursBefore;
    }

    public void setMinutesBefore(Integer _minutesBefore) {
        minutesBefore = _minutesBefore;
    }
}