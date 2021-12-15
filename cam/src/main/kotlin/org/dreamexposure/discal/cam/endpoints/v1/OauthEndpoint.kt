package org.dreamexposure.discal.cam.endpoints.v1

import org.dreamexposure.discal.cam.service.StateService
import org.dreamexposure.discal.core.`object`.BotSettings.ID
import org.dreamexposure.discal.core.`object`.BotSettings.REDIR_URL
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.Charset.defaultCharset

@RestController
@RequestMapping("/oauth/")
class OauthEndpoint(private val stateService: StateService) {
    private final val scopes = URLEncoder.encode("identify guilds", defaultCharset())
    private final val redirectUrl = URLEncoder.encode(REDIR_URL.get(), defaultCharset())
    private final val oauthLinkWithoutState =
        "https://discord.com" +
            "/api/oauth2/authorize" +
            "?client_id=${ID.get()}" +
            "&redirect_uri=$redirectUrl" +
            "&response_type=code" +
            "&scope=$scopes" +
            "&prompt=none"

    @GetMapping("login")
    fun login(): Mono<ServerResponse> {
        val state = stateService.generateState()

        val link = "$oauthLinkWithoutState&state=$state"

        return ServerResponse.temporaryRedirect(URI.create(link)).build()
    }

    @GetMapping("code")
    fun token(@RequestParam code: String,  @RequestParam state: String): Mono<ServerResponse> {
        //TODO: Validate state

        // TODO: Do token exchange and return token to our API + user info

        return ServerResponse.temporaryRedirect(URI.create("LINK_HERE"))
            //TODO: Insert body
            .build()
    }
}
