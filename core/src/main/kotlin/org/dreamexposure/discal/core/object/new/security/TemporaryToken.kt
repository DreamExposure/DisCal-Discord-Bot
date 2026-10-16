package org.dreamexposure.discal.core.`object`.new.security

import discord4j.common.util.Snowflake
import java.time.Instant

data class TemporaryToken(
    val token: String,
    val guildId: Snowflake,
    val expiresAt: Instant,
    val scopes: List<Scope>
)
