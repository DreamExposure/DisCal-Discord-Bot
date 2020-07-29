package org.dreamexposure.discal.core.utils;

import org.dreamexposure.discal.core.object.BotSettings;

import discord4j.common.util.Snowflake;

/**
 * Created by Nova Fox on 11/6/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
public class GuildUtils {
    public static int findShard(Snowflake id) {
        return ((int) id.asLong() >> 22) % Integer.parseInt(BotSettings.SHARD_COUNT.get());
    }

    public static boolean active(Snowflake id) {
        //TODO: Determine an accurate way to detect if a guild is still connected to DisCal
        return true;
    }
}