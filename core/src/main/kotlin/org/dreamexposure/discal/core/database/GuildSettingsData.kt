package org.dreamexposure.discal.core.database

import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("guild_settings")
data class GuildSettingsData(
    val guildId: Long,

    val controlRole: String,
    val timeFormat: Int,
    val patronGuild: Boolean,
    val devGuild : Boolean,
    val maxCalendars: Int,
    val lang: String,
    val branded: Boolean,
    val announcementStyle: Int,
    val eventKeepDuration: Boolean,
    val pauseAnnouncementsUntil: Instant?,
)
