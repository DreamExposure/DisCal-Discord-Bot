package org.dreamexposure.discal.core.business

import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.dreamexposure.discal.CredentialsCache
import org.dreamexposure.discal.core.database.CredentialData
import org.dreamexposure.discal.core.database.CredentialsRepository
import org.dreamexposure.discal.core.`object`.new.Credential
import org.springframework.stereotype.Component


@Component
class DefaultCredentialService(
    private val credentialsRepository: CredentialsRepository,
    private val credentialsCache: CredentialsCache,
) : CredentialService {

    override suspend fun createCredential(credential: Credential): Credential {
        val encryptedRefreshToken = Credential.aes.encrypt(credential.refreshToken).awaitSingle()
        val encryptedAccessToken = Credential.aes.encrypt(credential.accessToken).awaitSingle()

        val saved = credentialsRepository.save(CredentialData(
            credentialNumber = credential.credentialNumber,
            accessToken = encryptedAccessToken,
            refreshToken = encryptedRefreshToken,
            expiresAt = credential.expiresAt.toEpochMilli(),
        )).map(::Credential).awaitSingle()

        credentialsCache.put(saved.credentialNumber, saved)
        return saved
    }

    override suspend fun getCredential(number: Int): Credential? {
        var credential = credentialsCache.get(number)
        if (credential != null) return credential

        credential = credentialsRepository.findByCredentialNumber(number)
            .map(::Credential)
            .awaitSingle()

        if (credential != null) credentialsCache.put(number, credential)
        return credential
    }

    override suspend fun updateCredential(credential: Credential) {
        val encryptedRefreshToken = Credential.aes.encrypt(credential.refreshToken).awaitSingle()
        val encryptedAccessToken = Credential.aes.encrypt(credential.accessToken).awaitSingle()

        credentialsRepository.updateByCredentialNumber(
            credentialNumber = credential.credentialNumber,
            refreshToken = encryptedRefreshToken,
            accessToken = encryptedAccessToken,
            expiresAt = credential.expiresAt.toEpochMilli(),
        ).awaitSingleOrNull()

        credentialsCache.put(credential.credentialNumber, credential)
    }
}

interface CredentialService {
    suspend fun createCredential(credential: Credential): Credential
    suspend fun getCredential(number: Int): Credential?
    suspend fun updateCredential(credential: Credential)
}
