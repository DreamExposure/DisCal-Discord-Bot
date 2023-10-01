package org.dreamexposure.discal.cam.controllers.v1.oauth2

import org.dreamexposure.discal.cam.json.discal.LoginResponse
import org.dreamexposure.discal.cam.json.discal.TokenRequest
import org.dreamexposure.discal.cam.json.discal.TokenResponse
import org.dreamexposure.discal.cam.managers.DiscordOauthManager
import org.dreamexposure.discal.core.annotations.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/oauth2/discord/")
class DiscordOauthController(
    private val discordOauthManager: DiscordOauthManager,
) {

    @GetMapping("login")
    @Authentication(access = Authentication.AccessLevel.PUBLIC)
    suspend fun login(): LoginResponse {
        val link = discordOauthManager.getOauthLinkForLogin()
        return LoginResponse(link)
    }

    @GetMapping("logout")
    @Authentication(access = Authentication.AccessLevel.WRITE)
    suspend fun logout(@RequestHeader("Authorization") token: String) {
        discordOauthManager.handleLogout(token)
    }

    @PostMapping("code")
    @Authentication(access = Authentication.AccessLevel.PUBLIC)
    suspend fun token(@RequestBody body: TokenRequest): TokenResponse {
        return discordOauthManager.handleCodeExchange(body.state, body.code)
    }
}
