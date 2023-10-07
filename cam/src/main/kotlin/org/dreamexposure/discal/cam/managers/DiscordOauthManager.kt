package org.dreamexposure.discal.cam.managers

import org.dreamexposure.discal.cam.business.OauthStateService
import org.dreamexposure.discal.cam.discord.DiscordOauthHandler
import org.dreamexposure.discal.cam.json.discal.TokenResponse
import org.dreamexposure.discal.core.business.SessionService
import org.dreamexposure.discal.core.config.Config
import org.dreamexposure.discal.core.crypto.KeyGenerator
import org.dreamexposure.discal.core.`object`.WebSession
import org.dreamexposure.discal.core.utils.GlobalVal.discordApiUrl
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException
import java.net.URLEncoder
import java.nio.charset.Charset.defaultCharset

@Component
class DiscordOauthManager(
    private val sessionService: SessionService,
    private val oauthStateService: OauthStateService,
    private val discordOauthHandler: DiscordOauthHandler,
) {
    private final val redirectUrl = Config.URL_DISCORD_REDIRECT.getString()
    private final val clientId = Config.DISCORD_APP_ID.getString()

    private final val scopes = URLEncoder.encode("identify guilds", defaultCharset())
    private final val encodedRedirectUrl = URLEncoder.encode(redirectUrl, defaultCharset())
    private final val oauthLinkWithoutState = "$discordApiUrl/oauth2/authorize?client_id=$clientId&redirect_uri=$encodedRedirectUrl&response_type=code&scope=$scopes&prompt=none"

    suspend fun getOauthLinkForLogin(): String {
        val state = oauthStateService.generateState()

        return "$oauthLinkWithoutState&state=$state"
    }

    suspend fun handleLogout(token: String) {
        sessionService.deleteSession(token)
    }

    suspend fun handleCodeExchange(state: String, code: String): TokenResponse {
        // Validate state
        if (!oauthStateService.validateState(state)) {
            // State invalid - 400
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid state")
        }

        val dTokens = discordOauthHandler.doTokenExchange(code)
        val authInfo = discordOauthHandler.getOauthInfo(dTokens.accessToken)
        val apiToken = KeyGenerator.csRandomAlphaNumericString(64)
        val session = WebSession(
            apiToken,
            authInfo.user!!.id,
            accessToken = dTokens.accessToken,
            refreshToken = dTokens.refreshToken
        )

        sessionService.removeAndInsertSession(session)

        return TokenResponse(session.token, session.expiresAt, authInfo.user)
    }
}
