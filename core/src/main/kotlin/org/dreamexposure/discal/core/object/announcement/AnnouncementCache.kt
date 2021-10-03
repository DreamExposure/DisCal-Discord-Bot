package org.dreamexposure.discal.core.`object`.announcement

import discord4j.common.util.Snowflake
import org.dreamexposure.discal.core.entities.Calendar
import org.dreamexposure.discal.core.entities.Event
import reactor.core.publisher.Flux
import java.util.concurrent.ConcurrentHashMap

data class AnnouncementCache(
    val id: Snowflake,
    val calendars: ConcurrentHashMap<Int, Calendar> = ConcurrentHashMap(),
    val events: ConcurrentHashMap<Int, Flux<Event>> = ConcurrentHashMap(),
)
