package org.dreamexposure.discal.core.object.announcement;

import org.dreamexposure.discal.core.enums.announcement.AnnouncementModifier;
import org.dreamexposure.discal.core.enums.announcement.AnnouncementType;
import org.dreamexposure.discal.core.enums.event.EventColor;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;

/**
 * Created by Nova Fox on 11/10/17.
 * Website: www.dreamexposure.org
 * For Project: DisCal-Discord-Bot
 */
@SuppressWarnings("Duplicates")
public class Announcement {
    private UUID announcementId;
    private Snowflake guildId;

    private final ArrayList<String> subscriberRoleIds = new ArrayList<>();
    private final ArrayList<String> subscriberUserIds = new ArrayList<>();

    private String announcementChannelId;
    private AnnouncementType type;
    private AnnouncementModifier modifier;
    private String eventId;
    private EventColor eventColor;
    private int hoursBefore;
    private int minutesBefore;
    private String info;

    private boolean enabled;
    private boolean infoOnly;

    //Stuff for creator/editor wizards.
    private Message creatorMessage;
    private boolean editing;
    private long lastEdit;

    /**
     * Use this constructor when creating NEW announcements!!!
     *
     * @param _guildId The ID of the Guild this announcement belongs to.
     */
    public Announcement(final Snowflake _guildId) {
        this.guildId = _guildId;
        this.announcementId = UUID.randomUUID();
        this.announcementChannelId = "N/a";
        this.eventId = "N/a";
        this.eventColor = EventColor.RED;
        this.type = AnnouncementType.UNIVERSAL;
        this.modifier = AnnouncementModifier.BEFORE;
        this.hoursBefore = 0;
        this.minutesBefore = 0;
        this.info = "None";
        this.enabled = true;
        this.infoOnly = false;

        this.lastEdit = System.currentTimeMillis();
    }

    /**
     * Use this constructor when retrieving date from the database!!!
     *
     * @param _announcementId The ID of the announcement object.
     * @param _guildId        The ID of the guild the announcement belongs to.
     */
    public Announcement(final UUID _announcementId, final Snowflake _guildId) {
        this.announcementId = _announcementId;
        this.guildId = _guildId;
        this.announcementChannelId = "N/a";
        this.eventId = "N/a";
        this.eventColor = EventColor.RED;
        this.type = AnnouncementType.UNIVERSAL;
        this.modifier = AnnouncementModifier.BEFORE;
        this.hoursBefore = 0;
        this.minutesBefore = 0;
        this.info = "None";
        this.enabled = true;
        this.infoOnly = false;

        this.editing = false;
        this.lastEdit = System.currentTimeMillis();
    }

    @SuppressWarnings("CopyConstructorMissesField")
    public Announcement(final Announcement from) {
        this.guildId = from.getGuildId();
        this.announcementId = UUID.randomUUID();
        this.announcementChannelId = from.getAnnouncementChannelId();
        this.eventId = from.getEventId();
        this.eventColor = from.getEventColor();
        this.type = from.getAnnouncementType();
        this.modifier = from.getModifier();
        this.hoursBefore = from.getHoursBefore();
        this.minutesBefore = from.getMinutesBefore();
        this.info = from.getInfo();
        this.enabled = from.isEnabled();
        this.infoOnly = from.isInfoOnly();

        this.setSubscriberRoleIdsFromString(from.getSubscriberRoleIdString());
        this.setSubscriberUserIdsFromString(from.getSubscriberUserIdString());

        this.editing = false;
        this.lastEdit = System.currentTimeMillis();
    }

    public Announcement(final Announcement from, final boolean copyId) {
        this.guildId = from.getGuildId();
        if (copyId) {
            this.announcementId = from.getAnnouncementId();
        } else {
            this.announcementId = UUID.randomUUID();
        }
        this.announcementChannelId = from.getAnnouncementChannelId();
        this.eventId = from.getEventId();
        this.eventColor = from.getEventColor();
        this.type = from.getAnnouncementType();
        this.modifier = from.getModifier();
        this.hoursBefore = from.getHoursBefore();
        this.minutesBefore = from.getMinutesBefore();
        this.info = from.getInfo();
        this.enabled = from.isEnabled();
        this.infoOnly = from.isInfoOnly();

        this.setSubscriberRoleIdsFromString(from.getSubscriberRoleIdString());
        this.setSubscriberUserIdsFromString(from.getSubscriberUserIdString());

        this.editing = false;
        this.lastEdit = System.currentTimeMillis();
    }

