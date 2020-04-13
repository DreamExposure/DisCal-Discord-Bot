package org.dreamexposure.discal.client.listeners.discord;

import org.dreamexposure.discal.core.database.DatabaseManager;

import discord4j.core.event.domain.channel.TextChannelDeleteEvent;
import discord4j.rest.util.Snowflake;
import reactor.core.publisher.Mono;

public class ChannelDeleteListener {
    public static Mono<Void> handle(TextChannelDeleteEvent event) {
        //Check if deleted channel is discal channel...
        return DatabaseManager.getSettings(event.getChannel().getGuildId())
                .filter(settings -> !settings.getDiscalChannel().equalsIgnoreCase("all"))
                .filter(settings -> event.getChannel().getId().equals(Snowflake.of(settings.getDiscalChannel())))
                .doOnNext(settings -> settings.setDiscalChannel("all"))
                .flatMap(DatabaseManager::updateSettings)
                .then();
    }
}
