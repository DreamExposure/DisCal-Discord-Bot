package org.dreamexposure.discal.client.business.cronjob

import com.fasterxml.jackson.databind.ObjectMapper
import discord4j.core.GatewayDiscordClient
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.dreamexposure.discal.core.config.Config
import org.dreamexposure.discal.core.extensions.asSeconds
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.`object`.new.model.discal.BotInstanceDataV3Model
import org.dreamexposure.discal.core.`object`.new.model.discal.HeartbeatV3RequestModel
import org.dreamexposure.discal.core.utils.GlobalVal
import org.dreamexposure.discal.core.utils.GlobalVal.DEFAULT
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

@Component
class HeartbeatCronJob(
    private val discordClient: GatewayDiscordClient,
    private val httpClient: OkHttpClient,
    private val objectMapper: ObjectMapper,
): ApplicationRunner {
    private final val apiUrl = Config.URL_API.getString()

    override fun run(args: ApplicationArguments?) {
        Flux.interval(Config.HEARTBEAT_INTERVAL.getLong().asSeconds())
            .flatMap { heartbeat() }
            .doOnError {  LOGGER.error(DEFAULT, "[Heartbeat] Failed to heartbeat", it) }
            .onErrorResume { Mono.empty() }
            .subscribe()
    }

    private fun heartbeat(): Mono<Void> {
        LOGGER.debug("Bot heart beating...")

        return discordClient.guilds.count().map(Long::toInt).map { guildCount ->
            val requestBody = HeartbeatV3RequestModel(HeartbeatV3RequestModel.Type.BOT, bot = BotInstanceDataV3Model(guilds = guildCount))

           Request.Builder()
                .url("$apiUrl/v3/status/heartbeat")
                .post(objectMapper.writeValueAsString(requestBody).toRequestBody(GlobalVal.JSON))
                .header("Authorization", "Int ${Config.SECRET_DISCAL_API_KEY.getString()}")
                .header("Content-Type", "application/json")
                .build()
        }
            .flatMap { Mono.fromCallable(httpClient.newCall(it)::execute) }
            .map(Response::close)
            .then()
            .subscribeOn(Schedulers.boundedElastic())
            .doOnError { LOGGER.error(DEFAULT, "[Heartbeat] Failed to heartbeat", it) }
            .onErrorResume { Mono.empty() }
    }
}
