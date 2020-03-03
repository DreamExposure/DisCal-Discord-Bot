package org.dreamexposure.discal.client.utils;

import org.dreamexposure.discal.client.DisCalClient;
import org.dreamexposure.discal.core.object.BotSettings;

import java.util.Optional;

import discord4j.core.ServiceMediator;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.util.Snowflake;

public class GuildFinder {
	public static Optional<Guild> findGuild(Snowflake guildId) {
		if ((guildId.asLong() >> 22) % Integer.parseInt(BotSettings.SHARD_COUNT.get()) == DisCalClient.clientId()) {
			ServiceMediator med = DisCalClient.getClient().getServiceMediator();
			return med.getStateHolder().getGuildStore()
					.find(guildId.asLong())
					.blockOptional()
					.map(bean -> new Guild(med, bean));
		}

		return Optional.empty();
	}

	public static Optional<Guild> getGuild(Snowflake guildId) {
		ServiceMediator med = DisCalClient.getClient().getServiceMediator();
		return med.getStateHolder().getGuildStore()
				.find(guildId.asLong())
				.blockOptional()
				.map(bean -> new Guild(med, bean));
	}
}
