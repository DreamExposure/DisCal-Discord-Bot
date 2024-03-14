package org.dreamexposure.discal.core.health

import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.mono
import org.dreamexposure.discal.core.database.CalendarRepository
import org.dreamexposure.discal.core.logger.LOGGER
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.ReactiveHealthIndicator
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class DisCalDbReactiveHealthIndicator(
    private val calendarRepository: CalendarRepository,
): ReactiveHealthIndicator {
    override fun health(): Mono<Health> = mono {
        return@mono try {
            calendarRepository.healthCheck().awaitSingle()

            Health.up().build()
        } catch (ex: Exception) {
            LOGGER.error("DisCal database health check failed!", ex)

            Health.outOfService().withException(ex).build()
        }
    }
}
