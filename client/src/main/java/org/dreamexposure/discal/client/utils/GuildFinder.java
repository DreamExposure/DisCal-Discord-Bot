package org.dreamexposure.discal.client.utils;

import discord4j.core.ServiceMediator;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.util.Snowflake;
import org.dreamexposure.discal.client.DisCalClient;
import org.dreamexposure.discal.core.object.BotSettings;

import java.util.Optional;

public class GuildFinder {
	public static Optional<Guild> findGuild(Snowflake guildId) {
		if ((guildId.asLong() >> 22) % Integer.valueOf(BotSettings.SHARD_COUNT.get()) == DisCalClient.clientId()) {
			ServiceMediator med = DisCalClient.getClient().getServiceMediator();
			return med.getStateHolder().getGuildStore()
				.find(guildId.asLong())
				.blockOptional()
				.map(bean -> new Guild(med, bean));
		}

		return Optional.empty();
	}
}
