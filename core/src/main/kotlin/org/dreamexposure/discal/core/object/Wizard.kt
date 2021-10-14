package org.dreamexposure.discal.core.`object`

import discord4j.common.util.Snowflake
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

class Wizard<T: Pre> {
    private val active = ConcurrentHashMap<Snowflake, T>()

    fun get(id: Snowflake): T? {
        val p = active[id]
        if (p != null) p.lastEdit = Instant.now()
        return p
    }

    fun start(pre: T): T? = active.put(pre.guildId, pre)

    fun remove(id: Snowflake): T? = active.remove(id)
}
