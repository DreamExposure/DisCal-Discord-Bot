package org.dreamexposure.discal.cam.json.discal

import kotlinx.serialization.Serializable
import org.dreamexposure.discal.cam.json.discord.SimpleUserData
import org.dreamexposure.discal.core.serializers.InstantAsStringSerializer
import java.time.Instant

@Serializable
data class TokenResponse(
    val token: String,

    @Serializable(with = InstantAsStringSerializer::class)
    val expires: Instant,

    val user: SimpleUserData,
)
