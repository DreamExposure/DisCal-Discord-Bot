package org.dreamexposure.discal.core.`object`.new.model.discal

import discord4j.common.util.Snowflake
import org.dreamexposure.discal.core.`object`.new.Calendar
import org.dreamexposure.discal.core.`object`.new.CalendarMetadata
import java.time.ZoneId

data class CalendarV3Model(
    val number: Int,
    val guildId: Snowflake,

    val name: String,
    val description: String,
    val timezone: ZoneId,
    val link: String,

    val host: CalendarMetadata.Host,
    val hostLink: String,
    val external: Boolean,
) {
    constructor(calendar: Calendar): this(
        number = calendar.metadata.number,
        guildId = calendar.metadata.guildId,

        name = calendar.name,
        description = calendar.description,
        timezone = calendar.timezone,
        link = calendar.link,

        host = calendar.metadata.host,
        hostLink = calendar.hostLink,
        external = calendar.metadata.external,
    )
}