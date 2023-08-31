package org.dreamexposure.discal.core.database

import org.springframework.data.relational.core.mapping.Table

@Table("calendars")
data class CalendarData(
    val guildId: Long,
    val calendarNumber: Int,
    val host: String,
    val calendarId: String,
    val calendarAddress: String,
    val external: Boolean,
    val credentialId: Int,
    val privateKey: String,
    val accessToken: String,
    val refreshToken: String,
    val expiresAt: Long,
)
