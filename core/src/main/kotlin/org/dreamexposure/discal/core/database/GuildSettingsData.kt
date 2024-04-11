package org.dreamexposure.discal.core.database

import org.springframework.data.relational.core.mapping.Table

@Table("guild_settings")
data class GuildSettingsData(
    val guildId: Long,

    val controlRole: String,
    val timeFormat: Int,
    val patronGuild: Boolean,
    val devGuild : Boolean,
    val maxCalendars: Int,
    val dmAnnouncements: String,
    val lang: String,
    val prefix: String,
    val branded: Boolean,
    val announcementStyle: Int,
    val eventKeepDuration: Boolean,
)
