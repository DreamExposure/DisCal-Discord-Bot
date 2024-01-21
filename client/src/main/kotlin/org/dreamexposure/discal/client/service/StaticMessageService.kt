package org.dreamexposure.discal.client.service

import discord4j.core.GatewayDiscordClient
import discord4j.core.`object`.entity.Guild
import discord4j.core.spec.MessageEditSpec
import discord4j.rest.http.client.ClientException
import org.dreamexposure.discal.Application
import org.dreamexposure.discal.Application.Companion.getShardIndex
import org.dreamexposure.discal.client.message.embed.CalendarEmbed
import org.dreamexposure.discal.core.business.MetricService
import org.dreamexposure.discal.core.database.DatabaseManager
import org.dreamexposure.discal.core.entities.Calendar
import org.dreamexposure.discal.core.extensions.discord4j.getCalendar
import org.dreamexposure.discal.core.extensions.discord4j.getSettings
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.`object`.GuildSettings
import org.dreamexposure.discal.core.`object`.StaticMessage
import org.dreamexposure.discal.core.utils.GlobalVal.DEFAULT
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.getBean
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import org.springframework.util.StopWatch
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.function.TupleUtils
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit

@Component
class StaticMessageService(
    private val metricService: MetricService,
    private val beanFactory: BeanFactory
) : ApplicationRunner {
    private val discordClient: GatewayDiscordClient
        get() = beanFactory.getBean()

    override fun run(args: ApplicationArguments?) {
        Flux.interval(Duration.ofHours(1))
            .onBackpressureDrop()
            .flatMap { doMessageUpdateLogic() }
            .doOnError { LOGGER.error(DEFAULT, "!-Static Message Service Error-!", it) }
            .subscribe()
    }


    private fun doMessageUpdateLogic(): Mono<Void> {
        val taskTimer = StopWatch()
        taskTimer.start()

        return DatabaseManager.getStaticMessagesForShard(Application.getShardCount(), getShardIndex())
            .flatMapMany { Flux.fromIterable(it) }
            //We have no interest in updating the message so close to its last update
            .filter { Duration.between(Instant.now(), it.lastUpdate).abs().toMinutes() >= 30 }
            // Only update messages in range
            .filter { Duration.between(Instant.now(), it.scheduledUpdate).toMinutes() <= 60 }
            .flatMap { data ->
                discordClient.getMessageById(data.channelId, data.messageId).flatMap { message ->
                    when (data.type) {
                        StaticMessage.Type.CALENDAR_OVERVIEW -> {
                            val guildMono = message.guild.cache()
                            val setMono = guildMono.flatMap(Guild::getSettings)
                            val calMono = guildMono.flatMap { it.getCalendar(data.calendarNumber) }

                            Mono.zip(guildMono, setMono, calMono).flatMap(
                                TupleUtils.function { guild, settings, calendar ->
                                    CalendarEmbed.overview(guild, settings, calendar, true).flatMap {
                                        message.edit(
                                            MessageEditSpec.builder()
                                                .embedsOrNull(listOf(it))
                                                .build()
                                        ).doOnNext {
                                            metricService.incrementStaticMessagesUpdated(data.type)
                                        }.then(
                                            DatabaseManager.updateStaticMessage(
                                                data.copy(
                                                    lastUpdate = Instant.now(),
                                                    scheduledUpdate = data.scheduledUpdate.plus(1, ChronoUnit.DAYS)
                                                )
                                            )
                                        )
                                    }
                                })
                        }
                    }
                }.onErrorResume(ClientException.isStatusCode(403, 404)) {
                    //Message or channel was deleted OR access was revoked, delete from database
                    DatabaseManager.deleteStaticMessage(data.guildId, data.messageId)
                }
            }.doOnError {
                LOGGER.error(DEFAULT, "Static message update error", it)
            }.onErrorResume {
                Mono.empty()
            }.doFinally {
                taskTimer.stop()
                metricService.recordStaticMessageTaskDuration("overall", taskTimer.totalTimeMillis)
            }.then()
    }

    fun updateStaticMessage(calendar: Calendar, settings: GuildSettings): Mono<Void> {
        return discordClient.getGuildById(settings.guildID)
            .flatMap { updateStaticMessages(it, calendar, settings) }
    }

    fun updateStaticMessages(guild: Guild, calendar: Calendar, settings: GuildSettings): Mono<Void> {
        return DatabaseManager.getStaticMessagesForCalendar(guild.id, calendar.calendarNumber)
            .flatMapMany { Flux.fromIterable(it) }
            .flatMap { msg ->
                when (msg.type) {
                    StaticMessage.Type.CALENDAR_OVERVIEW -> {
                        CalendarEmbed.overview(guild, settings, calendar, true).flatMap {
                            guild.client.getMessageById(msg.channelId, msg.messageId).flatMap { message ->
                                message.edit(
                                    MessageEditSpec.builder()
                                        .embedsOrNull(listOf(it))
                                        .build()
                                ).doOnNext {
                                    metricService.incrementStaticMessagesUpdated(msg.type)
                                }.then(DatabaseManager.updateStaticMessage(msg.copy(lastUpdate = Instant.now())))
                            }.onErrorResume(ClientException.isStatusCode(403, 404)) {
                                //Message or channel was deleted OR access was revoked, delete from database
                                DatabaseManager.deleteStaticMessage(msg.guildId, msg.messageId)
                            }
                        }
                    }
                }
            }.doOnError {
                LOGGER.error(DEFAULT, "Static message update error", it)
            }.onErrorResume {
                Mono.empty()
            }.then()
    }
}
