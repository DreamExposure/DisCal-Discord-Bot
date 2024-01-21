package org.dreamexposure.discal.core.database

import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("sessions")
data class SessionData(
    val token: String,
    val userId: Long,
    val expiresAt: Instant,
    val accessToken: String,
    val refreshToken: String,
    val scopes: String,
)
