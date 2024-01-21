package org.dreamexposure.discal.server.endpoints.v2.status

import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import org.dreamexposure.discal.core.annotations.SecurityRequirement
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.utils.GlobalVal
import org.dreamexposure.discal.server.network.discal.NetworkManager
import org.dreamexposure.discal.server.utils.Authentication
import org.dreamexposure.discal.server.utils.responseMessage
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/v2/status")
class GetStatusEndpoint(val networkManager: NetworkManager) {

    @PostMapping("/get", produces = ["application/json"])
    @SecurityRequirement(disableSecurity = true, scopes = [])
    fun getStatus(swe: ServerWebExchange, response: ServerHttpResponse): Mono<String> {
        return Authentication.authenticate(swe).map { authState ->
            if (!authState.success) {
                response.rawStatusCode = authState.status
                return@map GlobalVal.JSON_FORMAT.encodeToString(authState)
            }

            //Handle request
            response.rawStatusCode = GlobalVal.STATUS_SUCCESS
            return@map GlobalVal.JSON_FORMAT.encodeToString(networkManager.getStatus())
        }.onErrorResume(SerializationException::class.java) {
            LOGGER.trace("[API-v2] JSON error. Bad request?", it)

            response.rawStatusCode = GlobalVal.STATUS_BAD_REQUEST
            return@onErrorResume responseMessage("Bad Request")
        }.onErrorResume {
            LOGGER.error(GlobalVal.DEFAULT, "[API-v2] get status error", it)

            response.rawStatusCode = GlobalVal.STATUS_INTERNAL_ERROR
            return@onErrorResume responseMessage("Internal Server Error")
        }
    }
}
