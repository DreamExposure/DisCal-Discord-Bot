package org.dreamexposure.discal.core.object.event;

import discord4j.core.object.util.Snowflake;

import javax.annotation.Nullable;

/**
 * Created by Nova Fox on 11/10/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
public class EventData {
	private final Snowflake guildId;

	private String eventId;
	private long eventEnd;
	private String imageLink;

	public EventData(Snowflake _guildId) {
		guildId = _guildId;
	}

	//Getters
	public Snowflake getGuildId() {
		return guildId;
	}

	public String getEventId() {
		return eventId;
	}

	public long getEventEnd() {
		return eventEnd;
	}

	public String getImageLink() {
		return imageLink;
	}

	//Setters
	public void setEventId(String _eventId) {
		eventId = _eventId;
	}

	public void setEventEnd(long _eventEnd) {
		eventEnd = _eventEnd;
	}

	public void setImageLink(@Nullable String _link) {
		imageLink = _link;
	}

	//Boolean/Checkers
	public boolean shouldBeSaved() {
		return imageLink != null;
	}
}