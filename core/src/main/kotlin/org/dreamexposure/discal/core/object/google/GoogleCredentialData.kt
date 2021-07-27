package org.dreamexposure.discal.core.`object`.google

import java.time.Instant

data class GoogleCredentialData(
        val credentialNumber: Int,
        var encryptedRefreshToken: String,
        var encryptedAccessToken: String,
        var expiresAt: Instant,
)
