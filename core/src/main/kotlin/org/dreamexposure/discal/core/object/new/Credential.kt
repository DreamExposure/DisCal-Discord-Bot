package org.dreamexposure.discal.core.`object`.new

import org.dreamexposure.discal.core.database.CredentialData
import org.dreamexposure.discal.core.extensions.asInstantMilli
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
        expiresAt = data.expiresAt.asInstantMilli(),
    )
}
