package org.dreamexposure.discal.cam.endpoints.v1.oauth2

import org.dreamexposure.discal.cam.discord.DiscordOauthHandler
import org.dreamexposure.discal.cam.json.discal.LoginResponse
import org.dreamexposure.discal.cam.json.discal.TokenRequest
import org.dreamexposure.discal.cam.json.discal.TokenResponse
import org.dreamexposure.discal.cam.service.StateService
import org.dreamexposure.discal.core.annotations.Authentication
import org.dreamexposure.discal.core.crypto.KeyGenerator
import org.dreamexposure.discal.core.database.DatabaseManager
import org.dreamexposure.discal.core.`object`.WebSession
import org.dreamexposure.discal.core.utils.GlobalVal.discordApiUrl
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono
import java.net.URLEncoder
import java.nio.charset.Charset.defaultCharset

@RestController
@RequestMapping("/oauth2/discord/")
class DiscordOauthEndpoint(
    private val stateService: StateService,
    private val discordOauthHandler: DiscordOauthHandler,
    @Value("\${bot.url.discord.redirect}")
    private val redirectUrl: String,
    @Value("\${bot.discord-app-id}")
    private val clientId: String,
) {
    private final val scopes = URLEncoder.encode("identify guilds", defaultCharset())
    private final val encodedRedirectUrl = URLEncoder.encode(redirectUrl, defaultCharset())
    private final val oauthLinkWithoutState = "$discordApiUrl/oauth2/authorize?client_id=$clientId&redirect_uri=$encodedRedirectUrl&response_type=code&scope=$scopes&prompt=none"

    @GetMapping("login")
    @Authentication(access = Authentication.AccessLevel.PUBLIC)
    fun login(): Mono<LoginResponse> {
        val state = stateService.generateState()

        val link = "$oauthLinkWithoutState&state=$state"

        return Mono.just(LoginResponse(link))
    }

    @GetMapping("logout")
    @Authentication(access = Authentication.AccessLevel.WRITE)
    fun logout(@RequestHeader("Authorization") token: String): Mono<Void> {
        return DatabaseManager.deleteSession(token).then()
    }

    @PostMapping("code")
    @Authentication(access = Authentication.AccessLevel.PUBLIC)
    fun token(@RequestBody body: TokenRequest): Mono<TokenResponse> {
        // Validate state
        if (!stateService.validateState(body.state)) {
            // State invalid - 400
            return Mono.error(ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid state"))
        }

        return discordOauthHandler.doTokenExchange(body.code).flatMap { dTokens ->
            // request current user info
            discordOauthHandler.getOauthInfo(dTokens.accessToken).flatMap { authInfo ->
                val apiToken = KeyGenerator.csRandomAlphaNumericString(64)

                val session = WebSession(
                    apiToken,
                    authInfo.user!!.id,
                    accessToken = dTokens.accessToken,
                    refreshToken = dTokens.refreshToken
                )

                // Save session data then return response
                DatabaseManager.removeAndInsertSessionData(session)
                    .thenReturn(TokenResponse(session.token, session.expiresAt, authInfo.user))
            }
        }
    }
}
