package org.dreamexposure.discal.core.`object`.new

import org.dreamexposure.discal.core.config.Config
import org.dreamexposure.discal.core.crypto.AESEncryption
import org.dreamexposure.discal.core.database.CredentialData
import org.dreamexposure.discal.core.extensions.asInstantMilli
import java.time.Instant

data class Credential(
    val credentialNumber: Int,
    var expiresAt: Instant,
    var refreshToken: String,
    var accessToken: String,
) {
    /**
     * Returns the encrypted refresh token, note that this will run the encryption on every access.
     */
    val encryptedRefreshToken: String
        get() = aes.encryptFixed(refreshToken)

    /**
     * Returns the encrypted access token, note that this will run the encryption on every access.
     */
    val encryptedAccessToken: String
        get() = aes.encryptFixed(accessToken)

    constructor(data: CredentialData) : this(
        credentialNumber = data.credentialNumber,
        expiresAt = data.expiresAt.asInstantMilli(),
        refreshToken = aes.decryptFixed(data.refreshToken),
        accessToken = aes.decryptFixed(data.accessToken),
    )

    companion object {
        private val aes = AESEncryption(Config.SECRET_GOOGLE_CREDENTIAL_KEY.getString())
    }
}
