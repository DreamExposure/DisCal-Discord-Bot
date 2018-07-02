package com.cloudcraftgaming.discal.api.object.web;

import com.cloudcraftgaming.discal.api.object.GuildSettings;
import sx.blah.discord.handle.obj.IRole;

/**
 * Created by Nova Fox on 1/6/18.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
public class WebRole {
	private long id;
	private String name;

	private boolean managed;
	private boolean controlRole;


	//Getters
	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public boolean isManaged() {
		return managed;
	}

	public boolean isControlRole() {
		return controlRole;
	}

	//Setters
	public void setId(long _id) {
		id = _id;
	}

	public void setName(String _name) {
		name = _name;
	}

	public void setManaged(boolean _managed) {
		managed = _managed;
	}

	public void setControlRole(boolean _control) {
		controlRole = _control;
	}

	//functions
	public WebRole fromRole(IRole r, GuildSettings settings) {
		id = r.getLongID();
		name = r.getName();

		managed = r.isManaged();

		if (r.isEveryoneRole() && settings.getControlRole().equalsIgnoreCase("everyone"))
			controlRole = true;
		else
			controlRole = settings.getControlRole().equalsIgnoreCase(String.valueOf(id));


		return this;
	}
}