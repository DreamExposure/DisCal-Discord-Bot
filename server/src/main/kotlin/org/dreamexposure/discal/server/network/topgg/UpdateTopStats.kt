package org.dreamexposure.discal.server.network.topgg

import org.discordbots.api.client.DiscordBotListAPI
import org.dreamexposure.discal.core.`object`.BotSettings
import org.dreamexposure.discal.core.`object`.network.discal.BotInstanceData
import org.dreamexposure.discal.server.network.discal.NetworkManager
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import java.time.Duration
import java.util.stream.Collectors

@Component
class UpdateTopStats(private val networkManager: NetworkManager): ApplicationRunner {
    private var api: DiscordBotListAPI? = null

    private fun update() {
        if (api != null) {
            val guildsOnShard: List<Int> = networkManager.getStatus().botStatus
                    .stream()
                    .map(BotInstanceData::guilds)
                    .collect(Collectors.toList())

            api?.setStats(guildsOnShard)
        }
    }

    override fun run(args: ApplicationArguments?) {
        if (BotSettings.UPDATE_SITES.get().equals("true", true)) {
            api = DiscordBotListAPI.Builder()
                    .token(BotSettings.TOP_GG_TOKEN.get())
                    .build()

            Flux.interval(Duration.ofHours(1))
                    .map { update() }
                    .subscribe()
        }
    }
}
