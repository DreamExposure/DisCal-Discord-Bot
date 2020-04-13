package org.dreamexposure.discal.core.object.web;

import org.dreamexposure.discal.core.object.GuildSettings;
import org.json.JSONObject;

import discord4j.core.object.entity.channel.TextChannel;

/**
 * Created by Nova Fox on 1/6/18.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
public class WebChannel {


	public static WebChannel fromChannel(TextChannel c, GuildSettings settings) {
		boolean dc = settings.getDiscalChannel().equalsIgnoreCase(c.getId().asString());

		return new WebChannel(c.getId().asLong(), c.getName(), dc);
	}

	public static WebChannel fromJson(JSONObject json) {
		long id = Long.parseLong(json.getString("id"));
		String name = json.getString("name");
		boolean discalChannel = json.getBoolean("discal_channel");

		return new WebChannel(id, name, discalChannel);
	}

	public static WebChannel all(GuildSettings settings) {
		boolean dc = settings.getDiscalChannel().equalsIgnoreCase("all");

		return new WebChannel(0, "All Channels", dc);
	}

	private final long id;
	private final String name;

	private final boolean discalChannel;

	private WebChannel(long id, String name, boolean discalChannel) {
		this.id = id;
		this.name = name;
		this.discalChannel = discalChannel;
	}

	//Getters
	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public boolean isDiscalChannel() {
		return discalChannel;
	}

	public JSONObject toJson() {
		JSONObject data = new JSONObject();

		data.put("id", String.valueOf(id));
		data.put("name", name);
		data.put("discal_channel", discalChannel);

		return data;
	}
}