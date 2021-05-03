package org.dreamexposure.discal.server.network.dbotsgg

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.dreamexposure.discal.core.`object`.BotSettings
import org.dreamexposure.discal.core.logger.LogFeed
import org.dreamexposure.discal.core.logger.`object`.LogObject
import org.dreamexposure.discal.core.utils.GlobalConst
import org.dreamexposure.discal.server.DisCalServer
import org.json.JSONObject
import reactor.core.publisher.Mono
import java.util.*
import kotlin.concurrent.timerTask

object UpdateDBotsData {
    //TODO: Use flux interval instead of timer eventually
    private var timer: Timer? = null

    fun init() {
        if (BotSettings.UPDATE_SITES.get().equals("true", true)) {
            timer = Timer(true)
            timer?.schedule(timerTask {
                update().subscribe()
            }, GlobalConst.oneHourMs)
        }
    }

    fun shutdown() {
        if (timer != null) timer?.cancel()
    }

    private fun update(): Mono<Void> {
        return Mono.fromCallable {
            val json = JSONObject()
                    .put("guildCount", DisCalServer.networkInfo.totalGuildCount)
                    .put("shardCount", DisCalServer.networkInfo.expectedClientCount)

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
}
