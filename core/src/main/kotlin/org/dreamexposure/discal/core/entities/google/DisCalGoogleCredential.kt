package org.dreamexposure.discal.core.entities.google

import org.dreamexposure.discal.core.config.Config
import org.dreamexposure.discal.core.crypto.AESEncryption
import org.dreamexposure.discal.core.`object`.new.Credential
import reactor.core.publisher.Mono
import java.time.Instant

data class DisCalGoogleCredential(
    val credential: Credential,
) {
    private val aes: AESEncryption = AESEncryption(Config.SECRET_GOOGLE_CREDENTIAL_KEY.getString())
    private var access: String? = null
    private var refresh: String? = null

    fun getRefreshToken(): Mono<String> {
       if (refresh != null) return Mono.justOrEmpty(refresh)
        return aes.decrypt(credential.encryptedRefreshToken)
                .doOnNext { refresh = it }
    }

    fun getAccessToken(): Mono<String> {
        if (access != null) return Mono.justOrEmpty(access)
        return aes.decrypt(credential.encryptedAccessToken)
                .doOnNext { access = it }
    }

    fun setRefreshToken(token: String): Mono<Void> {
        refresh = token
        //credentialData.encryptedRefreshToken = aes.encrypt(token)
        return aes.encrypt(token)
            .doOnNext { credential.encryptedRefreshToken = it }
                .then()
    }

    fun setAccessToken(token: String): Mono<Void> {
        access = token
        //credentialData.encryptedAccessToken = aes.encrypt(token)
        return aes.encrypt(token)
            .doOnNext { credential.encryptedAccessToken = it }
                .then()
    }

    fun expired() = Instant.now().isAfter(credential.expiresAt)
}
