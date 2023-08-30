package org.dreamexposure.discal.cam.business.cronjob

import kotlinx.coroutines.reactor.mono
import kotlinx.serialization.encodeToString
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.dreamexposure.discal.core.config.Config
import org.dreamexposure.discal.core.extensions.asSeconds
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.`object`.network.discal.InstanceData
import org.dreamexposure.discal.core.`object`.rest.HeartbeatRequest
import org.dreamexposure.discal.core.`object`.rest.HeartbeatType
import org.dreamexposure.discal.core.utils.GlobalVal
import org.dreamexposure.discal.core.utils.GlobalVal.HTTP_CLIENT
import org.dreamexposure.discal.core.utils.GlobalVal.JSON
import org.dreamexposure.discal.core.utils.GlobalVal.JSON_FORMAT
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

@Component
class HeartbeatCronJob: ApplicationRunner {
    private final val apiUrl = Config.URL_API.getString()

    override fun run(args: ApplicationArguments?) {
        Flux.interval(Config.HEARTBEAT_INTERVAL.getLong().asSeconds())
            .flatMap { heartbeat() }
            .doOnError {  LOGGER.error(GlobalVal.DEFAULT, "[Heartbeat] Failed to heartbeat", it) }
            .onErrorResume { Mono.empty() }
            .subscribe()
    }

    private fun heartbeat() = mono {
        val requestBody = HeartbeatRequest(HeartbeatType.CAM, instanceData = InstanceData())

        val request = Request.Builder()
            .url("$apiUrl/v2/status/heartbeat")
            .post(JSON_FORMAT.encodeToString(requestBody).toRequestBody(JSON))
            .header("Authorization", Config.SECRET_DISCAL_API_KEY.getString())
            .header("Content-Type", "application/json")
            .build()

        Mono.fromCallable(HTTP_CLIENT.newCall(request)::execute)
            .map(Response::close)
            .subscribeOn(Schedulers.boundedElastic())
            .doOnError { LOGGER.error(GlobalVal.DEFAULT, "[Heartbeat] Failed to heartbeat", it) }
            .onErrorResume { Mono.empty() }
            .subscribe()
    }
}
