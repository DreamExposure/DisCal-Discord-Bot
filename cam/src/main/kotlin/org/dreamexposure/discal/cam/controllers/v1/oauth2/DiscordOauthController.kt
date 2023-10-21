package org.dreamexposure.discal.cam.controllers.v1.oauth2

import org.dreamexposure.discal.cam.json.discal.LoginResponse
import org.dreamexposure.discal.cam.json.discal.TokenRequest
import org.dreamexposure.discal.cam.json.discal.TokenResponse
import org.dreamexposure.discal.cam.managers.DiscordOauthManager
import org.dreamexposure.discal.core.annotations.SecurityRequirement
import org.dreamexposure.discal.core.`object`.new.security.Scope.OAUTH2_DISCORD
import org.dreamexposure.discal.core.`object`.new.security.TokenType.BEARER
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/oauth2/discord/")
class DiscordOauthController(
    private val discordOauthManager: DiscordOauthManager,
) {

    @GetMapping("login")
    @SecurityRequirement(disableSecurity = true, scopes = [])
    suspend fun login(): LoginResponse {
        val link = discordOauthManager.getOauthLinkForLogin()
        return LoginResponse(link)
    }

    @GetMapping("logout")
    @SecurityRequirement(schemas = [BEARER], scopes = [OAUTH2_DISCORD])
    suspend fun logout(@RequestHeader("Authorization") token: String) {
        discordOauthManager.handleLogout(token)
    }

    @PostMapping("code")
    @SecurityRequirement(disableSecurity = true, scopes = [])
    suspend fun token(@RequestBody body: TokenRequest): TokenResponse {
        return discordOauthManager.handleCodeExchange(body.state, body.code)
    }
}
