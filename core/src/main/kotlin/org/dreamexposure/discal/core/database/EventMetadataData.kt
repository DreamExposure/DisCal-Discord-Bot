package org.dreamexposure.discal.core.database

import org.springframework.data.relational.core.mapping.Table

@Table("events")
data class EventMetadataData(
    val guildId: Long,
    val eventId: String,
    val calendarNumber: Int,
    val eventEnd: Long,
    val imageLink: String,
)
