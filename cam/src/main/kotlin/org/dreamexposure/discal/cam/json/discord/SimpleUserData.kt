package org.dreamexposure.discal.cam.json.discord

import discord4j.common.util.Snowflake
import kotlinx.serialization.Serializable
import org.dreamexposure.discal.core.serializers.SnowflakeAsStringSerializer

@Serializable
data class SimpleUserData(
    @Serializable(with = SnowflakeAsStringSerializer::class)
    val id: Snowflake,

    val username: String,

    val discriminator: String,

    val avatar: String?,
)
