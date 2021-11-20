package org.dreamexposure.discal.core.`object`

import discord4j.common.util.Snowflake
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.dreamexposure.discal.core.serializers.InstantAsStringSerializer
import org.dreamexposure.discal.core.serializers.SnowflakeAsStringSerializer
import java.time.Instant

@Serializable
data class StaticMessage(
        @SerialName("guild_id")
        @Serializable(with = SnowflakeAsStringSerializer::class)
        val guildId: Snowflake,

        @SerialName("message_id")
        @Serializable(with = SnowflakeAsStringSerializer::class)
        val messageId: Snowflake,
        val type: Type,
        @SerialName("last_update")
        @Serializable(with = InstantAsStringSerializer::class)
        val lastUpdate: Instant,
) {
    enum class Type(val value: Int) {
        CALENDAR_OVERVIEW(1);

        companion object {
            fun valueOf(type: Int): Type {
                return when (type) {
                    1 -> CALENDAR_OVERVIEW
                    else -> throw IllegalArgumentException("Unknown type: $type")
                }
            }
        }
    }
}
