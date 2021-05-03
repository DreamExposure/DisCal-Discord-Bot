package org.dreamexposure.discal.server.network.topgg

import org.discordbots.api.client.DiscordBotListAPI
import org.dreamexposure.discal.core.`object`.BotSettings
import org.dreamexposure.discal.core.`object`.network.discal.ConnectedClient
import org.dreamexposure.discal.core.utils.GlobalConst
import org.dreamexposure.discal.server.DisCalServer
import java.util.*
import java.util.stream.Collectors
import kotlin.concurrent.timerTask

object UpdateTopStats {
    //TODO: Use flux interval instead of timer eventually
    private var timer: Timer? = null
    private var api: DiscordBotListAPI? = null

    fun init() {
        if (BotSettings.UPDATE_SITES.get().equals("true", true)) {
            api = DiscordBotListAPI.Builder()
                    .token(BotSettings.TOP_GG_TOKEN.get())
                    .build()

            timer = Timer(true)

            timer?.schedule(timerTask {
                update()
            }, GlobalConst.oneHourMs)
        }
    }

    fun shutdown() {
        if (timer != null) timer?.cancel()
    }

    private fun update() {
        if (api != null) {
            val guildsOnShard: List<Int> = DisCalServer.networkInfo.clients
                    .stream()
                    .map(ConnectedClient::connectedServers)
                    .collect(Collectors.toList())

            api?.setStats(guildsOnShard)
        }
    }
}
