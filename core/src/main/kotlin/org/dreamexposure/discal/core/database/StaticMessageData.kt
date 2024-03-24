package org.dreamexposure.discal.core.database

import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("static_messages")
data class StaticMessageData(
    val guildId: Long,
    val messageId: Long,
    val channelId: Long,
    val type: Int,
    val lastUpdate: Instant,
    val scheduledUpdate: Instant,
    val calendarNumber: Int,
)
