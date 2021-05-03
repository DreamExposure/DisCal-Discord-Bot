package org.dreamexposure.discal.server.endpoints.v2.account

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.dreamexposure.discal.core.logger.LogFeed
import org.dreamexposure.discal.core.logger.`object`.LogObject
import org.dreamexposure.discal.core.utils.GlobalConst
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
class LogoutEndpoint {
    @GetMapping("/logout", produces = ["application/json"])
    fun logoutOfAccount(swe: ServerWebExchange, response: ServerHttpResponse): Mono<String> {
        return Authentication.authenticate(swe).flatMap { authState ->
            if (!authState.success) {
                response.rawStatusCode = authState.status
                return@flatMap Mono.just(Json.encodeToString(authState))
            } else if (authState.readOnly) {
                response.rawStatusCode = GlobalConst.STATUS_AUTHORIZATION_DENIED
                return@flatMap responseMessage("Unauthorized to use this endpoint")
            }

            //Handle request
            Authentication.removeTempKey(authState.keyUsed)

            response.rawStatusCode = GlobalConst.STATUS_SUCCESS
            return@flatMap responseMessage("Logged out")
        }.onErrorResume(JSONException::class.java) {
            it.printStackTrace()

            response.rawStatusCode = GlobalConst.STATUS_BAD_REQUEST
            return@onErrorResume responseMessage("Bad Request")
        }.onErrorResume {
            LogFeed.log(LogObject.forException("[API-v2] logout err key err", it, this.javaClass))

            response.rawStatusCode = GlobalConst.STATUS_INTERNAL_ERROR
            return@onErrorResume responseMessage("Internal Server Error")
        }
    }
}
