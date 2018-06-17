package com.cloudcraftgaming.discal.api.object.announcement;

import com.cloudcraftgaming.discal.api.enums.announcement.AnnouncementType;
import com.cloudcraftgaming.discal.api.enums.event.EventColor;
import sx.blah.discord.handle.obj.IMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

/**
 * Created by Nova Fox on 11/10/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
@SuppressWarnings("WeakerAccess")
public class Announcement {
	private final UUID announcementId;
	private final long guildId;

	private final ArrayList<String> subscriberRoleIds = new ArrayList<>();
	private final ArrayList<String> subscriberUserIds = new ArrayList<>();

	private String announcementChannelId;
	private AnnouncementType type;
	private String eventId;
	private EventColor eventColor;
	private int hoursBefore;
	private int minutesBefore;
	private String info;

	private boolean enabled;
	private boolean infoOnly;

	private IMessage creatorMessage;

	private boolean editing;

	private long lastEdit;

	/**
	 * Use this constructor when creating NEW announcements!!!
	 *
	 * @param _guildId The ID of the Guild this announcement belongs to.
	 */
	public Announcement(long _guildId) {
		guildId = _guildId;
		announcementId = UUID.randomUUID();
		announcementChannelId = "N/a";
		eventId = "N/a";
		eventColor = EventColor.RED;
		type = AnnouncementType.UNIVERSAL;
		hoursBefore = 0;
		minutesBefore = 0;
		info = "None";
		enabled = true;
		infoOnly = false;

		lastEdit = System.currentTimeMillis();
	}

	/**
	 * Use this constructor when retrieving date from the database!!!
	 *
	 * @param _announcementId The ID of the announcement object.
	 * @param _guildId        The ID of the guild the announcement belongs to.
	 */
	public Announcement(UUID _announcementId, long _guildId) {
		announcementId = _announcementId;
		guildId = _guildId;
		announcementChannelId = "N/a";
		eventId = "N/a";
		eventColor = EventColor.RED;
		type = AnnouncementType.UNIVERSAL;
		hoursBefore = 0;
		minutesBefore = 0;
		info = "None";
		enabled = true;
		infoOnly = false;

		editing = false;
		lastEdit = System.currentTimeMillis();
	}

	@SuppressWarnings("CopyConstructorMissesField")
	public Announcement(Announcement from) {
		guildId = from.getGuildId();
		announcementId = UUID.randomUUID();
		announcementChannelId = from.getAnnouncementChannelId();
		eventId = from.getEventId();
		eventColor = from.getEventColor();
		type = from.getAnnouncementType();
		hoursBefore = from.getHoursBefore();
		minutesBefore = from.getMinutesBefore();
		info = from.getInfo();
		enabled = from.isEnabled();
		infoOnly = from.isInfoOnly();

		setSubscriberRoleIdsFromString(from.getSubscriberRoleIdString());
		setSubscriberUserIdsFromString(from.getSubscriberUserIdString());

		editing = false;
		lastEdit = System.currentTimeMillis();
	}

	public Announcement(Announcement from, boolean copyId) {
		guildId = from.getGuildId();
		if (copyId) {
			announcementId = from.getAnnouncementId();
		} else {
			announcementId = UUID.randomUUID();
		}
		announcementChannelId = from.getAnnouncementChannelId();
		eventId = from.getEventId();
		eventColor = from.getEventColor();
		type = from.getAnnouncementType();
		hoursBefore = from.getHoursBefore();
		minutesBefore = from.getMinutesBefore();
		info = from.getInfo();
		enabled = from.isEnabled();
		infoOnly = from.isInfoOnly();

		setSubscriberRoleIdsFromString(from.getSubscriberRoleIdString());
		setSubscriberUserIdsFromString(from.getSubscriberUserIdString());

		editing = false;
		lastEdit = System.currentTimeMillis();
	}

	//Getters

	/**
	 * Gets the ID of the announcement.
	 *
	 * @return The ID of the announcement.
	 */
	public UUID getAnnouncementId() {
		return announcementId;
	}

	/**
	 * Gets the Guild ID the announcement belongs to.
	 *
	 * @return The Guild ID the announcement belongs to.
	 */
	public long getGuildId() {
		return guildId;
	}

	/**
	 * Gets the ID of the channel the announcement is to be broadcast in.
	 *
	 * @return The ID of the channel the announcement is to be broadcast in.
	 */
	public String getAnnouncementChannelId() {
		return announcementChannelId;
	}

	/**
	 * Gets the IDs of Roles that are subscribed to the announcement.
	 *
	 * @return The IDs fo the Roles that are subscribed to the announcement.
	 */
	public ArrayList<String> getSubscriberRoleIds() {
		return subscriberRoleIds;
	}

	/**
	 * Gets the IDs of the Users that are subscribed to the announcement.
	 *
	 * @return The IDs of the Users that are subscribed to the announcement.
	 */
	public ArrayList<String> getSubscriberUserIds() {
		return subscriberUserIds;
	}

	/**
	 * Gets a string of ALL roles that are subscribed to the announcement.
	 *
	 * @return A string of roles that are subscribed to the announcement.
	 */
	public String getSubscriberRoleIdString() {
		StringBuilder subs = new StringBuilder();
		Integer i = 0;
		for (String sub : subscriberRoleIds) {
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
		Integer i = 0;
		for (String sub : subscriberUserIds) {
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
		if (type != null)
			return type;
		else
			return AnnouncementType.UNIVERSAL;
	}

	/**
	 * Gets the Event ID linked to the announcement, if any.
	 *
	 * @return The Event ID linked to the announcement.
	 */
	public String getEventId() {
		return eventId;
	}

	public EventColor getEventColor() {
		return eventColor;
	}

	/**
	 * Gets the amount of hours before the event to announce.
	 *
	 * @return The amount of hours before the event to announce.
	 */
	public int getHoursBefore() {
		return hoursBefore;
	}

	/**
	 * Gets the amount of minutes before the event to announce.
	 *
	 * @return The amount of minutes before the event to announce.
	 */
	public int getMinutesBefore() {
		return minutesBefore;
	}

	/**
	 * Gets extra info for the announcement.
	 *
	 * @return Extra info for the announcement.
	 */
	public String getInfo() {
		return info;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public boolean isInfoOnly() {
		return infoOnly;
	}

	public IMessage getCreatorMessage() {
		return creatorMessage;
	}

	public boolean isEditing() {
		return editing;
	}

	public long getLastEdit() {
		return lastEdit;
	}

	//Setters

	/**
	 * Sets the ID of the channel to announce in.
	 *
	 * @param _announcementChannelId The ID of the channel to announce in.
	 */
	public void setAnnouncementChannelId(String _announcementChannelId) {
		announcementChannelId = _announcementChannelId;
	}

	/**
	 * Sets the type of announcement this is.
	 *
	 * @param _type The type of the announcement this is.
	 */
	public void setAnnouncementType(AnnouncementType _type) {
		type = _type;
	}

	/**
	 * Sets the ID of the event to announce for.
	 *
	 * @param _eventId The ID of the event to announce for.
	 */
	public void setEventId(String _eventId) {
		eventId = _eventId;
	}

	public void setEventColor(EventColor _eventColor) {
		eventColor = _eventColor;
	}

	/**
	 * Sets the hours before the event to announce for.
	 *
	 * @param _hoursBefore The hours before the event to announce for.
	 */
	public void setHoursBefore(Integer _hoursBefore) {
		hoursBefore = _hoursBefore;
	}

	/**
	 * Sets the minutes before the event to announce for.
	 *
	 * @param _minutesBefore The minutes before the event to announce for.
	 */
	public void setMinutesBefore(int _minutesBefore) {
		minutesBefore = _minutesBefore;
	}

	public void setInfo(String _info) {
		info = _info;
	}

	public void setEnabled(boolean _enabled) {
		enabled = _enabled;
	}

	public void setInfoOnly(boolean _infoOnly) {
		infoOnly = _infoOnly;
	}

	/**
	 * Sets the subscribers of the announcement from a String.
	 *
	 * @param subList String value of subscribing roles.
	 */
	public void setSubscriberRoleIdsFromString(String subList) {
		String[] subs = subList.split(",");
		Collections.addAll(subscriberRoleIds, subs);
	}

	/**
	 * Sets the subscribers of the announcement from a string.
	 *
	 * @param subList String value of subscribing users.
	 */
	public void setSubscriberUserIdsFromString(String subList) {
		String[] subs = subList.split(",");
		Collections.addAll(subscriberUserIds, subs);
	}

	public void setCreatorMessage(IMessage _message) {
		creatorMessage = _message;
	}

	public void setEditing(boolean _editing) {
		editing = _editing;
	}

	public void setLastEdit(long _lastEdit) {
		lastEdit = _lastEdit;
	}

	//Booleans/Checkers

	/**
	 * Checks if the announcement has all required values to be entered into a database.
	 *
	 * @return <code>true</code> if all values are present, else <code>false</code>.
	 */
	public Boolean hasRequiredValues() {
		return (minutesBefore != 0 || hoursBefore != 0) && !(type.equals(AnnouncementType.SPECIFIC) && eventId.equalsIgnoreCase("N/a")) && !announcementChannelId.equalsIgnoreCase("N/a");
	}
}