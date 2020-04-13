package org.dreamexposure.discal.core.utils;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.TextChannel;

/**
 * Created by Nova Fox on 3/29/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
@SuppressWarnings("ConstantConditions")
public class ChannelUtils {
    /**
     * Checks if the specified channel exists.
     *
     * @param nameOrId The channel name or ID.
     * @param event    The event received.
     * @return <code>true</code> if exists, else <code>false</code>.
     */
    public static boolean channelExists(String nameOrId, MessageCreateEvent event) {
        if (nameOrId.contains("#"))
            nameOrId = nameOrId.replace("#", "");

        for (TextChannel c : event.getGuild().block().getChannels().ofType(TextChannel.class).toIterable()) {
            if (c.getName().equalsIgnoreCase(nameOrId) || c.getId().asString().equals(nameOrId))
                return true;
        }
        return false;
    }

    public static boolean channelExists(String nameOrId, Guild guild) {
        if (nameOrId.contains("#"))
            nameOrId = nameOrId.replace("#", "");

        for (TextChannel c : guild.getChannels().ofType(TextChannel.class).toIterable()) {
            if (c.getName().equalsIgnoreCase(nameOrId) || c.getId().asString().equals(nameOrId))
                return true;
        }
        return false;
    }

    /**
     * Gets the IChannel from its name.
     *
     * @param nameOrId The channel name or ID.
     * @param event    The event received.
     * @return the IChannel if successful, else <code>null</code>.
     */
    public static TextChannel getChannelFromNameOrId(String nameOrId, MessageCreateEvent event) {
        if (nameOrId.contains("#"))
            nameOrId = nameOrId.replace("#", "");

        for (TextChannel c : event.getGuild().block().getChannels().ofType(TextChannel.class).toIterable()) {
            if (c.getName().equalsIgnoreCase(nameOrId) || c.getId().asString().equals(nameOrId))
                return c;
        }
        return null;
    }

    /**
     * Gets the IChannel from its name.
     *
     * @param nameOrId The channel name or ID.
     * @return the IChannel if successful, else <code>null</code>.
     */
    public static TextChannel getChannelFromNameOrId(String nameOrId, Guild guild) {
        if (nameOrId.contains("#"))
            nameOrId = nameOrId.replace("#", "");

        for (TextChannel c : guild.getChannels().ofType(TextChannel.class).toIterable()) {
            if (c.getName().equalsIgnoreCase(nameOrId) || c.getId().asString().equals(nameOrId))
                return c;
        }
        return null;
    }

    /**
     * Gets the IChannel from its name.
     *
     * @param nameOrId The channel name or ID.
     * @return the IChannel if successful, else <code>null</code>.
     */
    public static String getChannelNameFromNameOrId(String nameOrId, Guild guild) {
        if (nameOrId.contains("#"))
            nameOrId = nameOrId.replace("#", "");

        for (TextChannel c : guild.getChannels().ofType(TextChannel.class).toIterable()) {
            if (c.getName().equalsIgnoreCase(nameOrId) || c.getId().asString().equals(nameOrId))
                return c.getName();
        }
        return "ERROR";
    }
}