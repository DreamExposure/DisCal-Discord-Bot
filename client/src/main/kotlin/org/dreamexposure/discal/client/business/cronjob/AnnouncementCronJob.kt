package org.dreamexposure.discal.client.business.cronjob

import discord4j.core.GatewayDiscordClient
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.mono
import org.dreamexposure.discal.core.business.AnnouncementService
import org.dreamexposure.discal.core.business.MetricService
import org.dreamexposure.discal.core.config.Config
import org.dreamexposure.discal.core.extensions.asMinutes
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.utils.GlobalVal.DEFAULT
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import org.springframework.util.StopWatch
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
class AnnouncementCronJob(
    private val discordClient: GatewayDiscordClient,
    private val announcementService: AnnouncementService,
    private val metricService: MetricService,
) : ApplicationRunner {
    private val interval = Config.TIMING_ANNOUNCEMENT_TASK_RUN_INTERVAL_MINUTES.getLong().asMinutes()
    private val maxDifference = interval

    override fun run(args: ApplicationArguments?) {
        Flux.interval(interval)
            .onBackpressureDrop()
            .flatMap { doAction() }
            .doOnError { LOGGER.error(DEFAULT, "!-Announcement run error-! Failed to process announcements for all guilds", it) }
            .onErrorResume { Mono.empty() }
            .subscribe()
    }

    private fun doAction() = mono {
        val taskTimer = StopWatch()
        taskTimer.start()

        val guilds = discordClient.guilds.collectList().awaitSingle()

        guilds.forEach { guild ->
            try {
                announcementService.processAnnouncementsForGuild(guild.id, maxDifference)
            } catch (ex: Exception) {
                LOGGER.error("Failed to process announcements for guild | guildId:${guild.id.asLong()}", ex)
            }
        }
        taskTimer.stop()
        metricService.recordAnnouncementTaskDuration("cronjob", taskTimer.totalTimeMillis)
    }
}
