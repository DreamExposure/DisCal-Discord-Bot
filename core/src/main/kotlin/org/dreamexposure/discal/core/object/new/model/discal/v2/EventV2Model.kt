package org.dreamexposure.discal.core.`object`.new.model.discal.v2

import com.fasterxml.jackson.annotation.JsonProperty
import discord4j.common.util.Snowflake
import org.dreamexposure.discal.core.`object`.event.Recurrence
import org.dreamexposure.discal.core.`object`.new.Calendar
import org.dreamexposure.discal.core.`object`.new.Event

@Deprecated("Prefer to use V3 API implementation. This exists to maintain compatibility for the legacy website")
data class EventV2Model(
    @JsonProperty("guild_id")
    val guildId: Snowflake,
    val calendar: CalendarV2Model,
    @JsonProperty("event_id")
    val eventId: String,
    @JsonProperty("epoch_start")
    val epochStart: Long,
    @JsonProperty("epoch_end")
    val epochEnd: Long,
    val name: String,
    val description: String,
    val location: String,
    @JsonProperty("is_parent")
    val isParent: Boolean,
    val color: String,
    val recur: Boolean,
    val recurrence: Recurrence,
    val rrule: String,
    val image: String,
) {
    constructor(event: Event, calendar: Calendar): this(
        guildId = event.guildId,
        calendar = CalendarV2Model(calendar),
        eventId = event.id,
        epochStart = event.start.toEpochMilli(),
        epochEnd = event.end.toEpochMilli(),
        name = event.name,
        description = event.description,
        location = event.location,
        isParent = !event.id.contains("_"),
        color = event.color.name,
        recur = event.recur,
        recurrence = event.recurrence,
        rrule = event.recurrence.toRRule(),
        image = event.image,
    )
}