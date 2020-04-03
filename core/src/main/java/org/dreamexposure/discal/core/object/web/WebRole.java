package org.dreamexposure.discal.core.object.web;

import org.dreamexposure.discal.core.object.GuildSettings;
import org.json.JSONObject;

import discord4j.core.object.entity.Role;

/**
 * Created by Nova Fox on 1/6/18.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
public class WebRole {
	public static WebRole fromRole(Role r, GuildSettings settings) {
		boolean controlRole;
		if (r.isEveryone() && settings.getControlRole().equalsIgnoreCase("everyone"))
			controlRole = true;
		else
			controlRole = settings.getControlRole().equalsIgnoreCase(r.getId().asString());


		return new WebRole(r.getId().asLong(), r.getName(), r.isManaged(), controlRole, r.isEveryone());
	}

	public static WebRole fromJson(JSONObject data) {
		long id = Long.parseLong(data.getString("id"));
		String name = data.getString("name");
		boolean managed = data.getBoolean("managed");
		boolean controlRole = data.getBoolean("control_role");
		boolean everyone = data.getBoolean("everyone");

		return new WebRole(id, name, managed, controlRole, everyone);
	}


	private final long id;
	private final String name;

	private final boolean managed;
	private final boolean controlRole;

	private final boolean everyone;

	private WebRole(long id, String name, boolean managed, boolean controlRole, boolean everyone) {
		this.id = id;
		this.name = name;
		this.managed = managed;
		this.controlRole = controlRole;
		this.everyone = everyone;
	}

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

	public boolean isEveryone() {
		return everyone;
	}

	//functions
	public JSONObject toJson() {
		JSONObject data = new JSONObject();

		data.put("id", String.valueOf(id));
		data.put("name", name);
		data.put("managed", managed);
		data.put("control_role", controlRole);
		data.put("everyone", everyone);

		return data;
	}
}