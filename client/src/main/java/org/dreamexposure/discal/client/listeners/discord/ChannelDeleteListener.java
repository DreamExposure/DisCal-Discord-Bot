package org.dreamexposure.discal.client.listeners.discord;

import org.dreamexposure.discal.core.database.DatabaseManager;

import discord4j.core.event.domain.channel.TextChannelDeleteEvent;
import discord4j.core.object.util.Snowflake;
import reactor.core.publisher.Mono;

public class ChannelDeleteListener {
	public static void handle(TextChannelDeleteEvent event) {
		//Check if deleted channel is discal channel...

		DatabaseManager.getSettings(event.getChannel().getGuildId()).flatMap(settings -> {
			if (settings.getDiscalChannel().equalsIgnoreCase("all"))
				return Mono.empty();

			if (event.getChannel().getId().equals(Snowflake.of(settings.getDiscalChannel()))) {
				settings.setDiscalChannel("all");
				return DatabaseManager.updateSettings(settings);
			}

			return Mono.empty();
		}).subscribe();
	}
}
