package org.dreamexposure.discal.core.object.event;

import org.json.JSONObject;

import javax.annotation.Nullable;

import discord4j.core.object.util.Snowflake;

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

	public JSONObject toJson() {
		JSONObject json = new JSONObject();

		json.put("guild_id", guildId.asLong());
		json.put("event_id", eventId);
		json.put("event_end", eventEnd);
		json.put("image_link", imageLink);

		return json;
	}

	public EventData fromJson(JSONObject json) {
		eventId = json.getString("event_id");
		eventEnd = json.getLong("event_end");
		imageLink = json.getString("image_link");

		return this;
	}
}