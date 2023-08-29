package org.dreamexposure.discal.cam.endpoints.v1.oauth2

import kotlinx.coroutines.reactor.awaitSingle
import org.dreamexposure.discal.cam.business.OauthStateService
import org.dreamexposure.discal.cam.discord.DiscordOauthHandler
import org.dreamexposure.discal.cam.json.discal.LoginResponse
import org.dreamexposure.discal.cam.json.discal.TokenRequest
import org.dreamexposure.discal.cam.json.discal.TokenResponse
import org.dreamexposure.discal.core.annotations.Authentication
import org.dreamexposure.discal.core.business.SessionService
import org.dreamexposure.discal.core.config.Config
import org.dreamexposure.discal.core.crypto.KeyGenerator
import org.dreamexposure.discal.core.`object`.WebSession
import org.dreamexposure.discal.core.utils.GlobalVal.discordApiUrl
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.net.URLEncoder
import java.nio.charset.Charset.defaultCharset

@RestController
@RequestMapping("/oauth2/discord/")
class DiscordOauthEndpoint(
    private val sessionService: SessionService,
    private val oauthStateService: OauthStateService,
    private val discordOauthHandler: DiscordOauthHandler,
) {
    private val redirectUrl = Config.URL_DISCORD_REDIRECT.getString()
    private val clientId = Config.DISCORD_APP_ID.getString()

    private final val scopes = URLEncoder.encode("identify guilds", defaultCharset())
    private final val encodedRedirectUrl = URLEncoder.encode(redirectUrl, defaultCharset())
    private final val oauthLinkWithoutState = "$discordApiUrl/oauth2/authorize?client_id=$clientId&redirect_uri=$encodedRedirectUrl&response_type=code&scope=$scopes&prompt=none"

    @GetMapping("login")
    @Authentication(access = Authentication.AccessLevel.PUBLIC)
    suspend fun login(): LoginResponse {
        val state = oauthStateService.generateState()

        val link = "$oauthLinkWithoutState&state=$state"

        return LoginResponse(link)
    }

    @GetMapping("logout")
    @Authentication(access = Authentication.AccessLevel.WRITE)
    suspend fun logout(@RequestHeader("Authorization") token: String) {
        sessionService.deleteSession(token)
    }

    @PostMapping("code")
    @Authentication(access = Authentication.AccessLevel.PUBLIC)
    suspend fun token(@RequestBody body: TokenRequest): TokenResponse {
        // Validate state
        if (!oauthStateService.validateState(body.state)) {
            // State invalid - 400
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid state")
        }

        val dTokens = discordOauthHandler.doTokenExchange(body.code).awaitSingle()
        val authInfo = discordOauthHandler.getOauthInfo(dTokens.accessToken).awaitSingle()
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
