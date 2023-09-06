package org.dreamexposure.discal.core.business

import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.dreamexposure.discal.core.database.ApiRepository
import org.dreamexposure.discal.core.`object`.new.ApiKey
import org.springframework.stereotype.Component


@Component
class DefaultApiKeyService(
    private val apiRepository: ApiRepository,
): ApiKeyService {
    override suspend fun getKey(key: String): ApiKey? {
        return apiRepository.getByApiKey(key)
            .map(::ApiKey)
            .awaitSingleOrNull()
    }

}

interface ApiKeyService {
    suspend fun getKey(key: String): ApiKey?
}
