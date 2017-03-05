package com.cloudcraftgaming.module.announcement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

/**
 * Created by Nova Fox on 1/7/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class Announcement {
    private final UUID announcementId;
    private final String guildId;

    private final ArrayList<String> subscriberRoleIds = new ArrayList<>();
    private final ArrayList<String> subscriberUserIds = new ArrayList<>();

    private String announcementChannelId;
    private AnnouncementType type;
    private String eventId;
    private int hoursBefore;
    private int minutesBefore;

    /**
     * Use this constructor when creating NEW announcements!!!
     * @param _guildId The ID of the Guild this announcement belongs to.
     */
    public Announcement(String _guildId) {
        guildId = _guildId;
        announcementId = UUID.randomUUID();
        announcementChannelId = "N/a";
        eventId = "N/a";
        type = AnnouncementType.UNIVERSAL;
        hoursBefore = 0;
        minutesBefore = 0;
    }

    /**
     * Use this constructor when retrieving date from the database!!!
     * @param _announcementId The ID of the announcement object.
     * @param _guildId The ID of the guild the announcement belongs to.
     */
    public Announcement(UUID _announcementId, String _guildId) {
        announcementId = _announcementId;
        guildId = _guildId;
        announcementChannelId = "N/a";
        eventId = "N/a";
        type = AnnouncementType.UNIVERSAL;
        hoursBefore = 0;
        minutesBefore = 0;
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

    public ArrayList<String> getSubscriberRoleIds() {
        return subscriberRoleIds;
    }

    public ArrayList<String> getSubscriberUserIds() {
        return subscriberUserIds;
    }

    public String getSubscriberRoleIdString() {
        String subs = "";
        Integer i = 0;
        for (String sub : subscriberRoleIds) {
            if (i == 0) {
                subs = sub;
            } else {
                subs = subs + "," + sub;
            }
            i++;
        }
        return subs;
    }

    public String getSubscriberUserIdString() {
        String subs = "";
        Integer i = 0;
        for (String sub : subscriberUserIds) {
            if (i == 0) {
                subs = sub;
            } else {
                subs = subs + "," + sub;
            }
            i++;
        }
        return subs;
    }

    public AnnouncementType getAnnouncementType() {
        return type;
    }

    public String getEventId() {
        return eventId;
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

    public void setAnnouncementType(AnnouncementType _type) {
        type = _type;
    }

    public void setEventId(String _eventId) {
        eventId = _eventId;
    }

    public void setHoursBefore(Integer _hoursBefore) {
        hoursBefore = _hoursBefore;
    }

    public void setMinutesBefore(Integer _minutesBefore) {
        minutesBefore = _minutesBefore;
    }

    public void setSubscriberRoleIdsFromString(String subList) {
        String[] subs = subList.split(",");
        Collections.addAll(subscriberRoleIds, subs);
    }

    public void setSubscriberUserIdsFromString(String subList) {
        String[] subs = subList.split(",");
        Collections.addAll(subscriberUserIds, subs);
    }

    //Booleans/Checkers
    public Boolean hasRequiredValues() {
        if (minutesBefore != 0 || hoursBefore != 0) {
            if (type.equals(AnnouncementType.SPECIFIC)) {
                if (eventId.equalsIgnoreCase("N/a")) {
                    return false;
                }
            }
            return !announcementChannelId.equalsIgnoreCase("N/a");
        }
        return false;
    }
}