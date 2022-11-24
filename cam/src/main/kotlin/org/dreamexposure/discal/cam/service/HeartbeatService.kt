package org.dreamexposure.discal.cam.service

import kotlinx.serialization.encodeToString
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.`object`.BotSettings
import org.dreamexposure.discal.core.`object`.network.discal.InstanceData
import org.dreamexposure.discal.core.`object`.rest.HeartbeatRequest
import org.dreamexposure.discal.core.`object`.rest.HeartbeatType
import org.dreamexposure.discal.core.utils.GlobalVal
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.time.Duration

@Component
class HeartbeatService(
    @Value("\${bot.url.api}")
    private val apiUrl: String,
) : ApplicationRunner {

    private fun heartbeat(): Mono<Void> {
        return Mono.just(InstanceData())
                .map { data ->
                    val requestBody = HeartbeatRequest(HeartbeatType.CAM, instanceData = data)

                    val body = GlobalVal.JSON_FORMAT.encodeToString(requestBody).toRequestBody(GlobalVal.JSON)

                    Request.Builder()
                            .url("$apiUrl/v2/status/heartbeat")
                            .post(body)
                            .header("Authorization", BotSettings.BOT_API_TOKEN.get())
                            .header("Content-Type", "application/json")
                            .build()
                }.flatMap {
                    Mono.fromCallable(GlobalVal.HTTP_CLIENT.newCall(it)::execute)
                            .subscribeOn(Schedulers.boundedElastic())
                            .map(Response::close)
                }.doOnError {
                    LOGGER.error(GlobalVal.DEFAULT, "[Heartbeat] Failed to heartbeat", it)
                }.onErrorResume { Mono.empty() }
                .then()

    }

    override fun run(args: ApplicationArguments?) {
        Flux.interval(Duration.ofMinutes(2))
                .flatMap { heartbeat() }
                .subscribe()
    }
}
