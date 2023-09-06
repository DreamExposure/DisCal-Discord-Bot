package org.dreamexposure.discal.client.service

import discord4j.core.GatewayDiscordClient
import kotlinx.serialization.encodeToString
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.dreamexposure.discal.core.config.Config
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.`object`.network.discal.BotInstanceData
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
import java.time.Duration

@Component
class HeartbeatService(
    private val discordClient: GatewayDiscordClient,
) : ApplicationRunner {
    private final val apiUrl = Config.URL_API.getString()

    private fun heartbeat(): Mono<Void> {
        return BotInstanceData.load(discordClient)
                .map { data ->
                    val requestBody = HeartbeatRequest(HeartbeatType.BOT, botInstanceData = data)

                    val body = JSON_FORMAT.encodeToString(requestBody).toRequestBody(JSON)

                    Request.Builder()
                        .url("$apiUrl/v2/status/heartbeat")
                        .post(body)
                        .header("Authorization", Config.SECRET_DISCAL_API_KEY.getString())
                            .header("Content-Type", "application/json")
                            .build()
                }.flatMap {
                    Mono.fromCallable(HTTP_CLIENT.newCall(it)::execute)
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
