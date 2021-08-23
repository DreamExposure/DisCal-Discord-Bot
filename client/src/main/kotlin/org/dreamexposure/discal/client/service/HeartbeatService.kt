package org.dreamexposure.discal.client.service

import discord4j.core.GatewayDiscordClient
import kotlinx.serialization.encodeToString
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.dreamexposure.discal.Application
import org.dreamexposure.discal.GitProperty
import org.dreamexposure.discal.client.DisCalClient
import org.dreamexposure.discal.core.`object`.BotSettings
import org.dreamexposure.discal.core.`object`.rest.HeartbeatRequest
import org.dreamexposure.discal.core.logger.LOGGER
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
import kotlin.math.roundToInt

@Component
class HeartbeatService : ApplicationRunner {

    private fun heartbeat(): Mono<Void> {
        return Mono.justOrEmpty<GatewayDiscordClient>(DisCalClient.client)
                .flatMap { it.guilds.count() }
                .map { it.toInt() }
                .defaultIfEmpty(0)
                .map { guildCount ->
                    val requestBody = HeartbeatRequest(
                            instanceId = Application.instanceId,
                            clientIndex = Application.getShardIndex(),
                            expectedClients = Application.getShardCount(),
                            guildCount = guildCount,
                            memory = usedMemory(),
                            uptime = Application.getHumanReadableUptime(),
                            version = GitProperty.DISCAL_VERSION.value,
                            d4jVersion = GitProperty.DISCAL_VERSION_D4J.value
                    )

                    val body = JSON_FORMAT.encodeToString(requestBody).toRequestBody(JSON)

                    Request.Builder()
                            .url("${BotSettings.API_URL.get()}/v2/status/keep-alive")
                            .post(body)
                            .header("Authorization", BotSettings.BOT_API_TOKEN.get())
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

    private fun usedMemory(): Double {
        val totalMemory = Runtime.getRuntime().totalMemory()
        val freeMemory = Runtime.getRuntime().freeMemory()

        val raw = (totalMemory - freeMemory) / (1024 * 1024).toDouble()

        return (raw * 100).roundToInt().toDouble() / 100
    }

    override fun run(args: ApplicationArguments?) {
        Flux.interval(Duration.ofMinutes(1))
                .flatMap { heartbeat() }
                .subscribe()
    }
}
