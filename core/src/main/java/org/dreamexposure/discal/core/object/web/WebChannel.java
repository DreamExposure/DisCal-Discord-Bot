package org.dreamexposure.discal.core.object.web;

import discord4j.core.object.entity.channel.GuildMessageChannel;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.json.JSONObject;

/**
 * Created by Nova Fox on 1/6/18.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
public class WebChannel {


    public static WebChannel fromChannel(final GuildMessageChannel channel, final GuildSettings settings) {
        final boolean dc = settings.getDiscalChannel().equalsIgnoreCase(channel.getId().asString());

        return new WebChannel(channel.getId().asLong(), channel.getName(), dc);
    }

    public static WebChannel fromJson(final JSONObject json) {
        final long id = Long.parseLong(json.getString("id"));
        final String name = json.getString("name");
        final boolean discalChannel = json.getBoolean("discal_channel");

        return new WebChannel(id, name, discalChannel);
    }

    public static WebChannel all(final GuildSettings settings) {
        final boolean dc = "all".equalsIgnoreCase(settings.getDiscalChannel());

        return new WebChannel(0, "All Channels", dc);
    }

    private final long id;
    private final String name;

    private final boolean discalChannel;

    private WebChannel(final long id, final String name, final boolean discalChannel) {
        this.id = id;
        this.name = name;
        this.discalChannel = discalChannel;
    }

    //Getters
    public long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public boolean isDiscalChannel() {
        return this.discalChannel;
    }

    public JSONObject toJson() {
        final JSONObject data = new JSONObject();

        data.put("id", String.valueOf(this.id));
        data.put("name", this.name);
        data.put("discal_channel", this.discalChannel);

        return data;
    }
}