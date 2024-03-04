package org.dreamexposure.discal.client.business.cronjob

import discord4j.core.GatewayDiscordClient
import discord4j.core.`object`.presence.ClientActivity
import discord4j.core.`object`.presence.ClientPresence
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.reactor.mono
import org.dreamexposure.discal.Application
import org.dreamexposure.discal.GitProperty
import org.dreamexposure.discal.core.business.AnnouncementService
import org.dreamexposure.discal.core.business.CalendarService
import org.dreamexposure.discal.core.business.MetricService
import org.dreamexposure.discal.core.config.Config
import org.dreamexposure.discal.core.extensions.asMinutes
import org.dreamexposure.discal.core.logger.LOGGER
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import org.springframework.util.StopWatch
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.concurrent.atomic.AtomicInteger

@Component
class StatusUpdateCronJob(
    private val discordClient: GatewayDiscordClient,
    private val calendarService: CalendarService,
    private val announcementService: AnnouncementService,
    private val metricService: MetricService,
): ApplicationRunner {
    private val index = AtomicInteger(0)

    private final val status = listOf(
        "/discal for info & help",
        "Trans rights are human rights",
        "Version {version}",
        "{calendar_count} calendars managed!",
        "Now has interactions!",
        "Proudly written in Kotlin using Discord4J",
        "Free Palestine!",
        "https://discalbot.com",
        "I swear DisCal isn't abandoned",
        "Powered by Discord4J v{d4j_version}",
        "{shards} total shards!",
        "Slava Ukraini!",
        "Support DisCal on Patreon",
        "{announcement_count} announcements running!",
        "Finally fixing the annoying stuff"
    )

    override fun run(args: ApplicationArguments?) {
        Flux.interval(Config.TIMING_BOT_STATUS_UPDATE_MINUTES.getLong().asMinutes())
            .onBackpressureDrop()
            .flatMap { update() }
            .doOnError { LOGGER.error("Failed to update status", it) }
            .onErrorResume { Mono.empty()}
            .subscribe()
    }

    private fun update() = mono {
        val taskTimer = StopWatch()
        taskTimer.start()

        val currentIndex = index.get()
        // Update index
        if (currentIndex + 1 >= status.size) index.lazySet(0)
        else index.lazySet(currentIndex + 1)

        // Get status to change to
        var status = status[currentIndex]
            .replace("{version}", GitProperty.DISCAL_VERSION.value)
            .replace("{d4j_version}", GitProperty.DISCAL_VERSION_D4J.value)
            .replace("{shards}", Application.getShardCount().toString())

        if (status.contains("{calendar_count}")) {
            val count = calendarService.getCalendarCount()
            status = status.replace("{calendar_count}", count.toString())
        }
        if (status.contains("{announcement_count}")) {
            val count = announcementService.getAnnouncementCount()
            status = status.replace("{announcement_count}", count.toString())
        }

        discordClient.updatePresence(ClientPresence.online(ClientActivity.playing(status))).awaitSingleOrNull()

        taskTimer.stop()
        metricService.recordTaskDuration("status_update", duration = taskTimer.totalTimeMillis)
    }
}
