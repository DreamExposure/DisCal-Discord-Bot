package org.dreamexposure.discal.cam.service

import org.dreamexposure.discal.core.database.DatabaseManager
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.utils.GlobalVal
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration

@Component
class SessionService : ApplicationRunner {
    override fun run(args: ApplicationArguments?) {
        Flux.interval(Duration.ofHours(24))
            .flatMap {
                DatabaseManager.deleteExpiredSessions()
            }.doOnError {
                LOGGER.error(GlobalVal.DEFAULT, "Session Service runner error", it)
            }.onErrorResume {
                Mono.empty()
            }.subscribe()
    }
}
