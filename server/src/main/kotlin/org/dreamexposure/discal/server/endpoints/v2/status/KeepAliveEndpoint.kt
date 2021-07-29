package org.dreamexposure.discal.server.endpoints.v2.status

import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import org.dreamexposure.discal.core.`object`.network.discal.ConnectedClient
import org.dreamexposure.discal.core.`object`.network.discal.NetworkInfo
import org.dreamexposure.discal.core.`object`.rest.HeartbeatRequest
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.utils.GlobalVal
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
class KeepAliveEndpoint(val networkInfo: NetworkInfo) {

    @PostMapping(value = ["/keep-alive"], produces = ["application/json"])
    fun keepAlive(swe: ServerWebExchange, response: ServerHttpResponse, @RequestBody rBody: String): Mono<String> {
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

            if (networkInfo.doesClientExist(body.clientIndex.toInt())) {
                //In network, update info
                val cc = networkInfo.getClient(body.clientIndex.toInt())
                val oldId = cc.instanceId

                val newClient = cc.copy(
                        expectedClientCount = body.expectedClients,
                        version = body.version,
                        d4jVersion = body.d4jVersion,
                        connectedServers = body.guildCount,
                        lastKeepAlive = System.currentTimeMillis(),
                        uptime = body.uptime,
                        memUsed = body.memory,
                        instanceId = body.instanceId,
                )

                if (oldId != body.instanceId) {
                    LOGGER.info(GlobalVal.STATUS, "Client ID changed | Shard Index: ${cc.clientIndex}")
                }

                networkInfo.updateClient(newClient)
            } else {
                //Not in network, add info
                val cc = ConnectedClient(
                        body.clientIndex.toInt(),
                        body.expectedClients,
                        body.version,
                        body.d4jVersion,
                        body.guildCount,
                        System.currentTimeMillis(),
                        body.uptime,
                        body.memory,
                        body.instanceId
                )

                networkInfo.addClient(cc)
            }

            response.rawStatusCode = GlobalVal.STATUS_SUCCESS
            return@flatMap responseMessage("Success!")
        }.onErrorResume(SerializationException::class.java) {
            LOGGER.trace("[API-v2] JSON error. Bad request?", it)

            response.rawStatusCode = GlobalVal.STATUS_BAD_REQUEST
            return@onErrorResume responseMessage("Bad Request")
        }.onErrorResume {
            LOGGER.error(GlobalVal.DEFAULT, "[API-v2] keep alive error", it)

            response.rawStatusCode = GlobalVal.STATUS_INTERNAL_ERROR
            return@onErrorResume responseMessage("Internal Server Error")
        }
    }
}
