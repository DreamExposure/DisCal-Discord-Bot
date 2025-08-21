package org.dreamexposure.discal.server.business.cronjob

import kotlinx.coroutines.reactor.mono
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.utils.GlobalVal
import org.dreamexposure.discal.server.business.NetworkStatusService
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration

@Component
class NetworkStatusCronJob(
    private val networkStatusService: NetworkStatusService,
): ApplicationRunner {

    override fun run(args: ApplicationArguments?) {
        Flux.interval(Duration.ofMinutes(1))
            .flatMap { mono { networkStatusService.doNetworkStatusHealthCheck() } }
            .doOnError { LOGGER.error(GlobalVal.DEFAULT, "[NetworkStatus] Network status cronjob failure", it) }
            .onErrorResume { Mono.empty() }
            .subscribe()
    }
}