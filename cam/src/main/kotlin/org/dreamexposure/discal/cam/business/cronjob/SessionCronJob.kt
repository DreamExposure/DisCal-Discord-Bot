package org.dreamexposure.discal.cam.business.cronjob

import kotlinx.coroutines.reactor.mono
import org.dreamexposure.discal.core.business.SessionService
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.utils.GlobalVal
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration

@Component
class SessionCronJob(
    val sessionService: SessionService,
) : ApplicationRunner {
    override fun run(args: ApplicationArguments?) {
        Flux.interval(Duration.ofHours(1))
            .flatMap { justDoIt() }
            .doOnError { LOGGER.error(GlobalVal.DEFAULT, "Session cronjob error", it) }
            .onErrorResume { Mono.empty() }
            .subscribe()
    }

    private fun justDoIt() = mono {
        LOGGER.debug("Running expired session purge job")

        sessionService.deleteExpiredSessions()
    }
}
