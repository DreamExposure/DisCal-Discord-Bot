package org.dreamexposure.discal.core.object.web;

import org.dreamexposure.discal.core.object.GuildSettings;
import org.json.JSONObject;

import discord4j.core.object.entity.TextChannel;

/**
 * Created by Nova Fox on 1/6/18.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
public class WebChannel {
	private long id;
	private String name;

	private boolean discalChannel;

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

	//Setters
	public void setId(long _id) {
		id = _id;
	}

	public void setName(String _name) {
		name = _name;
	}

	public void setDiscalChannel(boolean _dc) {
		discalChannel = _dc;
	}

	//Functions
	public WebChannel fromChannel(TextChannel c, GuildSettings settings) {
		id = c.getId().asLong();
		name = c.getName();

		discalChannel = settings.getDiscalChannel().equalsIgnoreCase(String.valueOf(id));

		return this;
	}

	public JSONObject toJson() {
		JSONObject data = new JSONObject();

		data.put("id", String.valueOf(id));
		data.put("name", name);
		data.put("discal_channel", discalChannel);

		return data;
	}

	public WebChannel fromJson(JSONObject data) {
		id = Long.parseLong(data.getString("id"));
		name = data.getString("name");
		discalChannel = data.getBoolean("discal_channel");

		return this;
	}
}