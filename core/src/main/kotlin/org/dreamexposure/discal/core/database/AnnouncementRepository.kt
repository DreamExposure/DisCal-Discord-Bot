package org.dreamexposure.discal.core.database

import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.r2dbc.repository.R2dbcRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface AnnouncementRepository: R2dbcRepository<AnnouncementData, String> {

    @Query("SELECT COUNT(*) FROM announcements")
    fun countAll(): Mono<Long>

    fun findByGuildIdAndAnnouncementId(guildId: Long, announcementId: String): Mono<AnnouncementData>

    fun findAllByGuildId(guildId: Long): Flux<AnnouncementData>

    fun findAllByGuildIdAndEnabledIsTrue(guildId: Long): Flux<AnnouncementData>

    fun findAllByGuildIdAndAnnouncementTypeAndEnabledIsTrue(guildId: Long, announcementType: String): Flux<AnnouncementData>

    @Query("""
        SELECT 
            announcement_id,
            calendar_number,
            guild_id,
            subscribers_role,
            subscribers_user,
            channel_id,
            announcement_type, 
            modifier,
            event_id, 
            event_color, 
            hours_before, 
            minutes_before, 
            info, 
            enabled, 
            publish
        FROM announcements
        WHERE MOD(guild_id >> 22, :shardCount) = :shardIndex
            AND ENABLED = 1
    """)
    fun findAllByShardIndexAndEnabledIsTrue(shardIndex: Int, shardCount: Int): Flux<AnnouncementData>

    @Query("""
        UPDATE announcements
        SET calendar_number = :calendarNumber,
            subscribers_role = :subscribersRole,
            subscribers_user = :subscribersUser,
            channel_id = :channelId,
            announcement_type = :announcementType,
            modifier = :modifier,
            event_id = :eventId,
            event_color = :eventColor,
            hours_before = :hoursBefore,
            minutes_before = :minutesBefore,
            info = :info,
            enabled = :enabled,
            publish = :publish
        WHERE guild_id = :guildId 
            AND announcement_id = :announcementId
    """)
    fun updateByGuildIdAndAnnouncementId(
        guildId: Long,
        announcementId: String,
        calendarNumber: Int,
        subscribersRole: String,
        subscribersUser: String,
        channelId: String,
        announcementType: String,
        modifier: String,
        eventId: String,
        eventColor: String,
        hoursBefore: Int,
        minutesBefore: Int,
        info: String,
        enabled: Boolean,
        publish: Boolean,
    ): Mono<Int>

    fun deleteByAnnouncementId(announcementId: String): Mono<Void>

    fun deleteAllByGuildIdAndEventId(guildId: Long, eventId: String): Mono<Void>

    fun deleteAllByGuildIdAndCalendarNumber(guildId: Long, calendarNumber: Int): Mono<Void>

    @Query("""
        UPDATE announcements
        SET calendar_number = calendar_number - 1
        WHERE calendar_number >= :calendarNumber AND guild_id = :guildId
    """)
    fun decrementCalendarsByGuildIdAndCalendarNumber(guildId: Long, calendarNumber: Int): Mono<Long>
}
