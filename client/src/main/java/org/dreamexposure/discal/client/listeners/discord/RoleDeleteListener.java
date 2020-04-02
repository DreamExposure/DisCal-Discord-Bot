package org.dreamexposure.discal.client.listeners.discord;

import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.object.GuildSettings;

import discord4j.core.event.domain.role.RoleDeleteEvent;
import discord4j.core.object.util.Snowflake;

public class RoleDeleteListener {
	public static void handle(RoleDeleteEvent event) {
		//Check if deleted channel is discal channel...
		GuildSettings settings = DatabaseManager.getManager().getSettings(event.getGuildId());

		if (settings.getControlRole().equalsIgnoreCase("everyone"))
			return;

		if (event.getRoleId().equals(Snowflake.of(settings.getControlRole()))) {
			settings.setControlRole("everyone");
			DatabaseManager.getManager().updateSettings(settings);
		}
	}
}