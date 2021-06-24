package org.dreamexposure.discal.server.network.discal

import org.dreamexposure.discal.core.`object`.BotSettings
import org.dreamexposure.discal.core.`object`.network.discal.ConnectedClient
import org.dreamexposure.discal.core.`object`.network.discal.NetworkInfo
import org.dreamexposure.discal.core.utils.GlobalConst
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration

@Component
class NetworkMediator(private val networkInfo: NetworkInfo) : ApplicationRunner {

    private fun issueRestart(client: ConnectedClient): Mono<Void> {
        if (!BotSettings.USE_RESTART_SERVICE.get().equals("true", true)) {
            networkInfo.removeClient(client.clientIndex, "Restart service not active!")
        } else {
            //TODO: Actually support restarting clients automatically one day
        }
        return Mono.empty()
    }


    override fun run(args: ApplicationArguments?) {
        Flux.interval(Duration.ofMinutes(1))
                .flatMapIterable { networkInfo.clients }
                .filter { System.currentTimeMillis() > it.lastKeepAlive + (5 * GlobalConst.oneMinuteMs) }
                .flatMap(::issueRestart)
                .subscribe()
    }
}
