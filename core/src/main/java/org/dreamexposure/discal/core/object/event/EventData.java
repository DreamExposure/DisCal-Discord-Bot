package org.dreamexposure.discal.core.object.event;

import org.json.JSONObject;

import discord4j.common.util.Snowflake;

/**
 * Created by Nova Fox on 11/10/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
public class EventData {
    public static EventData fromJson(final JSONObject json) {
        return new EventData(
            Snowflake.of(json.getString("guild_id")),
            json.getString("event_id"),
            json.getLong("event_end"),
            json.getString("image_link")
        );
    }

    public static EventData fromImage(final Snowflake guildId, final String eventId, final long eventEnd,
                                      final String imageLink) {
        return new EventData(guildId, eventId, eventEnd, imageLink);
    }

    public static EventData empty() {
        return new EventData(Snowflake.of(0), "", 0, "");
    }

    private final Snowflake guildId;
    private final String eventId;
    private final long eventEnd;
    private final String imageLink;

    private EventData(final Snowflake guildId, final String eventId, final long eventEnd, final String imageLink) {
        this.guildId = guildId;
        this.eventId = eventId;
        this.eventEnd = eventEnd;
        this.imageLink = imageLink;
    }

    //Getters
    public Snowflake getGuildId() {
        return this.guildId;
    }

    public String getEventId() {
        return this.eventId;
    }

    public long getEventEnd() {
        return this.eventEnd;
    }

    public String getImageLink() {
        return this.imageLink;
    }

    //Boolean/Checkers
    public boolean shouldBeSaved() {
        return !this.imageLink.isEmpty();
    }

    public JSONObject toJson() {
        final JSONObject json = new JSONObject();

        json.put("guild_id", this.guildId.asString());
        json.put("event_id", this.eventId);
        json.put("event_end", this.eventEnd);
        json.put("image_link", this.imageLink);

        return json;
    }
}