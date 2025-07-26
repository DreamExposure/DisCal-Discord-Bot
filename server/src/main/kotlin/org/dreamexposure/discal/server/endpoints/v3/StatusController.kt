package org.dreamexposure.discal.server.endpoints.v3

import kotlinx.coroutines.reactor.mono
import org.dreamexposure.discal.core.annotations.SecurityRequirement
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.`object`.new.model.discal.HeartbeatV3RequestModel
import org.dreamexposure.discal.core.`object`.new.model.discal.NetworkDataV3Model
import org.dreamexposure.discal.core.`object`.new.security.Scope.INTERNAL_HEARTBEAT
import org.dreamexposure.discal.core.`object`.new.security.TokenType.INTERNAL
import org.dreamexposure.discal.core.`object`.rest.GenericResponse
import org.dreamexposure.discal.server.business.NetworkStatusService
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/v3/status")
class StatusController(
    private val networkStatusService: NetworkStatusService,
) {

    @SecurityRequirement(disableSecurity = true, scopes = [])
    @GetMapping(produces = ["application/json"])
    suspend fun get(): NetworkDataV3Model = networkStatusService.getNetworkStatus()

    @SecurityRequirement(schemas = [INTERNAL], scopes = [INTERNAL_HEARTBEAT])
    @PostMapping("/heartbeat", produces = ["application/json"])
    fun post(@RequestBody body: HeartbeatV3RequestModel): Mono<GenericResponse> {
        LOGGER.debug("Heartbeat request received type: {}", body.type)

        return mono {
            when (body.type) {
                HeartbeatV3RequestModel.Type.BOT -> networkStatusService.handleBotHeartbeat(body.bot!!)
                HeartbeatV3RequestModel.Type.WEBSITE -> networkStatusService.handleWebsiteHeartbeat(body.instance!!)
                HeartbeatV3RequestModel.Type.CAM -> networkStatusService.handleCamHeartbeat(body.instance!!)
            }

            return@mono GenericResponse("Success!")
        }
    }
}
