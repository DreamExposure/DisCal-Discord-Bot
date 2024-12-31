package org.dreamexposure.discal.core.`object`.new

import discord4j.common.util.Snowflake
import org.dreamexposure.discal.core.database.EventMetadataData
import org.dreamexposure.discal.core.extensions.asSnowflake
import java.time.Instant

data class EventMetadata(
    val id: String,
    val guildId: Snowflake,
    val calendarNumber: Int,
    val eventEnd: Instant,
    val imageLink: String,
) {
    constructor(data: EventMetadataData): this(
        id = data.eventId,
        guildId = data.guildId.asSnowflake(),
        calendarNumber = data.calendarNumber,
        eventEnd = Instant.ofEpochMilli(data.eventEnd),
        imageLink = data.imageLink,
    )

    constructor(id: String, guildId: Snowflake, calendarNumber: Int): this(
        id = id,
        guildId = guildId,
        calendarNumber = calendarNumber,
        eventEnd = Instant.now(),
        imageLink = "",
    )
}
