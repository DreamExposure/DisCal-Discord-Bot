package org.dreamexposure.discal.server.endpoints.v2.account

import kotlinx.serialization.encodeToString
import org.dreamexposure.discal.core.annotations.SecurityRequirement
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.utils.GlobalVal
import org.dreamexposure.discal.server.utils.Authentication
import org.dreamexposure.discal.server.utils.responseMessage
import org.json.JSONException
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/v2/account")
class LogoutEndpoint(
    private val authentication: Authentication,
) {
    @SecurityRequirement(disableSecurity = true, scopes = [])
    @GetMapping("/logout", produces = ["application/json"])
    fun logoutOfAccount(swe: ServerWebExchange, response: ServerHttpResponse): Mono<String> {
        return authentication.authenticate(swe).flatMap { authState ->
            if (!authState.success) {
                response.rawStatusCode = authState.status
                return@flatMap Mono.just(GlobalVal.JSON_FORMAT.encodeToString(authState))
            } else if (authState.readOnly) {
                response.rawStatusCode = GlobalVal.STATUS_AUTHORIZATION_DENIED
                return@flatMap responseMessage("Unauthorized to use this endpoint")
            }

            //Handle request
            authentication.removeTempKey(authState.keyUsed)

            response.rawStatusCode = GlobalVal.STATUS_SUCCESS
            return@flatMap responseMessage("Logged out")
        }.onErrorResume(JSONException::class.java) {
            LOGGER.trace("[API-v2] JSON error. Bad request?", it)

            response.rawStatusCode = GlobalVal.STATUS_BAD_REQUEST
            return@onErrorResume responseMessage("Bad Request")
        }.onErrorResume {
            LOGGER.error(GlobalVal.DEFAULT, "[API-v2] Logout error / key error", it)

            response.rawStatusCode = GlobalVal.STATUS_INTERNAL_ERROR
            return@onErrorResume responseMessage("Internal Server Error")
        }
    }
}
