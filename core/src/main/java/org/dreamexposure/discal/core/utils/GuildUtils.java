package org.dreamexposure.discal.core.utils;

import discord4j.common.util.Snowflake;
import org.dreamexposure.discal.Application;

/**
 * Created by Nova Fox on 11/6/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
public class GuildUtils {
    @SuppressWarnings("MagicNumber")
    public static int findShard(final Snowflake id) {
        return ((int) id.asLong() >> 22) % Application.getShardCount();
    }

    public static boolean isActive(final Snowflake id) {
        //TODO: Determine an accurate way to detect if a guild is still connected to DisCal
        return true;
    }
}
