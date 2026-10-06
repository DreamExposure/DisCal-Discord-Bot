package org.dreamexposure.discal.cam.business

import discord4j.common.util.Snowflake
import org.dreamexposure.discal.core.business.ApiKeyService
import org.dreamexposure.discal.core.business.CalendarService
import org.dreamexposure.discal.core.business.PermissionService
import org.dreamexposure.discal.core.business.SessionService
import org.dreamexposure.discal.core.config.Config
import org.dreamexposure.discal.core.extensions.isExpiredTtl
import org.dreamexposure.discal.core.`object`.new.CalendarMetadata
import org.dreamexposure.discal.core.`object`.new.security.AccessLevel
import org.dreamexposure.discal.core.`object`.new.security.Scope
import org.dreamexposure.discal.core.`object`.new.security.TokenType
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

@Component
class SecurityService(
    private val sessionService: SessionService,
    private val apiKeyService: ApiKeyService,
    private val permissionService: PermissionService,
    private val calendarService: CalendarService,
) {
    suspend fun authenticateAndAuthorizeToken(token: String, schemas: List<TokenType>, scopes: List<Scope>, accessLevel: AccessLevel, guildId: Snowflake?, calNumber: Int?): Pair<HttpStatus, String> {
        if (!authenticateToken(token)) return Pair(HttpStatus.UNAUTHORIZED, "Unauthenticated")

        if (!validateTokenSchema(token, schemas)) return Pair(HttpStatus.UNAUTHORIZED, "Unsupported schema")

        if (!authorizeToken(token, scopes)) return Pair(HttpStatus.FORBIDDEN, "Access denied")

        if (!authorizeOnAccessLevel(token, accessLevel, guildId, calNumber)) return Pair(HttpStatus.FORBIDDEN, "Access to resource denied")

        return Pair(HttpStatus.OK, "Authorized")
    }

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

    suspend fun authorizeOnAccessLevel(token: String, accessLevel: AccessLevel, guildId: Snowflake?, calNumber: Int?): Boolean {
        if (getSchema(token) == TokenType.INTERNAL) return true // bot has access to everything, cannot get here unless already authed, no need to double-check auth
        if (guildId == null) return true // No guildId in request, access level for guilds not relevant
        if (accessLevel == AccessLevel.PUBLIC) return true // Public resource regardless of guild

        val userId = getUserFromToken(token) ?: return false // User needs to exist
        val hasAccessToGuild = permissionService.hasAccessToGuild(guildId, userId)

        return when (accessLevel) {
            AccessLevel.GUILD_MEMBERS -> hasAccessToGuild
            AccessLevel.DEFER_TO_CALENDAR_PRIVACY -> {
                if (calNumber == null) return false // Calendar needs to be provided in the request, otherwise what does this defer to?
                val calendarMetadata = calendarService.getCalendarMetadata(guildId, calNumber) ?: return false // calendar must exist

                when(calendarMetadata.privacy) {
                    CalendarMetadata.Privacy.PUBLIC -> true
                    CalendarMetadata.Privacy.GUILD_MEMBERS_ONLY -> hasAccessToGuild
                }
            }
            AccessLevel.PRIVILEGED_MEMBERS -> hasAccessToGuild && permissionService.hasControlRole(guildId, userId)
            AccessLevel.ELEVATED_MEMBERS -> hasAccessToGuild && permissionService.hasElevatedPermissions(guildId, userId)
        }
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

    private suspend fun getUserFromToken(token: String): Snowflake? {
        val schema = getSchema(token)
        val tokenStr = token.removePrefix(schema.schema)

        return when (schema) {
            TokenType.BEARER -> sessionService.getSession(tokenStr)?.user
            TokenType.APP -> apiKeyService.getKey(tokenStr)?.userId
            else -> null
        }
    }
}
