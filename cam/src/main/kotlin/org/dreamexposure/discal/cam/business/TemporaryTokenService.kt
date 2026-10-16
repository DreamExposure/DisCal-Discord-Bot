package org.dreamexposure.discal.cam.business

import discord4j.common.util.Snowflake
import org.dreamexposure.discal.TemporaryTokenCache
import org.dreamexposure.discal.core.config.Config
import org.dreamexposure.discal.core.crypto.KeyGenerator
import org.dreamexposure.discal.core.extensions.asMinutes
import org.dreamexposure.discal.core.extensions.isExpiredTtl
import org.dreamexposure.discal.core.extensions.sha256Hash
import org.dreamexposure.discal.core.`object`.new.security.Scope
import org.dreamexposure.discal.core.`object`.new.security.TemporaryToken
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class TemporaryTokenService(
    private val temporaryTokenCache: TemporaryTokenCache,
) {
    private val TOKEN_TIME = Config.CACHE_TTL_TEMPORARY_TOKEN_MINUTES.getLong().asMinutes()
    private val TOKEN_LENGTH = 64

    suspend fun createTokenForCalendarEmbedViewing(guildId: Snowflake): TemporaryToken {
        val token = TemporaryToken(
            token = KeyGenerator.csRandomAlphaNumericString(TOKEN_LENGTH),
            guildId = guildId,
            expiresAt = Instant.now().plus(TOKEN_TIME),
            scopes = listOf(Scope.CALENDAR_READ, Scope.CALENDAR_EVENT_READ)
        )

        val cacheKey = token.token.toByteArray().sha256Hash()
        temporaryTokenCache.put(key = cacheKey, value = token)

        return token
    }

    suspend fun getToken(token: String): TemporaryToken? {
        if (token.length > TOKEN_LENGTH) return null

        val existing = temporaryTokenCache.get(key = token.toByteArray().sha256Hash()) ?: return null

        return if (!existing.expiresAt.isExpiredTtl()) existing else null
    }

    suspend fun invalidateAllTokens() {
        temporaryTokenCache.evictAll()
    }
}