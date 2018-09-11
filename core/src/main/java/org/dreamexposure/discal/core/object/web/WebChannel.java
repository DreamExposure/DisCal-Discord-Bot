package org.dreamexposure.discal.core.object.web;

import discord4j.core.object.entity.TextChannel;
import org.dreamexposure.discal.core.object.GuildSettings;

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
}