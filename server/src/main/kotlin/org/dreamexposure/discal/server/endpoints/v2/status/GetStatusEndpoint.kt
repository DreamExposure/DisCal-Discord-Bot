package org.dreamexposure.discal.server.endpoints.v2.status

import kotlinx.coroutines.reactor.mono
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import org.dreamexposure.discal.core.annotations.SecurityRequirement
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.`object`.network.discal.BotInstanceData
import org.dreamexposure.discal.core.`object`.network.discal.InstanceData
import org.dreamexposure.discal.core.`object`.network.discal.NetworkData
import org.dreamexposure.discal.core.utils.GlobalVal
import org.dreamexposure.discal.server.business.NetworkStatusService
import org.dreamexposure.discal.server.utils.Authentication
import org.dreamexposure.discal.server.utils.responseMessage
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.time.Instant

@RestController
@RequestMapping("/v2/status")
class GetStatusEndpoint(
    private val networkStatusService: NetworkStatusService,
    private val authentication: Authentication,
) {

    @PostMapping("/get", produces = ["application/json"])
    @SecurityRequirement(disableSecurity = true, scopes = [])
    fun getStatus(swe: ServerWebExchange, response: ServerHttpResponse): Mono<String> {
        return authentication.authenticate(swe).flatMap { authState ->
            if (!authState.success) {
                response.rawStatusCode = authState.status
                return@flatMap Mono.just(GlobalVal.JSON_FORMAT.encodeToString(authState))
            }

            //Handle request
            response.rawStatusCode = GlobalVal.STATUS_SUCCESS

            // Build the legacy object
            return@flatMap mono { networkStatusService.getNetworkStatus() }
                .map { data ->
                    NetworkData(totalCalendars = data.totalCalendars,
                        totalAnnouncements = data.totalAnnouncements,
                        apiStatus = data.apiStatus.map {
                            InstanceData(
                                instanceId = it.instanceId,
                                version = it.version,
                                d4jVersion = it.d4jVersion,
                                uptime = it.uptime,
                                lastHeartbeat = Instant.parse(it.lastHeartbeat),
                                memory = it.memory
                            )
                        }.first(),
                        websiteStatus = if (data.websiteStatus == null) null else InstanceData(
                            instanceId = data.websiteStatus!!.instanceId,
                            version = data.websiteStatus!!.version,
                            d4jVersion = data.websiteStatus!!.d4jVersion,
                            uptime = data.websiteStatus!!.uptime,
                            lastHeartbeat = Instant.parse(data.websiteStatus!!.lastHeartbeat),
                            memory = data.websiteStatus!!.memory
                        ),
                        camStatus = data.camStatus.map {
                            InstanceData(
                                instanceId = it.instanceId,
                                version = it.version,
                                d4jVersion = it.d4jVersion,
                                uptime = it.uptime,
                                lastHeartbeat = Instant.parse(it.lastHeartbeat),
                                memory = it.memory
                            )
                        }.toMutableList(),
                        botStatus = data.botStatus.map {
                            BotInstanceData(
                                instanceData = InstanceData(
                                    instanceId = it.instanceData.instanceId,
                                    version = it.instanceData.version,
                                    d4jVersion = it.instanceData.d4jVersion,
                                    uptime = it.instanceData.uptime,
                                    lastHeartbeat = Instant.parse(it.instanceData.lastHeartbeat),
                                    memory = it.instanceData.memory
                                ),
                                shardIndex = it.shardIndex,
                                shardCount = it.shardCount,
                                guilds = it.guilds,
                            )
                        }.toMutableList())
                }.map { GlobalVal.JSON_FORMAT.encodeToString(it) }
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
