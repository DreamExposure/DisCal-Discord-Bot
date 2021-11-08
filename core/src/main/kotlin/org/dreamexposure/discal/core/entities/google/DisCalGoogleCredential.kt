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

    fun getRefreshToken(): Mono<String> {
       if (refresh != null) return Mono.justOrEmpty(refresh)
        return aes.decrypt(credentialData.encryptedRefreshToken)
                .doOnNext { refresh = it }
    }

    fun getAccessToken(): Mono<String> {
        if (access != null) return Mono.justOrEmpty(access)
        return aes.decrypt(credentialData.encryptedAccessToken)
                .doOnNext { access = it }
    }

    fun setRefreshToken(token: String): Mono<Void> {
        refresh = token
        //credentialData.encryptedRefreshToken = aes.encrypt(token)
        return aes.encrypt(token)
                .doOnNext { credentialData.encryptedRefreshToken = it }
                .then()
    }

    fun setAccessToken(token: String): Mono<Void> {
        access = token
        //credentialData.encryptedAccessToken = aes.encrypt(token)
        return aes.encrypt(token)
                .doOnNext { credentialData.encryptedAccessToken = it }
                .then()
    }

    fun expired() = Instant.now().isAfter(credentialData.expiresAt)
}
