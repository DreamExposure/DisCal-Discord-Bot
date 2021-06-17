package org.dreamexposure.discal.core.`object`.google

data class GoogleCredentialData(
        val credentialNumber: Int,
        var encryptedRefreshToken: String,
        var encryptedAccessToken: String,
)
