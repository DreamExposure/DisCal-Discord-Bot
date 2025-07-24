package org.dreamexposure.discal.core.database

import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.r2dbc.repository.R2dbcRepository
import reactor.core.publisher.Mono
import java.time.Instant

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
            lang = :lang,
            branded = :branded,
            announcement_style = :announcementStyle,
            event_keep_duration = :eventKeepDuration,
            show_rsvp_dropdown = :showRsvpDropdown,
            pause_announcements_until = :pauseAnnouncementsUntil
        WHERE guild_id = :guildId
    """)
    fun updateByGuildId(
        guildId: Long,

        controlRole: String,
        timeFormat: Int,
        patronGuild: Boolean,
        devGuild: Boolean,
        maxCalendars: Int,
        lang: String,
        branded: Boolean,
        announcementStyle: Int,
        eventKeepDuration: Boolean,
        showRsvpDropdown: Boolean,
        pauseAnnouncementsUntil: Instant?,
    ): Mono<Int>
}
