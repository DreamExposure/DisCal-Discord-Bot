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

        val savedData = credentialsRepository.save(CredentialData(
            credentialNumber = credential.credentialNumber,
            accessToken = encryptedAccessToken,
            refreshToken = encryptedRefreshToken,
            expiresAt = credential.expiresAt.toEpochMilli(),
        )).awaitSingle()
        val saved = Credential(savedData)


        credentialsCache.put(key = saved.credentialNumber, value = saved)
        return saved
    }

    override suspend fun getCredential(number: Int): Credential? {
        var credential = credentialsCache.get(key = number)
        if (credential != null) return credential

        val data = credentialsRepository.findByCredentialNumber(number).awaitSingleOrNull() ?: return null
        credential = Credential(data)

        credentialsCache.put(key = number, value = credential)
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

        credentialsCache.put(key = credential.credentialNumber, value = credential)
    }
}

interface CredentialService {
    suspend fun createCredential(credential: Credential): Credential
    suspend fun getCredential(number: Int): Credential?
    suspend fun updateCredential(credential: Credential)
}
