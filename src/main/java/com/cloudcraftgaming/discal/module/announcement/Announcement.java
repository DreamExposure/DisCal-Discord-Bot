package com.cloudcraftgaming.discal.module.announcement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

/**
 * Created by Nova Fox on 1/7/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
@SuppressWarnings("WeakerAccess")
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
    /**
     *  Gets the ID of the announcement.
     * @return The ID of the announcement.
     */
    public UUID getAnnouncementId() {
        return announcementId;
    }

    /**
     * Gets the Guild ID the announcement belongs to.
     * @return The Guild ID the announcement belongs to.
     */
    public String getGuildId() {
        return guildId;
    }

    /**
     * Gets the ID of the channel the announcement is to be broadcast in.
     * @return The ID of the channel the announcement is to be broadcast in.
     */
    public String getAnnouncementChannelId() {
        return announcementChannelId;
    }

    /**
     * Gets the IDs of Roles that are subscribed to the announcement.
     * @return The IDs fo the Roles that are subscribed to the announcement.
     */
    public ArrayList<String> getSubscriberRoleIds() {
        return subscriberRoleIds;
    }

    /**
     * Gets the IDs of the Users that are subscribed to the announcement.
     * @return The IDs of the Users that are subscribed to the announcement.
     */
    public ArrayList<String> getSubscriberUserIds() {
        return subscriberUserIds;
    }

    /**
     * Gets a string of ALL roles that are subscribed to the announcement.
     * @return A string of roles that are subscribed to the announcement.
     */
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

    /**
     * Gets a string of ALL users that are subscribed to the announcement.
     * @return A string of users that are subscribed to the announcement.
     */
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

    /**
     * Get the type of announcement this is.
     * @return The type of announcement this is.
     */
    public AnnouncementType getAnnouncementType() {
        return type;
    }

    /**
     * Gets the Event ID linked to the announcement, if any.
     * @return The Event ID linked to the announcement.
     */
    public String getEventId() {
        return eventId;
    }

    /**
     * Gets the amount of hours before the event to announce.
     * @return The amount of hours before the event to announce.
     */
    public int getHoursBefore() {
        return hoursBefore;
    }

    /**
     * Gets the amount of minutes before the event to announce.
     * @return The amount of minutes before the event to announce.
     */
    public int getMinutesBefore() {
        return minutesBefore;
    }

    //Setters
    /**
     * Sets the ID of the channel to announce in.
     * @param _announcementChannelId The ID of the channel to announce in.
     */
    public void setAnnouncementChannelId(String _announcementChannelId) {
        announcementChannelId = _announcementChannelId;
    }

    /**
     * Sets the type of announcement this is.
     * @param _type The type of the announcement this is.
     */
    public void setAnnouncementType(AnnouncementType _type) {
        type = _type;
    }

    /**
     * Sets the ID of the event to announce for.
     * @param _eventId The ID of the event to announce for.
     */
    public void setEventId(String _eventId) {
        eventId = _eventId;
    }

    /**
     * Sets the hours before the event to announce for.
     * @param _hoursBefore The hours before the event to announce for.
     */
    public void setHoursBefore(Integer _hoursBefore) {
        hoursBefore = _hoursBefore;
    }

    /**
     * Sets the minutes before the event to announce for.
     * @param _minutesBefore The minutes before the event to announce for.
     */
    public void setMinutesBefore(Integer _minutesBefore) {
        minutesBefore = _minutesBefore;
    }

    /**
     * Sets the subscribers of the announcement from a String.
     * @param subList String value of subscribing roles.
     */
    public void setSubscriberRoleIdsFromString(String subList) {
        String[] subs = subList.split(",");
        Collections.addAll(subscriberRoleIds, subs);
    }

    /**
     * Sets the subscribers of the announcement from a string.
     * @param subList String value of subscribing users.
     */
    public void setSubscriberUserIdsFromString(String subList) {
        String[] subs = subList.split(",");
        Collections.addAll(subscriberUserIds, subs);
    }

    //Booleans/Checkers
    /**
     * Checks if the announcement has all required values to be entered into a database.
     * @return <code>true</code> if all values are present, else <code>false</code>.
     */
    public Boolean hasRequiredValues() {
        return (minutesBefore != 0 || hoursBefore != 0) && !(type.equals(AnnouncementType.SPECIFIC) && eventId.equalsIgnoreCase("N/a")) && !announcementChannelId.equalsIgnoreCase("N/a");
    }
}