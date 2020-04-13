package org.dreamexposure.discal.client.listeners.discord;

import org.dreamexposure.discal.core.database.DatabaseManager;

import discord4j.core.event.domain.role.RoleDeleteEvent;
import discord4j.rest.util.Snowflake;
import reactor.core.publisher.Mono;

public class RoleDeleteListener {
	public static void handle(RoleDeleteEvent event) {
		DatabaseManager.getSettings(event.getGuildId()).flatMap(settings -> {
			if (settings.getControlRole().equalsIgnoreCase("everyone"))
				return Mono.empty();

			if (event.getRoleId().equals(Snowflake.of(settings.getControlRole()))) {
				settings.setControlRole("everyone");
				return DatabaseManager.updateSettings(settings);
			}

			return Mono.empty();
		}).subscribe();
	}
}