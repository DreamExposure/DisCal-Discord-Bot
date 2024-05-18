package org.dreamexposure.discal.core.database

import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.r2dbc.repository.R2dbcRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface CalendarMetadataRepository : R2dbcRepository<CalendarMetadataData, Long> {

    @Query("SELECT COUNT(*) FROM calendars")
    fun countAll(): Mono<Long>

    fun countAllByGuildId(guildId: Long): Mono<Long>

    fun findAllByGuildId(guildId: Long): Flux<CalendarMetadataData>

    fun findByGuildIdAndCalendarNumber(guildId: Long, calendarNumber: Int): Mono<CalendarMetadataData>

    @Query("""
        UPDATE calendars
        SET host = :host,
            calendar_id = :calendarId,
            calendar_address = :calendarAddress,
            external = :external,
            credential_id = :credentialId,
            private_key = :privateKey,
            access_token = :accessToken,
            refresh_token = :refreshToken,
            expires_at = :expiresAt
        WHERE guild_id = :guildId AND calendar_number = :calendarNumber
    """)
    fun updateCalendarByGuildIdAndCalendarNumber(
        guildId: Long,
        calendarNumber: Int,
        host: String,
        calendarId: String,
        calendarAddress: String,
        external: Boolean,
        credentialId: Int,
        privateKey: String,
        accessToken: String,
        refreshToken: String,
        expiresAt: Long,
    ): Mono<Int>
}
