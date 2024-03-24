package org.dreamexposure.discal.core.database

import org.springframework.data.relational.core.mapping.Table

@Table("announcements")
data class AnnouncementData(
    val announcementId: String,
    val calendarNumber: Int,
    val guildId: Long,
    val subscribersRole: String,
    val subscribersUser: String,
    val channelId: String,
    val announcementType: String,
    val modifier: String,
    val eventId: String,
    val eventColor: String,
    val hoursBefore: Int,
    val minutesBefore: Int,
    val info: String,
    val enabled: Boolean,
    val publish: Boolean,
)
