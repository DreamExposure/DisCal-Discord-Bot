package org.dreamexposure.discal.core.database

import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.r2dbc.repository.R2dbcRepository
import reactor.core.publisher.Mono

interface GuildSettingsRepository : R2dbcRepository<GuildSettingsData, Long> {

    fun existsByGuildId(guildId: Long): Mono<Boolean>

    fun findByGuildId(guildId: Long): Mono<GuildSettingsData>

    @Query("""
        UPDATE guild_settings
        SET control_role = :controlRole,
            time_format = :timeFormat,
            patron_guild = :patronGuild,
            dev_guild = :devGuild,
            max_calendars = :maxCalendars,
            dm_announcements = :dmAnnouncements,
            lang = :lang,
            prefix = :prefix,
            branded = :branded,
            announcement_style = :announcementStyle,
            event_keep_duration = :eventKeepDuration
        WHERE guild_id = :guildId
    """)
    fun updateByGuildId(
        guildId: Long,

        controlRole: String,
        timeFormat: Int,
        patronGuild: Boolean,
        devGuild: Boolean,
        maxCalendars: Int,
        dmAnnouncements: String,
        lang: String,
        prefix: String,
        branded: Boolean,
        announcementStyle: Int,
        eventKeepDuration: Boolean,
    ): Mono<Int>
}
