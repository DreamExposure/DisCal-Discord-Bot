package org.dreamexposure.discal.core.utils;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.TextChannel;
import reactor.core.publisher.Mono;

/**
 * Created by Nova Fox on 3/29/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
@SuppressWarnings("ConstantConditions")
public class ChannelUtils {
    public static Mono<Boolean> channelExists(String nameOrId, MessageCreateEvent event) {
        final String name = nameOrId.replace("#", "");
        return event.getGuild()
            .flatMapMany(Guild::getChannels)
            .ofType(TextChannel.class)
            .filter(c -> c.getName().equalsIgnoreCase(name) || c.getId().asString().equals(name))
            .hasElements();
    }

    public static Mono<Boolean> channelExists(String nameOrId, Guild guild) {
        final String name = nameOrId.replace("#", "");
        return guild.getChannels()
            .ofType(TextChannel.class)
            .filter(c -> c.getName().equalsIgnoreCase(name) || c.getId().asString().equals(name))
            .hasElements();
    }

    public static Mono<TextChannel> getChannelFromNameOrId(String nameOrId, MessageCreateEvent event) {
        final String name = nameOrId.replace("#", "");
        return event.getGuild()
            .flatMapMany(Guild::getChannels)
            .ofType(TextChannel.class)
            .filter(c -> c.getName().equalsIgnoreCase(name) || c.getId().asString().equals(name))
            .next();
    }

    public static Mono<TextChannel> getChannelFromNameOrId(String nameOrId, Guild guild) {
        final String name = nameOrId.replace("#", "");
        return guild.getChannels()
            .ofType(TextChannel.class)
            .filter(c -> c.getName().equalsIgnoreCase(name) || c.getId().asString().equals(name))
            .next();
    }

    public static Mono<String> getChannelNameFromNameOrId(String nameOrId, Guild guild) {
        return getChannelFromNameOrId(nameOrId, guild)
            .map(TextChannel::getName);
    }
}