package org.dreamexposure.discal.core.`object`.web

import discord4j.common.util.Snowflake
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.dreamexposure.discal.core.enums.calendar.CalendarHost
import org.dreamexposure.discal.core.enums.time.TimeFormat
import org.dreamexposure.discal.core.serializers.SnowflakeAsStringSerializer

@Serializable
data class WebCalendar internal constructor(
    @SerialName("guild_id")
    @Serializable(with = SnowflakeAsStringSerializer::class)
    val guildId: Snowflake,
    val id: String = "primary",
    val address: String = "primary",
    val number: Int = 1,
    val host: CalendarHost = CalendarHost.GOOGLE,
    val link: String = "",
    @SerialName("host_link")
    val hostLink: String = "",
    val name: String = "N/a",
    val description: String = "",
    val timezone: String = "",
    val external: Boolean = false,
    val timeFormat: TimeFormat = TimeFormat.TWELVE_HOUR,
)
