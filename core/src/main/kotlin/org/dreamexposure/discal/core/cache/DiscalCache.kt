package org.dreamexposure.discal.core.cache

import discord4j.common.util.Snowflake
import org.dreamexposure.discal.core.`object`.GuildSettings
import java.util.concurrent.ConcurrentHashMap

object DiscalCache {
    val guildSettings: MutableMap<Snowflake, GuildSettings> = ConcurrentHashMap()

    fun invalidateAll() {
        guildSettings.clear()
    }
}
