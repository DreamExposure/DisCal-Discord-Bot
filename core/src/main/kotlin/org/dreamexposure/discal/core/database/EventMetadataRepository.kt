package org.dreamexposure.discal.core.database

import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.r2dbc.repository.R2dbcRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface EventMetadataRepository : R2dbcRepository<EventMetadataData, Long> {
    fun existsByGuildIdAndEventId(guildId: Long, eventId: String): Mono<Boolean>

    fun findByGuildIdAndEventId(guildId: Long, eventId: String): Mono<EventMetadataData>

    fun findAllByGuildIdAndEventIdIn(guildId: Long, eventIds: Collection<String>): Flux<EventMetadataData>

    @Query("""
        UPDATE events
        SET calendar_number = :calendarNumber,
            event_end = :eventEnd,
            image_link = :imageLink
        WHERE guild_id = :guildId AND event_id = :eventId
    """)
    fun updateByGuildIdAndEventId(
        guildId: Long,
        eventId: String,
        calendarNumber: Int,
        eventEnd: Long,
        imageLink: String,
    ): Mono<Long>

    fun deleteByGuildIdAndEventId(guildId: Long, eventId: String): Mono<Void>

    fun deleteAllByGuildIdAndCalendarNumber(guildId: Long, calendarNumber: Int): Mono<Void>

    @Query("""
        UPDATE events
        SET calendar_number = calendar_number - 1
        WHERE calendar_number >= :calendarNumber AND guild_id = :guildId
    """)
    fun decrementCalendarsByGuildIdAndCalendarNumber(guildId: Long, calendarNumber: Int): Mono<Long>
}