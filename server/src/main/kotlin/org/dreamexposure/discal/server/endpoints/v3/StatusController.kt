package org.dreamexposure.discal.server.endpoints.v3

import org.dreamexposure.discal.core.annotations.Authentication
import org.dreamexposure.discal.core.`object`.network.discal.NetworkData
import org.dreamexposure.discal.core.`object`.rest.GenericResponse
import org.dreamexposure.discal.core.`object`.rest.HeartbeatRequest
import org.dreamexposure.discal.core.`object`.rest.HeartbeatType
import org.dreamexposure.discal.server.network.discal.NetworkManager
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v3/status")
class StatusController(val networkManager: NetworkManager) {

    @Authentication(access = Authentication.AccessLevel.PUBLIC)
    @GetMapping(produces = ["application/json"])
    fun get(): NetworkData = networkManager.getStatus()

    @Authentication(access = Authentication.AccessLevel.ADMIN)
    @PostMapping(produces = ["application/json"])
    fun post(@RequestBody body: HeartbeatRequest): GenericResponse {
        when (body.type) {
            HeartbeatType.BOT -> networkManager.handleBot(body.botInstanceData!!)
            HeartbeatType.WEBSITE -> networkManager.handleWebsite(body.instanceData!!)
            HeartbeatType.CAM -> networkManager.handleCam(body.instanceData!!)
        }
        return GenericResponse("Success!")
    }
}
