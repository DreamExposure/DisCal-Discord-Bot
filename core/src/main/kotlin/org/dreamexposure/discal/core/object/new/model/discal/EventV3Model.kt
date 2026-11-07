package org.dreamexposure.discal.core.`object`.new.model.discal

import discord4j.common.util.Snowflake
import org.dreamexposure.discal.core.enums.event.EventColor
import org.dreamexposure.discal.core.`object`.new.Event
import org.dreamexposure.discal.core.`object`.new.EventRecurrence
import java.time.Instant
import java.time.ZoneId

data class EventV3Model(
    val id: String,
    val guildId: Snowflake,
    val calendarNumber: Int,
    val name: String,
    val description: String,
    val location: String,
    val link: String,
    val color: EventColor,
    val start: Instant,
    val end: Instant,
    val recur: Boolean,
    val recurrence: EventRecurrence,
    val rrule: String,
    val image: String,
    val imageAlt: String,
    val timezone: ZoneId,

    val isOngoing: Boolean,
    val isOver: Boolean,
    val is24Hours: Boolean,
    val isAllDay: Boolean,
    val isMultiDay: Boolean,
) {
    constructor(event: Event): this(
        id = event.id,
        guildId = event.guildId,
        calendarNumber = event.calendarNumber,
        name = event.name,
        description = event.description,
        location = event.location,
        link = event.link,
        color = event.color,
        start = event.start,
        end = event.end,
        recur = event.recur,
        recurrence = event.recurrence,
        rrule = event.recurrence.asRRule(),
        image = event.image,
        imageAlt = event.imageAlt,
        timezone = event.timezone,

        isOngoing = event.isOngoing(),
        isOver = event.isOver(),
        is24Hours = event.is24Hours(),
        isAllDay = event.isAllDay(),
        isMultiDay = event.isMultiDay(),
    )
}