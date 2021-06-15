package org.dreamexposure.discal.client.listeners.discord;

import org.dreamexposure.discal.core.database.DatabaseManager;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.channel.TextChannelDeleteEvent;
import reactor.core.publisher.Mono;

public class ChannelDeleteListener {
    public static Mono<Void> handle(final TextChannelDeleteEvent event) {
        //Check if deleted channel is discal channel...
        return DatabaseManager.INSTANCE.getSettings(event.getChannel().getGuildId())
            .filter(settings -> !"all".equalsIgnoreCase(settings.getDiscalChannel()))
            .filter(settings -> event.getChannel().getId().equals(Snowflake.of(settings.getDiscalChannel())))
            .doOnNext(settings -> settings.setDiscalChannel("all"))
            .flatMap(DatabaseManager.INSTANCE::updateSettings)
            .then();
    }
}
