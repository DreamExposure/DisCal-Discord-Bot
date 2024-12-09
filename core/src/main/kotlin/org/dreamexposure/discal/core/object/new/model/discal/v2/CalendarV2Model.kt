package org.dreamexposure.discal.core.`object`.new.model.discal.v2

import com.fasterxml.jackson.annotation.JsonProperty
import discord4j.common.util.Snowflake
import org.dreamexposure.discal.core.`object`.new.Calendar
import java.time.ZoneId

@Deprecated("Prefer to use V3 APIs, this is for compatibility for the old site old")
data class CalendarV2Model(
    @JsonProperty("guild_id")
    val guildId: Snowflake,
    @JsonProperty("calendar_id")
    val calendarId: String,
    @JsonProperty("calendar_address")
    val calendarAddress: String,
    @JsonProperty("calendar_number")
    val calendarNumber: Int,
    val host: String,
    @JsonProperty("host_link")
    val hostLink: String,
    val external: Boolean,
    val name: String,
    val description: String,
    val timezone: ZoneId,
    val link: String,
) {
    constructor(calendar: Calendar): this(
        guildId = calendar.metadata.guildId,
        calendarId = calendar.metadata.id,
        calendarAddress = calendar.metadata.address,
        calendarNumber = calendar.metadata.number,
        host = calendar.metadata.host.name,
        hostLink = calendar.hostLink,
        external = calendar.metadata.external,
        name = calendar.name,
        description = calendar.description,
        timezone = calendar.timezone,
        link = calendar.link,
    )
}
