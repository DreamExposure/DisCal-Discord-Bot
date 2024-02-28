package org.dreamexposure.discal.client.business.cronjob

import kotlinx.coroutines.reactor.mono
import org.dreamexposure.discal.Application.Companion.getShardCount
import org.dreamexposure.discal.Application.Companion.getShardIndex
import org.dreamexposure.discal.core.business.MetricService
import org.dreamexposure.discal.core.business.StaticMessageService
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.utils.GlobalVal.DEFAULT
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import org.springframework.util.StopWatch
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration
import java.time.Instant

@Component
class StaticMessageUpdateCronJob(
    private val staticMessageService: StaticMessageService,
    private val metricService: MetricService,
):ApplicationRunner {
    override fun run(args: ApplicationArguments?) {
        Flux.interval(Duration.ofHours(1))
            .onBackpressureDrop()
            .flatMap { doUpdate() }
            .onErrorResume { Mono.empty() }
            .subscribe()
    }

    private fun doUpdate() = mono {
        val taskTimer = StopWatch()
        taskTimer.start()

        try {
            val messages = staticMessageService.getStaticMessagesForShard(getShardIndex(), getShardCount())
                //We have no interest in updating the message so close to its last update
                .filter { Duration.between(Instant.now(), it.lastUpdate).abs().toMinutes() >= 30 }
                // Only update messages in range
                .filter { Duration.between(Instant.now(), it.scheduledUpdate).toMinutes() <= 60 }

            LOGGER.debug("StaticMessageUpdateCronJob | Found ${messages.size} messages to update for shard ${getShardIndex()}")

            messages.forEach {
                try {
                    staticMessageService.updateStaticMessage(it.guildId, it.messageId)
                } catch (ex: Exception) {
                    LOGGER.error("Failed to update static message | guildId:${it.guildId} | messageId:${it.messageId}", ex)
                }
            }
        } catch (ex: Exception) {
            LOGGER.error(DEFAULT, "StaticMessageUpdateCronJob failure", ex)
        } finally {
            taskTimer.stop()
            metricService.recordStaticMessageTaskDuration("overall", taskTimer.totalTimeMillis)
        }
    }
}
