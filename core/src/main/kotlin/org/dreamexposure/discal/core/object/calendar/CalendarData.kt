package org.dreamexposure.discal.core.`object`.calendar

import discord4j.common.util.Snowflake
import kotlinx.serialization.Serializable
import org.dreamexposure.discal.core.serializers.SnowflakeAsStringSerializer

@Serializable
data class CalendarData(
        @Serializable(with = SnowflakeAsStringSerializer::class)
        val guildId: Snowflake = Snowflake.of(0),
        val calendarNumber: Int = 0,
        val calendarId: String = "primary",
        val calendarAddress: String = "primary",
        val external: Boolean = false,
        val credentialId: Int = 0,
)
