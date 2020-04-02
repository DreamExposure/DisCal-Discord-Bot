package org.dreamexposure.discal.client.listeners.discord;

import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.object.GuildSettings;

import discord4j.core.event.domain.channel.TextChannelDeleteEvent;
import discord4j.core.object.util.Snowflake;

public class ChannelDeleteListener {
	public static void handle(TextChannelDeleteEvent event) {
		//Check if deleted channel is discal channel...
		GuildSettings settings = DatabaseManager.getManager().getSettings(event.getChannel().getGuildId());

		if (settings.getDiscalChannel().equalsIgnoreCase("all"))
			return;

		if (event.getChannel().getId().equals(Snowflake.of(settings.getDiscalChannel()))) {
			settings.setDiscalChannel("all");
			DatabaseManager.getManager().updateSettings(settings);
		}
	}
}
