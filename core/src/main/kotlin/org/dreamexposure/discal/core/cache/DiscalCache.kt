package org.dreamexposure.discal.core.cache

import discord4j.common.util.Snowflake
import org.dreamexposure.discal.core.`object`.GuildSettings
import org.dreamexposure.discal.core.entities.Calendar
import reactor.core.publisher.Flux
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

//TODO: Eventually use redis instead of in-memory so these can be shared across the whole discal network and need less time for eventual consistency.
object DiscalCache {
    //guild id -> settings
    val guildSettings: MutableMap<Snowflake, GuildSettings> = ConcurrentHashMap()
    //guild id -> cal num -> calendar
    private val calendars: MutableMap<Snowflake, ConcurrentHashMap<Int, Calendar>> = ConcurrentHashMap()

    init {
        //Automatically clear caches every so often...
        Flux.interval(Duration.ofMinutes(15))
            .doOnEach { invalidateAll() }
            .subscribe()
    }


    fun invalidateAll() {
        guildSettings.clear()
        calendars.clear()
    }

    //Functions to stop direct modification
    fun getCalendar(guildId: Snowflake, calNum: Int): Calendar? = calendars[guildId]?.get(calNum)

    fun getAllCalendars(guildId: Snowflake): Collection<Calendar>? = calendars[guildId]?.values

    fun putCalendar(calendar: Calendar) {
        if (calendars.containsKey(calendar.guildId))
            calendars[calendar.guildId]!![calendar.calendarNumber] = calendar
        else {
            val map = ConcurrentHashMap<Int, Calendar>()
            map[calendar.calendarNumber] = calendar

            calendars[calendar.guildId] = map
        }
    }

    fun removeCalendar(guildId: Snowflake, calNum: Int) {
        if (calendars.containsKey(guildId))
            calendars[guildId]!!.remove(calNum)
    }
}
