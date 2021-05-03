package org.dreamexposure.discal.server.endpoints.v2.status

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.dreamexposure.discal.core.`object`.network.discal.ConnectedClient
import org.dreamexposure.discal.core.logger.LogFeed
import org.dreamexposure.discal.core.logger.`object`.LogObject
import org.dreamexposure.discal.core.utils.GlobalConst
import org.dreamexposure.discal.server.DisCalServer
import org.dreamexposure.discal.server.utils.Authentication
import org.dreamexposure.discal.server.utils.responseMessage
import org.json.JSONException
import org.json.JSONObject
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/v2/status")
class KeepAliveEndpoint {

    @PostMapping(value = ["/keep-alive"], produces = ["application/json"])
    fun keepAlive(swe: ServerWebExchange, response: ServerHttpResponse, @RequestBody rBody: String): Mono<String> {
        return Authentication.authenticate(swe).flatMap { authState ->
            if (!authState.success) {
                response.rawStatusCode = authState.status
                return@flatMap Mono.just(Json.encodeToString(authState))
            } else if (!authState.fromDiscalNetwork) {
                response.rawStatusCode = GlobalConst.STATUS_AUTHORIZATION_DENIED
                return@flatMap responseMessage("Only official DisCal clients can use this endpoint")
            }

            //Handle request
            val body = JSONObject(rBody)
            val index = body.getInt("index")

            if (DisCalServer.networkInfo.doesClientExist(index)) {
                //In network, update info
                val cc = DisCalServer.networkInfo.getClient(index)
                val oldPid = cc.pid

                val newClient = cc.copy(
                        version = body.optString("version", "Unknown"),
                        d4jVersion = body.optString("d4j_version", "Unknown"),
                        connectedServers = body.getInt("guilds"),
                        lastKeepAlive = System.currentTimeMillis(),
                        uptime = body.getString("uptime"),
                        memUsed = body.getDouble("memory"),
                        ipForRestart = body.getString("ip"),
                        portForRestart = body.getInt("port"),
                        pid = body.getString("pid"),
                )

                if (oldPid != body.getString("pid")) {
                    LogFeed.log(LogObject.forStatus("Client pid changed", "Shard index: ${cc.clientIndex}"))
                }

                DisCalServer.networkInfo.updateClient(newClient)
            } else {
                //Not in network, add info
                val cc = ConnectedClient(
                        index,
                        body.optString("version", "Unknown"),
                        body.optString("d4j_version", "Unknown"),
                        body.getInt("guilds"),
                        System.currentTimeMillis(),
                        body.getString("uptime"),
                        body.getDouble("memory"),
                        body.getString("ip"),
                        body.getInt("port"),
                        body.getString("pid")
                )

                DisCalServer.networkInfo.addClient(cc)
            }

            response.rawStatusCode = GlobalConst.STATUS_SUCCESS
            return@flatMap responseMessage("Success!")
        }.onErrorResume(JSONException::class.java) {
            it.printStackTrace()

            response.rawStatusCode = GlobalConst.STATUS_BAD_REQUEST
            return@onErrorResume responseMessage("Bad Request")
        }.onErrorResume {
            LogFeed.log(LogObject.forException("[API-v2] keep alive err", it, this.javaClass))

            response.rawStatusCode = GlobalConst.STATUS_INTERNAL_ERROR
            return@onErrorResume responseMessage("Internal Server Error")
        }
    }
}
