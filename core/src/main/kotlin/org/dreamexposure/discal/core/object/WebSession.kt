package org.dreamexposure.discal.core.`object`

import discord4j.common.util.Snowflake
import org.dreamexposure.discal.core.database.SessionData
import org.dreamexposure.discal.core.extensions.asSnowflake
import org.dreamexposure.discal.core.extensions.asStringListFromDatabase
import org.dreamexposure.discal.core.`object`.new.security.Scope
import java.time.Instant
import java.time.temporal.ChronoUnit

data class WebSession(
    val token: String,
    val user: Snowflake,
    val expiresAt: Instant = Instant.now().plus(7, ChronoUnit.DAYS),
    val accessToken: String,
    val refreshToken: String,
    val scopes: List<Scope>,
) {
    constructor(data: SessionData) : this(
        token = data.token,
        user = data.userId.asSnowflake(),
        expiresAt = data.expiresAt,
        accessToken = data.accessToken,
        refreshToken = data.refreshToken,
        scopes = data.scopes.asStringListFromDatabase().map(Scope::valueOf),
    )
}
