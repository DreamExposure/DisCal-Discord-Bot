package org.dreamexposure.discal.server.endpoints.v2.account

import kotlinx.serialization.encodeToString
import org.dreamexposure.discal.core.annotations.SecurityRequirement
import org.dreamexposure.discal.core.crypto.KeyGenerator
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.utils.GlobalVal
import org.dreamexposure.discal.server.utils.Authentication
import org.dreamexposure.discal.server.utils.responseMessage
import org.json.JSONException
import org.json.JSONObject
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/v2/account/key")
class KeyRequestEndpoint {
    @SecurityRequirement(disableSecurity = true, scopes = [])
    @PostMapping("/readonly/get", produces = ["application/json"])
    fun getReadOnlyKey(swe: ServerWebExchange, response: ServerHttpResponse): Mono<String> {
        return Authentication.authenticate(swe).flatMap { authState ->
            if (!authState.success) {
                response.rawStatusCode = authState.status
                return@flatMap Mono.just(GlobalVal.JSON_FORMAT.encodeToString(authState))
            } else if (!authState.fromDiscalNetwork) {
                response.rawStatusCode = GlobalVal.STATUS_AUTHORIZATION_DENIED
                return@flatMap responseMessage("Unauthorized to use this endpoint")
            }

            //Handle request
            val key = KeyGenerator.csRandomAlphaNumericString(64)

            Authentication.saveReadOnlyKey(key)

            //Return key
            val json = JSONObject()
            json.put("key", key)

            response.rawStatusCode = GlobalVal.STATUS_SUCCESS
            return@flatMap Mono.just(json.toString())
        }.onErrorResume(JSONException::class.java) {
            LOGGER.trace("[API-v2] JSON error. Bad request?", it)

            response.rawStatusCode = GlobalVal.STATUS_BAD_REQUEST
            return@onErrorResume responseMessage("Bad Request")
        }.onErrorResume {
            LOGGER.error(GlobalVal.DEFAULT, "[API-v2] get read-only key error", it)

            response.rawStatusCode = GlobalVal.STATUS_INTERNAL_ERROR
            return@onErrorResume responseMessage("Internal Server Error")
        }
    }
}
