package org.dreamexposure.discal.core.entities.google

import org.dreamexposure.discal.core.`object`.BotSettings
import org.dreamexposure.discal.core.`object`.google.GoogleCredentialData
import org.dreamexposure.discal.core.crypto.AESEncryption
import reactor.core.publisher.Mono
import java.time.Instant

data class DisCalGoogleCredential(
        val credentialData: GoogleCredentialData,
) {
    private val aes: AESEncryption = AESEncryption(BotSettings.CREDENTIALS_KEY.get())
    private var access: String? = null
    private var refresh: String? = null

    fun getRefreshToken(): String {
        if (refresh == null)
            refresh = aes.decrypt(credentialData.encryptedRefreshToken)

        return refresh!!
    }

    fun getAccessToken(): String {
        if (access == null)
            access = aes.decrypt(credentialData.encryptedAccessToken)

        return access!!
    }

    fun setRefreshToken(token: String) {
        refresh = token
        credentialData.encryptedRefreshToken = aes.encrypt(token)
    }

    fun setAccessToken(token: String): Mono<Void> {
        access = token
        //credentialData.encryptedAccessToken = aes.encrypt(token)
        return aes.encryptReactive(token)
                .doOnNext { credentialData.encryptedAccessToken = it }
                .then()
    }

    fun expired() = Instant.now().isAfter(credentialData.expiresAt)
}
