package org.dreamexposure.discal.server.endpoints.v2.status

import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import org.dreamexposure.discal.core.`object`.rest.HeartbeatRequest
import org.dreamexposure.discal.core.`object`.rest.HeartbeatType
import org.dreamexposure.discal.core.annotations.Authentication.*
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.utils.GlobalVal
import org.dreamexposure.discal.server.network.discal.NetworkManager
import org.dreamexposure.discal.server.utils.Authentication
import org.dreamexposure.discal.server.utils.responseMessage
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/v2/status")
class HeartbeatEndpoint(val networkManager: NetworkManager) {

    @PostMapping(value = ["/heartbeat"], produces = ["application/json"])
    @org.dreamexposure.discal.core.annotations.Authentication(access = AccessLevel.PUBLIC)
    fun heartbeat(swe: ServerWebExchange, response: ServerHttpResponse, @RequestBody rBody: String): Mono<String> {
        return Authentication.authenticate(swe).flatMap { authState ->
            if (!authState.success) {
                response.rawStatusCode = authState.status
                return@flatMap Mono.just(GlobalVal.JSON_FORMAT.encodeToString(authState))
            } else if (!authState.fromDiscalNetwork) {
                response.rawStatusCode = GlobalVal.STATUS_AUTHORIZATION_DENIED
                return@flatMap responseMessage("Only official DisCal clients can use this endpoint")
            }

            //Handle request
            val body: HeartbeatRequest = GlobalVal.JSON_FORMAT.decodeFromString(rBody)

            when (body.type) {
                HeartbeatType.BOT -> networkManager.handleBot(body.botInstanceData!!)
                HeartbeatType.WEBSITE -> networkManager.handleWebsite(body.instanceData!!)
                HeartbeatType.CAM -> networkManager.handleCam(body.instanceData!!)
            }

            response.rawStatusCode = GlobalVal.STATUS_SUCCESS
            return@flatMap responseMessage("Success!")
        }.onErrorResume(SerializationException::class.java) {
            LOGGER.trace("[API-v2] JSON error. Bad request?", it)

            response.rawStatusCode = GlobalVal.STATUS_BAD_REQUEST
            return@onErrorResume responseMessage("Bad Request")
        }.onErrorResume {
            LOGGER.error(GlobalVal.DEFAULT, "[API-v2] Heartbeat error", it)

            response.rawStatusCode = GlobalVal.STATUS_INTERNAL_ERROR
            return@onErrorResume responseMessage("Internal Server Error")
        }
    }
}
