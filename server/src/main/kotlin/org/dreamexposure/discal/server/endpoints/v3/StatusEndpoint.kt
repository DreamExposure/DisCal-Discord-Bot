package org.dreamexposure.discal.server.endpoints.v3

import org.dreamexposure.discal.core.`object`.network.discal.NetworkData
import org.dreamexposure.discal.core.`object`.rest.GenericResponse
import org.dreamexposure.discal.core.`object`.rest.HeartbeatRequest
import org.dreamexposure.discal.core.`object`.rest.HeartbeatType
import org.dreamexposure.discal.core.annotations.Authentication
import org.dreamexposure.discal.server.network.discal.NetworkManager
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/v3/")
class StatusEndpoint(val networkManager: NetworkManager) {

    @Authentication(access = Authentication.AccessLevel.PUBLIC)
    @GetMapping("status", produces = ["application/json"])
    fun get(): Mono<NetworkData> {
        return Mono.just(networkManager.getStatus())
    }

    @Authentication(access = Authentication.AccessLevel.ADMIN)
    @PostMapping("status", produces = ["application/json"])
    fun post(@RequestBody body: HeartbeatRequest): Mono<GenericResponse> {
        when (body.type) {
            HeartbeatType.BOT -> networkManager.handleBot(body.botInstanceData!!)
            HeartbeatType.WEBSITE -> networkManager.handleWebsite(body.instanceData!!)
            HeartbeatType.CAM -> networkManager.handleCam(body.instanceData!!)
        }
        return GenericResponse("Success!").asMono()
    }
}