    //Getters

    /**
     * Gets the ID of the announcement.
     *
     * @return The ID of the announcement.
     */
    public UUID getAnnouncementId() {
        return this.announcementId;
    }

    /**
     * Gets the Guild ID the announcement belongs to.
     *
     * @return The Guild ID the announcement belongs to.
     */
    public Snowflake getGuildId() {
        return this.guildId;
    }

    /**
     * Gets the ID of the channel the announcement is to be broadcast in.
     *
     * @return The ID of the channel the announcement is to be broadcast in.
     */
    public String getAnnouncementChannelId() {
        return this.announcementChannelId;
    }

    /**
     * Gets the IDs of Roles that are subscribed to the announcement.
     *
     * @return The IDs fo the Roles that are subscribed to the announcement.
     */
    public ArrayList<String> getSubscriberRoleIds() {
        return this.subscriberRoleIds;
    }

    /**
     * Gets the IDs of the Users that are subscribed to the announcement.
     *
     * @return The IDs of the Users that are subscribed to the announcement.
     */
    public ArrayList<String> getSubscriberUserIds() {
        return this.subscriberUserIds;
    }

    /**
     * Gets a string of ALL roles that are subscribed to the announcement.
     *
     * @return A string of roles that are subscribed to the announcement.
     */
    public String getSubscriberRoleIdString() {
        StringBuilder subs = new StringBuilder();
        int i = 0;
        for (final String sub : this.subscriberRoleIds) {
            if (i == 0) {
                subs = new StringBuilder(sub);
            } else {
                subs.append(",").append(sub);
            }
            i++;
        }
        return subs.toString();
    }

    /**
     * Gets a string of ALL users that are subscribed to the announcement.
     *
     * @return A string of users that are subscribed to the announcement.
     */
    public String getSubscriberUserIdString() {
        StringBuilder subs = new StringBuilder();
        int i = 0;
        for (final String sub : this.subscriberUserIds) {
            if (i == 0) {
                subs = new StringBuilder(sub);
            } else {
                subs.append(",").append(sub);
            }
            i++;
        }
        return subs.toString();
    }

    /**
     * Get the type of announcement this is.
     *
     * @return The type of announcement this is.
     */
    public AnnouncementType getAnnouncementType() {
        if (this.type != null)
            return this.type;
        else
            return AnnouncementType.UNIVERSAL;
    }

    public AnnouncementModifier getModifier() {
        return this.modifier;
    }

    /**
     * Gets the Event ID linked to the announcement, if any.
     *
     * @return The Event ID linked to the announcement.
     */
    public String getEventId() {
        return this.eventId;
    }

    public EventColor getEventColor() {
        return this.eventColor;
    }

    /**
     * Gets the amount of hours before the event to announce.
     *
     * @return The amount of hours before the event to announce.
     */
    public int getHoursBefore() {
        return this.hoursBefore;
    }

    /**
     * Gets the amount of minutes before the event to announce.
     *
     * @return The amount of minutes before the event to announce.
     */
    public int getMinutesBefore() {
        return this.minutesBefore;
    }

