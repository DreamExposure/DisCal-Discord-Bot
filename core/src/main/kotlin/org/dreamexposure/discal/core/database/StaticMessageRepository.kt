package org.dreamexposure.discal.core.database

import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.r2dbc.repository.R2dbcRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant

interface StaticMessageRepository: R2dbcRepository<StaticMessageData, Long> {
    fun existsByGuildIdAndMessageId(guildId: Long, messageId: Long): Mono<Boolean>

    fun findByGuildIdAndMessageId(guildId: Long, messageId: Long): Mono<StaticMessageData>

    fun findAllByGuildIdAndCalendarNumber(guildId: Long, calendarNumber: Int): Flux<StaticMessageData>

    @Query("""
        SELECT guild_id,
            message_id,
            channel_id,
            type,
            last_update,
            scheduled_update,
            calendar_number
        FROM static_messages
        WHERE MOD(guild_id >> 22, :shardCount) = :shardIndex
    """)
    fun findAllByShardIndex(shardIndex: Int, shardCount: Int): Flux<StaticMessageData>

    @Query("""
    UPDATE static_messages
    SET channel_id = :channelId,
        type = :type,
        last_update = :lastUpdate,
        scheduled_update = :scheduledUpdate,
        calendar_number = :calendarNumber
    WHERE guild_id = :guildId AND message_id = :messageId
    """)
    fun updateByGuildIdAndMessageId(
        guildId: Long,
        messageId: Long,
        channelId: Long,
        type: Int,
        lastUpdate: Instant,
        scheduledUpdate: Instant,
        calendarNumber: Int,
    ): Mono<Int>

    fun deleteByGuildIdAndMessageId(guildId: Long, messageId: Long): Mono<Void>
}
