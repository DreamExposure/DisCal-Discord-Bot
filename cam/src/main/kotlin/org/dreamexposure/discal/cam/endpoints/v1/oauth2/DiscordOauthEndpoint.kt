package org.dreamexposure.discal.cam.endpoints.v1.oauth2

import org.dreamexposure.discal.cam.discord.DiscordOauthHandler
import org.dreamexposure.discal.cam.json.discal.TokenRequest
import org.dreamexposure.discal.cam.json.discal.TokenResponse
import org.dreamexposure.discal.cam.service.StateService
import org.dreamexposure.discal.core.`object`.BotSettings.ID
import org.dreamexposure.discal.core.`object`.BotSettings.REDIR_URL
import org.dreamexposure.discal.core.`object`.WebSession
import org.dreamexposure.discal.core.annotations.Authentication
import org.dreamexposure.discal.core.crypto.KeyGenerator
import org.dreamexposure.discal.core.database.DatabaseManager
import org.dreamexposure.discal.core.utils.GlobalVal
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.Charset.defaultCharset

@RestController
@RequestMapping("/oauth2/discord/")
class DiscordOauthEndpoint(private val stateService: StateService) {
    private final val scopes = URLEncoder.encode("identify guilds", defaultCharset())
    private final val redirectUrl = URLEncoder.encode(REDIR_URL.get(), defaultCharset())
    private final val oauthLinkWithoutState =
        "${GlobalVal.discordApiUrl}/oauth2/authorize" +
            "?client_id=${ID.get()}" +
            "&redirect_uri=$redirectUrl" +
            "&response_type=code" +
            "&scope=$scopes" +
            "&prompt=none" // Skip consent screen if user has already authorized these scopes before

    @GetMapping("login")
    @Authentication(access = Authentication.AccessLevel.PUBLIC)
    fun login(): Mono<ServerResponse> {
        val state = stateService.generateState()

        val link = "$oauthLinkWithoutState&state=$state"

        return ServerResponse.temporaryRedirect(URI.create(link)).build()
    }

    @PostMapping("code")
    @Authentication(access = Authentication.AccessLevel.PUBLIC)
    fun token(@RequestBody body: TokenRequest): Mono<TokenResponse> {
        // Validate state
        if (!stateService.validateState(body.state)) {
            // State invalid - 400
            return Mono.error(ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid state"))
        }

        return DiscordOauthHandler.doTokenExchange(body.code).flatMap { dTokens ->
            // request current user info
            DiscordOauthHandler.getOauthInfo(dTokens.accessToken).flatMap { authInfo ->
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
