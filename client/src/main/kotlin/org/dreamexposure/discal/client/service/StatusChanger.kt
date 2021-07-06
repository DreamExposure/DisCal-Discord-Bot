package org.dreamexposure.discal.client.service

import discord4j.core.`object`.presence.ClientActivity
import discord4j.core.`object`.presence.ClientPresence
import org.dreamexposure.discal.Application
import org.dreamexposure.discal.client.DisCalClient
import org.dreamexposure.discal.core.database.DatabaseManager
import org.dreamexposure.discal.core.utils.GlobalVal
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.function.TupleUtils
import java.time.Duration
import java.util.concurrent.atomic.AtomicInteger

@Component
class StatusChanger: ApplicationRunner {
    private val index = AtomicInteger(0)

    private val statuses = listOf(
            "Discord Calendar",
            "!help for help",
            "!DisCal for info",
            "Powered by DreamExposure",
            "{guilds} guilds on shard!",
            "{calendars} calendars managed!",
            "{announcements} announcements running!",
            "{shards} total shards!",
            "Version {version}",
            "DisCal is on Patreon!",
    )

    private fun update(): Mono<Void> {
        if (DisCalClient.client == null)
            return Mono.empty()

        val guCountMono = DisCalClient.client!!.guilds.count()
        val calCountMono = DatabaseManager.getCalendarCount()
        val annCountMono = DatabaseManager.getAnnouncementCount()

        return Mono.zip(guCountMono, calCountMono, annCountMono)
                .flatMap(TupleUtils.function { guilds, calendars, announcements ->
                    val currentIndex = index.get()
                    //Update index
                    if (currentIndex + 1 >= statuses.size)
                        index.lazySet(0)
                    else
                        index.lazySet(currentIndex + 1)

                    //Get status we want to change to
                    val status = statuses[currentIndex]
                            .replace("{guilds}", guilds.toString())
                            .replace("{calendars}", calendars.toString())
                            .replace("{announcements}", announcements.toString())
                            .replace("{shards}", Application.getShardCount().toString())
                            .replace("{version}", GlobalVal.version)


                    DisCalClient.client!!.updatePresence(ClientPresence.online(ClientActivity.playing(status)))
                })
    }

    override fun run(args: ApplicationArguments?) {
        Flux.interval(Duration.ofMinutes(5))
                .flatMap { update() }
                .subscribe()
    }
}
