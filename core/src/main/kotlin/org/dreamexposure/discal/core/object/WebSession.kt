package org.dreamexposure.discal.core.`object`

import discord4j.common.util.Snowflake
import org.dreamexposure.discal.core.database.SessionData
import java.time.Instant
import java.time.temporal.ChronoUnit

data class WebSession(
    val token: String,

    val user: Snowflake,

    val expiresAt: Instant = Instant.now().plus(7, ChronoUnit.DAYS),

    val accessToken: String,

    val refreshToken: String,
) {
    constructor(data: SessionData) : this(
        token = data.token,
        user = Snowflake.of(data.userId),
        expiresAt = data.expiresAt,
        accessToken = data.accessToken,
        refreshToken = data.refreshToken,
    )
}
