package org.dreamexposure.discal.client.service

import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration

@Component
class AnnouncementService: ApplicationRunner {

    private fun doAnnouncementCycle(): Mono<Void> {


        TODO("Not yet implemented")
    }

    override fun run(args: ApplicationArguments?) {
        Flux.interval(Duration.ofMinutes(5))
            .onBackpressureBuffer()
            .flatMap { doAnnouncementCycle() }
            .subscribe()
    }
}
