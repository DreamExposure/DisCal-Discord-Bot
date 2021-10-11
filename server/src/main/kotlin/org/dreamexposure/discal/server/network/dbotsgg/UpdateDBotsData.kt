package org.dreamexposure.discal.server.network.dbotsgg

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.dreamexposure.discal.core.`object`.BotSettings
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.utils.GlobalVal
import org.dreamexposure.discal.server.network.discal.NetworkManager
import org.json.JSONObject
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration

@Component
class UpdateDBotsData(private val networkManager: NetworkManager) : ApplicationRunner {

    private fun update(): Mono<Void> {
        return Mono.fromCallable {
            val json = JSONObject()
                    .put("guildCount", networkManager.getStatus().totalGuilds)
                    .put("shardCount", networkManager.getStatus().expectedShardCount)

            val client = OkHttpClient()

            val body = json.toString().toRequestBody(GlobalVal.JSON)
            val request = Request.Builder()
                    .url("https://discord.bots.gg/api/v1/bots/265523588918935552/stats")
                    .post(body)
                    .header("Authorization", BotSettings.D_BOTS_GG_TOKEN.get())
                    .header("Content-Type", "application/json")
                    .build()

            client.newCall(request).execute()
        }.doOnNext { response ->
            if (response.code != GlobalVal.STATUS_SUCCESS) {
                LOGGER.debug("Failed to update DBots.gg stats | Body: ${response.body?.string()}")
                response.body?.close()
                response.close()
            }
        }.onErrorResume {
            Mono.empty()
        }.then()
    }

    override fun run(args: ApplicationArguments?) {
        if (BotSettings.UPDATE_SITES.get().equals("true", true)) {
            Flux.interval(Duration.ofHours(1))
                    .flatMap { update() }
                    .subscribe()
        }
    }
}
