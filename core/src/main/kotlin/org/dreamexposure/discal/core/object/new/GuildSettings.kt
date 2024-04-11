package org.dreamexposure.discal.core.`object`.new

import discord4j.common.util.Snowflake
import org.dreamexposure.discal.core.database.GuildSettingsData
import org.dreamexposure.discal.core.enums.time.TimeFormat
import org.dreamexposure.discal.core.extensions.asLocale
import org.dreamexposure.discal.core.extensions.asSnowflake
import org.dreamexposure.discal.core.extensions.asStringListFromDatabase
import java.util.*

data class GuildSettings(
    val guildId: Snowflake,

    val controlRole: String = "everyone",
    val patronGuild: Boolean = false,
    val devGuild: Boolean = false,
    val maxCalendars: Int = 1,
    val dmAnnouncements: Set<Snowflake> = setOf(),
    val locale: Locale = Locale.ENGLISH,
    @Deprecated("Should be removed since chat commands are no longer used")
    val prefix: String = "!",
    val eventKeepDuration: Boolean = false,

    val interfaceStyle: InterfaceStyle = InterfaceStyle()
) {
    constructor(data: GuildSettingsData): this(
        guildId = data.guildId.asSnowflake(),

        controlRole = data.controlRole,
        patronGuild = data.patronGuild,
        devGuild = data.devGuild,
        maxCalendars = data.maxCalendars,
        dmAnnouncements = data.dmAnnouncements.asStringListFromDatabase().map(Snowflake::of).toSet(),
        locale = data.lang.asLocale(),
        prefix = data.prefix,
        eventKeepDuration = data.eventKeepDuration,

        interfaceStyle = InterfaceStyle(
            timeFormat = TimeFormat.entries.first { it.value == data.timeFormat },
            announcementStyle = AnnouncementStyle.entries.first { it.value == data.announcementStyle },
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
