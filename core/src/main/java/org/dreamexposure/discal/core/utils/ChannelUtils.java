package org.dreamexposure.discal.core.utils;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.GuildMessageChannel;
import reactor.core.publisher.Mono;

/**
 * Created by Nova Fox on 3/29/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class ChannelUtils {
    public static Mono<Boolean> channelExists(final String nameOrId, final MessageCreateEvent event) {
        final String name = nameOrId.replace("#", "");
        return event.getGuild()
            .flatMapMany(Guild::getChannels)
            .ofType(GuildMessageChannel.class)
            .filter(channel -> channel.getName().equalsIgnoreCase(name) || channel.getId().asString().equals(name))
            .hasElements();
    }

    public static Mono<Boolean> channelExists(final String nameOrId, final Guild guild) {
        final String name = nameOrId.replace("#", "");
        return guild.getChannels()
            .ofType(GuildMessageChannel.class)
            .filter(channel -> channel.getName().equalsIgnoreCase(name) || channel.getId().asString().equals(name))
            .hasElements();
    }

    public static Mono<GuildMessageChannel> getChannelFromNameOrId(final String nameOrId, final MessageCreateEvent event) {
        final String name = nameOrId.replace("#", "");
        return event.getGuild()
            .flatMapMany(Guild::getChannels)
            .ofType(GuildMessageChannel.class)
            .filter(channel -> channel.getName().equalsIgnoreCase(name) || channel.getId().asString().equals(name))
            .next();
    }

    public static Mono<GuildMessageChannel> getChannelFromNameOrId(final String nameOrId, final Guild guild) {
        final String name = nameOrId.replace("#", "");
        return guild.getChannels()
            .ofType(GuildMessageChannel.class)
            .filter(channel -> channel.getName().equalsIgnoreCase(name) || channel.getId().asString().equals(name))
            .next();
    }

    public static Mono<String> getChannelNameFromNameOrId(final String nameOrId, final Guild guild) {
        return getChannelFromNameOrId(nameOrId, guild)
            .map(GuildMessageChannel::getName);
    }
}