package org.dreamexposure.discal.core.`object`.new

import org.dreamexposure.discal.core.database.CredentialData
import java.time.Instant

data class Credential(
    val credentialNumber: Int,
    var encryptedRefreshToken: String,
    var encryptedAccessToken: String,
    var expiresAt: Instant,
) {
    constructor(data: CredentialData) : this(
        credentialNumber = data.credentialNumber,
        encryptedRefreshToken = data.refreshToken,
        encryptedAccessToken = data.accessToken,
        expiresAt = Instant.ofEpochMilli(data.expiresAt),
    )
}
