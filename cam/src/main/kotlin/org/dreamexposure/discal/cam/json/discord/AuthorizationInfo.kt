package org.dreamexposure.discal.cam.json.discord

import kotlinx.serialization.Serializable
import org.dreamexposure.discal.core.serializers.InstantAsStringSerializer
import java.time.Instant

@Serializable
data class AuthorizationInfo(
    val scopes: List<String>,

    @Serializable(with = InstantAsStringSerializer::class)
    val expires: Instant,

    val user: SimpleUserData?,
)
