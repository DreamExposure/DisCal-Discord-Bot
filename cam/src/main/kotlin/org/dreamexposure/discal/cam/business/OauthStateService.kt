package org.dreamexposure.discal.cam.business

import org.dreamexposure.discal.OauthStateCache
import org.dreamexposure.discal.core.crypto.KeyGenerator
import org.springframework.stereotype.Component

@Component
class OauthStateService(
    private val stateCache: OauthStateCache,
) {
    suspend fun generateState(): String {
        val state = KeyGenerator.csRandomAlphaNumericString(64)
        stateCache.put(key = state, value = state)

        return state
    }

    suspend fun validateState(state: String) = stateCache.getAndRemove(key = state) != null
}
