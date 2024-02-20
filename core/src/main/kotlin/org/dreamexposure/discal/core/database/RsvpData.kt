package org.dreamexposure.discal.core.database

import org.springframework.data.relational.core.mapping.Table

@Table("rsvp")
data class RsvpData(
    val guildId: Long,
    val eventId: String,
    val calendarNumber: Int,
    val eventEnd: Long,
    val goingOnTime: String,
    val goingLate: String,
    val notGoing: String,
    val undecided: String,
    val waitlist: String,
    val rsvpLimit: Int,
    val rsvpRole: Long?,
)
