package org.dreamexposure.discal.core.`object`

import discord4j.common.util.Snowflake
import reactor.core.publisher.Flux
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.ConcurrentHashMap

class Wizard<T: Pre> {
    private val active = ConcurrentHashMap<Snowflake, T>()

    init {
        Flux.interval(Duration.ofMinutes(30))
                .map { removeOld() }
                .subscribe()
    }

    fun get(id: Snowflake): T? {
        val p = active[id]
        if (p != null) p.lastEdit = Instant.now()
        return p
    }

    fun start(pre: T): T? = active.put(pre.guildId, pre)

    fun remove(id: Snowflake): T? = active.remove(id)

    private fun removeOld() {
        val toRemove = mutableListOf<Snowflake>()

        active.forEach {
            if (Instant.now().isAfter(it.value.lastEdit.plus(30, ChronoUnit.MINUTES))) {
                toRemove.add(it.key)
            }
        }
        toRemove.forEach { active.remove(it) }
    }
}
