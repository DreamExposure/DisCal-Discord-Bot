package org.dreamexposure.discal.core.`object`.new.security

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import discord4j.common.util.Snowflake
import org.dreamexposure.discal.core.serializers.SnowflakeMapper
import java.time.Instant

data class TemporaryToken(
    val token: String,
    @JsonSerialize(using = SnowflakeMapper.SnowflakeSerializer::class)
    @JsonDeserialize(using = SnowflakeMapper.SnowflakeDeserializer::class)
    val guildId: Snowflake,
    val expiresAt: Instant,
    val scopes: List<Scope>
)
