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
    val forcedUpdate: Instant?,
    val enabled: Boolean,

    val calendarNumber: Int
) {
    constructor(data: StaticMessageData): this(
        guildId = data.guildId.asSnowflake(),
        messageId = data.messageId.asSnowflake(),
        channelId = data.channelId.asSnowflake(),

        type = Type.getByValue(data.type),

        lastUpdate = data.lastUpdate,
        scheduledUpdate = data.scheduledUpdate,
        forcedUpdate = data.forcedUpdate,
        enabled = data.enabled,

        calendarNumber = data.calendarNumber,
    )



    enum class Type(val value: Int) {
        CALENDAR_OVERVIEW(1),
        CALENDAR_WEEKLY(2),
        NEXT_EVENT(3),
        NEXT_EVENT_WITH_RSVP(4),

        ONGOING_EVENTS(5),
        ;

        fun isEventSpecific() = this == NEXT_EVENT || this == NEXT_EVENT_WITH_RSVP

        companion object {
            fun getByValue(value: Int) = entries.first { it.value == value }
        }
    }
}
