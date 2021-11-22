package org.dreamexposure.discal.client.service

import discord4j.core.`object`.entity.Guild
import discord4j.core.spec.MessageEditSpec
import discord4j.rest.http.client.ClientException
import org.dreamexposure.discal.Application
import org.dreamexposure.discal.Application.Companion.getShardIndex
import org.dreamexposure.discal.client.DisCalClient
import org.dreamexposure.discal.client.message.embed.CalendarEmbed
import org.dreamexposure.discal.core.`object`.StaticMessage
import org.dreamexposure.discal.core.database.DatabaseManager
import org.dreamexposure.discal.core.extensions.discord4j.getCalendar
import org.dreamexposure.discal.core.extensions.discord4j.getSettings
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.utils.GlobalVal
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.function.TupleUtils
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit

@Component
class StaticMessageService : ApplicationRunner {
    //TODO: use gateway client from DI once available

    override fun run(args: ApplicationArguments?) {
        Flux.interval(Duration.ofHours(1))
                .flatMap { doMessageUpdateLogic() }
                .onBackpressureBuffer()
                .doOnError { LOGGER.error(GlobalVal.DEFAULT, "!-Static Message Service Error-!", it) }
                .subscribe()
    }


    private fun doMessageUpdateLogic(): Mono<Void> {
        if (DisCalClient.client == null) return Mono.empty()

        return DatabaseManager.getStaticMessagesForShard(Application.getShardCount(), getShardIndex().toInt())
                .flatMapMany { Flux.fromIterable(it) }
                //We have no interest in updating the message so close to its last update
                .filter { Duration.between(Instant.now(), it.lastUpdate).abs().toMinutes() >= 30 }
                // Only update messages in range
                .filter { Duration.between(Instant.now(), it.scheduledUpdate).toMinutes() <= 60 }
                .flatMap { data ->
                    DisCalClient.client!!.getMessageById(data.channelId, data.messageId).flatMap { message ->
                        when (data.type) {
                            StaticMessage.Type.CALENDAR_OVERVIEW -> {
                                val guildMono = DisCalClient.client!!.getGuildById(data.guildId).cache()
                                val setMono = guildMono.flatMap(Guild::getSettings)
                                val calMono = guildMono.flatMap { it.getCalendar(data.calendarNumber) }

                                Mono.zip(guildMono, setMono, calMono).flatMap(
                                        TupleUtils.function { guild, settings, calendar ->
                                            CalendarEmbed.overview(guild, settings, calendar, true).flatMap {
                                                message.edit(MessageEditSpec.builder()
                                                        .embedsOrNull(listOf(it))
                                                        .build()
                                                ).then(DatabaseManager.updateStaticMessage(data.copy(
                                                        lastUpdate = Instant.now(),
                                                        scheduledUpdate = data.scheduledUpdate.plus(1, ChronoUnit.DAYS))
                                                ))
                                            }
                                        })
                            }
                        }
                    }.onErrorResume(ClientException.isStatusCode(404)) {
                        //Message or channel was deleted, delete from database
                        DatabaseManager.deleteStaticMessage(data.guildId, data.messageId)
                    }
                }.doOnError {
                    LOGGER.error(GlobalVal.DEFAULT, "Static message update error", it)
                }.onErrorResume {
                    Mono.empty()
                }.then()
    }
}
