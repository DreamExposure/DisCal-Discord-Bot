package org.dreamexposure.discal.server.network.dbotsgg

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.dreamexposure.discal.core.`object`.BotSettings
import org.dreamexposure.discal.core.`object`.network.discal.NetworkInfo
import org.dreamexposure.discal.core.logger.LogFeed
import org.dreamexposure.discal.core.logger.`object`.LogObject
import org.dreamexposure.discal.core.utils.GlobalConst
import org.json.JSONObject
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration

@Component
class UpdateDBotsData(private val networkInfo: NetworkInfo) : ApplicationRunner {

    private fun update(): Mono<Void> {
        return Mono.fromCallable {
            val json = JSONObject()
                    .put("guildCount", networkInfo.totalGuildCount)
                    .put("shardCount", networkInfo.expectedClientCount)

            val client = OkHttpClient()

            val body = RequestBody.create(GlobalConst.JSON, json.toString())
            val request = Request.Builder()
                    .url("https://discord.bots.gg/api/v1/bots/265523588918935552/stats")
                    .post(body)
                    .header("Authorization", BotSettings.D_BOTS_GG_TOKEN.get())
                    .header("Content-Type", "application/json")
                    .build()

            client.newCall(request).execute()
        }.doOnNext { response ->
            if (response.code() != GlobalConst.STATUS_SUCCESS) {
                LogFeed.log(LogObject.forDebug("Failed to update DBots.gg stats", "Body: ${response.body()?.string()}"))
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
