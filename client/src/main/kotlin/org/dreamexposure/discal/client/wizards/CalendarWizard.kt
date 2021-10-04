package org.dreamexposure.discal.client.wizards

import discord4j.common.util.Snowflake
import org.dreamexposure.discal.core.`object`.calendar.PreCalendar
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

@Component
class CalendarWizard {
    init {
        Flux.interval(Duration.ofHours(1)).map {
            val toRemove = mutableListOf<Snowflake>()
            val expireTime = Duration.ofMinutes(30).toMillis()

            for (wizard in active.values) {
                if (wizard.lastEdit.minusMillis(System.currentTimeMillis()).toEpochMilli() < expireTime) {
                    // Hasn't been touched in 30+ minutes, auto cancel
                    toRemove += wizard.guildId
                }
            }
            toRemove.forEach { active.remove(it) }
        }.subscribe()
    }

    private val active = ConcurrentHashMap<Snowflake, PreCalendar>()

    fun get(id: Snowflake): PreCalendar? = active[id]

    fun start(pre: PreCalendar): PreCalendar? = active.put(pre.guildId, pre)

    fun remove(id: Snowflake): PreCalendar? = active.remove(id)
}