    /**
     * Gets extra info for the announcement.
     *
     * @return Extra info for the announcement.
     */
    public String getInfo() {
        return this.info;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public boolean isInfoOnly() {
        return this.infoOnly;
    }

    public Message getCreatorMessage() {
        return this.creatorMessage;
    }

    public boolean isEditing() {
        return this.editing;
    }

    public long getLastEdit() {
        return this.lastEdit;
    }

    //Setters

    /**
     * Sets the ID of the channel to announce in.
     *
     * @param _announcementChannelId The ID of the channel to announce in.
     */
    public void setAnnouncementChannelId(final String _announcementChannelId) {
        this.announcementChannelId = _announcementChannelId;
    }

    /**
     * Sets the type of announcement this is.
     *
     * @param _type The type of the announcement this is.
     */
    public void setAnnouncementType(final AnnouncementType _type) {
        this.type = _type;
    }

    public void setModifier(final AnnouncementModifier modifier) {
        this.modifier = modifier;
    }

    /**
     * Sets the ID of the event to announce for.
     *
     * @param _eventId The ID of the event to announce for.
     */
    public void setEventId(final String _eventId) {
        this.eventId = _eventId;
    }

    public void setEventColor(final EventColor _eventColor) {
        this.eventColor = _eventColor;
    }

    /**
     * Sets the hours before the event to announce for.
     *
     * @param _hoursBefore The hours before the event to announce for.
     */
    public void setHoursBefore(final Integer _hoursBefore) {
        this.hoursBefore = _hoursBefore;
    }

    /**
     * Sets the minutes before the event to announce for.
     *
     * @param _minutesBefore The minutes before the event to announce for.
     */
    public void setMinutesBefore(final int _minutesBefore) {
        this.minutesBefore = _minutesBefore;
    }

    public void setInfo(final String _info) {
        this.info = _info;
    }

    public void setEnabled(final boolean _enabled) {
        this.enabled = _enabled;
    }

    public void setInfoOnly(final boolean _infoOnly) {
        this.infoOnly = _infoOnly;
    }

    /**
     * Sets the subscribers of the announcement from a String.
     *
     * @param subList String value of subscribing roles.
     */
    public void setSubscriberRoleIdsFromString(final String subList) {
        final String[] subs = subList.split(",");
        Collections.addAll(this.subscriberRoleIds, subs);
    }

    /**
     * Sets the subscribers of the announcement from a string.
     *
     * @param subList String value of subscribing users.
     */
    public void setSubscriberUserIdsFromString(final String subList) {
        final String[] subs = subList.split(",");
        Collections.addAll(this.subscriberUserIds, subs);
    }

    public void setCreatorMessage(final Message _message) {
        this.creatorMessage = _message;
    }

    public void setEditing(final boolean _editing) {
        this.editing = _editing;
    }

    public void setLastEdit(final long _lastEdit) {
        this.lastEdit = _lastEdit;
    }

    //Booleans/Checkers

    /**
     * Checks if the announcement has all required values to be entered into a database.
     *
     * @return {@code true} if all values are present, else {@code false}.
     */
    public Boolean hasRequiredValues() {
        return (this.minutesBefore != 0 || this.hoursBefore != 0)
            && !(this.type.equals(AnnouncementType.SPECIFIC) && "N/a".equalsIgnoreCase(this.eventId))
            && !"N/a".equalsIgnoreCase(this.announcementChannelId);
    }

    public JSONObject toJson() {
        final JSONObject data = new JSONObject();

        data.put("guild_id", this.guildId.asString());
        data.put("id", this.announcementId.toString());

        final JSONArray roles = new JSONArray();
        for (final String s : this.subscriberRoleIds) {
            roles.put(s);
        }
        data.put("subscriber_roles", roles);

        final JSONArray users = new JSONArray();
        for (final String s : this.subscriberUserIds) {
            users.put(s);
        }
        data.put("subscriber_users", users);

        data.put("channel_id", this.announcementChannelId);
        data.put("type", this.type.name());
        data.put("modifier", this.modifier.name());
        data.put("event_id", this.eventId);
        data.put("event_color", this.eventColor.name());
        data.put("hours", this.hoursBefore);
        data.put("minutes", this.minutesBefore);
        data.put("info", this.info);
        data.put("enabled", this.enabled);
        data.put("info_only", this.infoOnly);

        return data;
    }

    public Announcement fromJson(final JSONObject data) {
        this.guildId = Snowflake.of(data.getString("guild_id"));
        this.announcementId = UUID.fromString(data.getString("id"));

        final JSONArray roles = data.getJSONArray("subscriber_roles");
        for (int i = 0; i < roles.length(); i++) {
            this.subscriberRoleIds.add(roles.getString(i));
        }

        final JSONArray users = data.getJSONArray("subscriber_users");
        for (int i = 0; i < users.length(); i++) {
            this.subscriberUserIds.add(users.getString(i));
        }

        this.announcementChannelId = data.getString("channel_id");
        this.type = AnnouncementType.fromValue(data.getString("type"));
        this.modifier = AnnouncementModifier.fromValue(data.getString("modifier"));
        this.eventId = data.getString("event_id");
        this.eventColor = EventColor.valueOf(data.getString("event_color"));
        this.hoursBefore = data.getInt("hours");
        this.minutesBefore = data.getInt("minutes");
        this.info = data.getString("info");
        this.enabled = data.getBoolean("enabled");
        this.infoOnly = data.getBoolean("info_only");

        return this;
    }
}