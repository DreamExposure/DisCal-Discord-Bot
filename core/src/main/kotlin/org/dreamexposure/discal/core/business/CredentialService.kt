package org.dreamexposure.discal.core.business

import kotlinx.coroutines.reactor.awaitSingle
import org.dreamexposure.discal.CredentialsCache
import org.dreamexposure.discal.core.database.CredentialsRepository
import org.dreamexposure.discal.core.`object`.new.Credential
import org.springframework.stereotype.Component


@Component
class DefaultCredentialService(
    private val credentialsRepository: CredentialsRepository,
    private val credentialsCache: CredentialsCache,
) : CredentialService {
    override suspend fun getCredential(number: Int): Credential? {
        var credential = credentialsCache.get(number)
        if (credential != null) return credential

        credential = credentialsRepository.findByCredentialNumber(number)
            .map(::Credential)
            .awaitSingle()

        if (credential != null) credentialsCache.put(number, credential)
        return credential
    }

}

interface CredentialService {
    suspend fun getCredential(number: Int): Credential?
}
