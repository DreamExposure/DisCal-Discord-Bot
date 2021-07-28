package org.dreamexposure.discal.core.`object`.event

import discord4j.common.util.Snowflake
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.dreamexposure.discal.core.serializers.SnowflakeAsStringSerializer

@Serializable
data class EventData(
        @Serializable(with = SnowflakeAsStringSerializer::class)
        @SerialName("guild_id")
        val guildId: Snowflake = Snowflake.of(0),

        @SerialName("event_id")
        val eventId: String = "",

        @SerialName("calendar_number")
        val calendarNumber: Int = 1,

        @SerialName("event_end")
        val eventEnd: Long = 0,

        @SerialName("image_link")
        val imageLink: String = ""
) {
    fun shouldBeSaved(): Boolean {
        return this.imageLink.isNotEmpty()
    }
}
