package org.dreamexposure.discal.cam.business

import org.dreamexposure.discal.core.business.ApiKeyService
import org.dreamexposure.discal.core.business.SessionService
import org.dreamexposure.discal.core.config.Config
import org.dreamexposure.discal.core.extensions.isExpiredTtl
import org.dreamexposure.discal.core.`object`.new.security.Scope
import org.dreamexposure.discal.core.`object`.new.security.TokenType
import org.springframework.stereotype.Component

@Component
class SecurityService(
    private val sessionService: SessionService,
    private val apiKeyService: ApiKeyService,
) {
    suspend fun authenticateToken(token: String): Boolean {
        val schema = getSchema(token)
        val tokenStr = token.removePrefix(schema.schema)

        return when (schema) {
            TokenType.BEARER -> authenticateUserToken(tokenStr)
            TokenType.APP -> authenticateAppToken(tokenStr)
            TokenType.INTERNAL -> authenticateInternalToken(tokenStr)
            else -> false
        }
    }

    suspend fun validateTokenSchema(token: String, allowedSchemas: List<TokenType>): Boolean {
        if (allowedSchemas.isEmpty()) return true // No schemas required
        val schema = getSchema(token)

        return allowedSchemas.contains(schema)
    }

    suspend fun authorizeToken(token: String, requiredScopes: List<Scope>): Boolean {
        if (requiredScopes.isEmpty()) return true // No scopes required

        val schema = getSchema(token)
        val tokenStr = token.removePrefix(schema.schema)

        val scopes = when (schema) {
            TokenType.BEARER -> getScopesForUserToken(tokenStr)
            TokenType.APP -> getScopesForAppToken(tokenStr)
            TokenType.INTERNAL -> getScopesForInternalToken()
            else -> return false
        }

        return scopes.containsAll(requiredScopes)
    }


    // Authentication based on token type
    private suspend fun authenticateUserToken(token: String): Boolean {
        val session = sessionService.getSession(token) ?: return false

        return !session.expiresAt.isExpiredTtl()
    }

    private suspend fun authenticateAppToken(token: String): Boolean {
        val key = apiKeyService.getKey(token) ?: return false

        return !key.blocked
    }

    private fun authenticateInternalToken(token: String): Boolean {
        return Config.SECRET_DISCAL_API_KEY.getString() == token
    }

    // Fetching scopes for tokens
    private suspend fun getScopesForUserToken(token: String): List<Scope> {
        return sessionService.getSession(token)?.scopes ?: emptyList()
    }

    private suspend fun getScopesForAppToken(token: String): List<Scope> {
        return apiKeyService.getKey(token)?.scopes ?: emptyList()
    }

    private fun getScopesForInternalToken(): List<Scope> = Scope.entries.toList()

    // Various other stuff
    private fun getSchema(token: String): TokenType {
        return when {
            token.startsWith(TokenType.BEARER.schema) -> TokenType.BEARER
            token.startsWith(TokenType.APP.schema) -> TokenType.APP
            token.startsWith(TokenType.INTERNAL.schema) -> TokenType.INTERNAL
            else -> TokenType.NONE
        }
    }
}
