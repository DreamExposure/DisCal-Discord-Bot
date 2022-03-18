package org.dreamexposure.discal.server.endpoints.v3

import org.dreamexposure.discal.core.`object`.network.discal.NetworkData
import org.dreamexposure.discal.core.annotations.Authentication
import org.dreamexposure.discal.server.network.discal.NetworkManager
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/v3/")
class StatusEndpoint(val networkManager: NetworkManager) {

    @Authentication(access = Authentication.AccessLevel.PUBLIC)
    @GetMapping("status", produces = ["application/json"])
    fun get(): Mono<NetworkData> {
        return Mono.just(networkManager.getStatus())
    }
}
