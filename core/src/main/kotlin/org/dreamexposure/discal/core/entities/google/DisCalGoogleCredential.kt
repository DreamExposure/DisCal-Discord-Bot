package org.dreamexposure.discal.core.entities.google

import org.dreamexposure.discal.core.`object`.BotSettings
import org.dreamexposure.discal.core.`object`.google.GoogleCredentialData
import org.dreamexposure.discal.core.crypto.AESEncryption
import java.time.Instant

data class DisCalGoogleCredential(
        val credentialData: GoogleCredentialData,
) {
    val aes: AESEncryption = AESEncryption(BotSettings.CREDENTIALS_KEY.get())

    fun getRefreshToken() = aes.decrypt(credentialData.encryptedRefreshToken)

    fun getAccessToken() = aes.decrypt(credentialData.encryptedAccessToken)

    fun setRefreshToken(token: String) {
        credentialData.encryptedRefreshToken = aes.encrypt(token)
    }

    fun setAccessToken(token: String) {
        credentialData.encryptedAccessToken = aes.encrypt(token)
    }

    fun expired() = Instant.now().isAfter(credentialData.expiresAt)
}
