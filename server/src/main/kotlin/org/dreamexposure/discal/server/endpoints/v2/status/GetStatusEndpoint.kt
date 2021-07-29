package org.dreamexposure.discal.server.endpoints.v2.status

import kotlinx.serialization.encodeToString
import org.dreamexposure.discal.core.`object`.network.discal.NetworkInfo
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.utils.GlobalVal
import org.dreamexposure.discal.server.utils.Authentication
import org.dreamexposure.discal.server.utils.responseMessage
import org.json.JSONException
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/v2/status")
class GetStatusEndpoint(val networkInfo: NetworkInfo) {

    @PostMapping("/get", produces = ["application/json"])
    fun getStatus(swe: ServerWebExchange, response: ServerHttpResponse): Mono<String> {
        return Authentication.authenticate(swe).flatMap { authState ->
            if (!authState.success) {
                response.rawStatusCode = authState.status
                return@flatMap Mono.just(GlobalVal.JSON_FORMAT.encodeToString(authState))
            }

            //Handle request
            response.rawStatusCode = GlobalVal.STATUS_SUCCESS
            return@flatMap networkInfo.update()
                    .thenReturn(networkInfo.toJson().toString())
        }.onErrorResume(JSONException::class.java) {
            it.printStackTrace()

            response.rawStatusCode = GlobalVal.STATUS_BAD_REQUEST
            return@onErrorResume responseMessage("Bad Request")
        }.onErrorResume {
            LOGGER.error(GlobalVal.DEFAULT, "[API-v2] get status error", it)

            response.rawStatusCode = GlobalVal.STATUS_INTERNAL_ERROR
            return@onErrorResume responseMessage("Internal Server Error")
        }
    }
}
