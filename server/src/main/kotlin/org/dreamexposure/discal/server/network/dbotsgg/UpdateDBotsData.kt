package org.dreamexposure.discal.server.network.dbotsgg

import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.reactor.mono
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.dreamexposure.discal.core.config.Config
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.utils.GlobalVal
import org.dreamexposure.discal.server.business.NetworkStatusService
import org.json.JSONObject
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration

@Component
@ConditionalOnProperty("bot.integrations.update-bot-sites", havingValue = "true")
class UpdateDBotsData(
    private val networkStatusService: NetworkStatusService,
    private val httpClient: OkHttpClient
) : ApplicationRunner {
    private val token = Config.SECRET_INTEGRATION_D_BOTS_GG_TOKEN.getString()

    private fun update() = mono {
        val status = networkStatusService.getNetworkStatus()

        Mono.fromCallable {
            val json = JSONObject()
                .put("guildCount", status.totalGuildsCount)
                .put("shardCount", status.expectedShardCount)

            val body = json.toString().toRequestBody(GlobalVal.JSON)
            val request = Request.Builder()
                .url("https://discord.bots.gg/api/v1/bots/265523588918935552/stats")
                .post(body)
                .header("Authorization", token)
                .header("Content-Type", "application/json")
                .build()

            httpClient.newCall(request).execute()
        }.doOnNext { response ->
            if (response.code != GlobalVal.STATUS_SUCCESS) {
                LOGGER.debug("Failed to update DBots.gg stats | Body: ${response.body.string()}")
                response.body.close()
                response.close()
            }
        }.onErrorResume {
            Mono.empty()
        }.awaitSingleOrNull()
    }

    override fun run(args: ApplicationArguments?) {
        Flux.interval(Duration.ofHours(1))
            .flatMap { update() }
            .subscribe()
    }
}
