package org.dreamexposure.discal.core.object.event;

import org.json.JSONObject;

import discord4j.rest.util.Snowflake;

/**
 * Created by Nova Fox on 11/10/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
public class EventData {
	public static EventData fromJson(JSONObject json) {
		return new EventData(
				Snowflake.of(json.getString("guild_id")),
				json.getString("event_id"),
				json.getLong("event_end"),
				json.getString("image_link")
		);
	}

	public static EventData fromImage(Snowflake guildId, String eventId, long eventEnd,
									  String imageLink) {
		return new EventData(guildId, eventId, eventEnd, imageLink);
	}

	public static EventData empty() {
		return new EventData(Snowflake.of(0), "", 0, "");
	}

	private final Snowflake guildId;
	private final String eventId;
	private final long eventEnd;
	private final String imageLink;

	private EventData(Snowflake guildId, String eventId, long eventEnd, String imageLink) {
		this.guildId = guildId;
		this.eventId = eventId;
		this.eventEnd = eventEnd;
		this.imageLink = imageLink;
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

	//Boolean/Checkers
	public boolean shouldBeSaved() {
		return !imageLink.isEmpty();
	}

	public JSONObject toJson() {
		JSONObject json = new JSONObject();

		json.put("guild_id", guildId.asString());
		json.put("event_id", eventId);
		json.put("event_end", eventEnd);
		json.put("image_link", imageLink);

		return json;
	}
}