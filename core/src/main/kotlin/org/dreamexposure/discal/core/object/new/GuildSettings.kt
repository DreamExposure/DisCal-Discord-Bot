package org.dreamexposure.discal.core.`object`.new

import discord4j.common.util.Snowflake
import org.dreamexposure.discal.core.database.GuildSettingsData
import org.dreamexposure.discal.core.enums.time.TimeFormat
import org.dreamexposure.discal.core.extensions.asLocale
import org.dreamexposure.discal.core.extensions.asSnowflake
import java.time.Instant
import java.util.*

data class GuildSettings(
    val guildId: Snowflake,

    val controlRole: Snowflake? = null,
    val patronGuild: Boolean = false,
    val devGuild: Boolean = false,
    val maxCalendars: Int = 1,
    val locale: Locale = Locale.ENGLISH,
    val eventKeepDuration: Boolean = false,
    val pauseAnnouncementsUntil: Instant? = null,

    val interfaceStyle: InterfaceStyle = InterfaceStyle()
) {
    constructor(data: GuildSettingsData): this(
        guildId = data.guildId.asSnowflake(),

        controlRole = if (data.controlRole.equals("everyone", true)) null else Snowflake.of(data.controlRole),
        patronGuild = data.patronGuild,
        devGuild = data.devGuild,
        maxCalendars = data.maxCalendars,
        locale = data.lang.asLocale(),
        eventKeepDuration = data.eventKeepDuration,
        pauseAnnouncementsUntil = data.pauseAnnouncementsUtil,

        interfaceStyle = InterfaceStyle(
            // Just in case there's weird data, I'm going to allow going back to the default value on a failure
            timeFormat = TimeFormat.entries.firstOrNull { it.value == data.timeFormat } ?: TimeFormat.TWENTY_FOUR_HOUR,
            announcementStyle = AnnouncementStyle.entries.firstOrNull { it.value == data.announcementStyle } ?: AnnouncementStyle.EVENT,
            branded = data.branded,
        )
    )

    ////////////////////////////
    ////// Nested classes //////
    ////////////////////////////
    data class InterfaceStyle(
        val timeFormat: TimeFormat = TimeFormat.TWENTY_FOUR_HOUR,
        val announcementStyle: AnnouncementStyle = AnnouncementStyle.EVENT,
        val branded: Boolean = false,
    )

    enum class AnnouncementStyle(val value: Int = 1) {
        FULL(1),
        SIMPLE(2),
        EVENT(3),
    }
}
