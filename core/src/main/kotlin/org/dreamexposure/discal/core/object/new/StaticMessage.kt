package org.dreamexposure.discal.core.`object`.new

import discord4j.common.util.Snowflake
import org.dreamexposure.discal.core.database.StaticMessageData
import org.dreamexposure.discal.core.extensions.asSnowflake
import java.time.Instant

data class StaticMessage(
    val guildId: Snowflake,
    val messageId: Snowflake,
    val channelId: Snowflake,

    val type: Type,

    val lastUpdate: Instant,
    val scheduledUpdate: Instant,

    val calendarNumber: Int
) {
    constructor(data: StaticMessageData): this(
        guildId = data.guildId.asSnowflake(),
        messageId = data.messageId.asSnowflake(),
        channelId = data.channelId.asSnowflake(),

        type = Type.getByValue(data.type),

        lastUpdate = data.lastUpdate,
        scheduledUpdate = data.scheduledUpdate,

        calendarNumber = data.calendarNumber,
    )



    enum class Type(val value: Int) {
        CALENDAR_OVERVIEW(1);

        companion object {
            fun getByValue(value: Int) = entries.first { it.value == value }
        }
    }
}
