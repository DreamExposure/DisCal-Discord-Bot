package org.dreamexposure.discal.core.`object`.network.discal

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.dreamexposure.discal.core.serializers.InstantAsStringSerializer
import java.time.Instant

@Serializable
@Deprecated("Really shouldn't need this after the auth wrapper has been redone, replaced with the TokenV1Model")
data class CredentialData(
        @SerialName("access_token")
        val accessToken: String,

        @SerialName("valid_until")
        @Serializable(with = InstantAsStringSerializer::class)
        val validUntil: Instant
) {
    fun isExpired(): Boolean = Instant.now().isAfter(validUntil)
}
