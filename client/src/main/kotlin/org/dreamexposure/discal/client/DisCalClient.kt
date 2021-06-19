package org.dreamexposure.discal.client

import discord4j.core.DiscordClientBuilder
import discord4j.core.GatewayDiscordClient
import discord4j.core.`object`.presence.Activity
import discord4j.core.`object`.presence.Presence
import discord4j.core.event.domain.channel.TextChannelDeleteEvent
import discord4j.core.event.domain.lifecycle.ReadyEvent
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.event.domain.role.RoleDeleteEvent
import discord4j.core.shard.ShardingStrategy
import discord4j.discordjson.json.GuildData
import discord4j.discordjson.json.MessageData
import discord4j.gateway.intent.Intent
import discord4j.gateway.intent.IntentSet
import discord4j.store.api.mapping.MappingStoreService
import discord4j.store.api.service.StoreService
import discord4j.store.jdk.JdkStoreService
import discord4j.store.redis.RedisStoreService
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import org.dreamexposure.discal.Application
import org.dreamexposure.discal.client.listeners.discord.ChannelDeleteListener
import org.dreamexposure.discal.client.listeners.discord.MessageCreateListener
import org.dreamexposure.discal.client.listeners.discord.ReadyEventListener
import org.dreamexposure.discal.client.listeners.discord.RoleDeleteListener
import org.dreamexposure.discal.client.message.Messages
import org.dreamexposure.discal.client.module.announcement.AnnouncementThread
import org.dreamexposure.discal.client.module.command.*
import org.dreamexposure.discal.client.service.KeepAliveHandler
import org.dreamexposure.discal.client.service.TimeManager
import org.dreamexposure.discal.core.`object`.BotSettings
import org.dreamexposure.discal.core.database.DatabaseManager
import org.dreamexposure.discal.core.logger.LogFeed
import org.dreamexposure.discal.core.logger.`object`.LogObject
import org.dreamexposure.discal.core.network.google.Authorization
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.io.FileReader
import java.time.Duration
import java.util.*
import javax.annotation.PreDestroy
import kotlin.system.exitProcess

@Component
class DisCalClient {
    companion object {
        @JvmStatic
        @Deprecated("Try to use client that is provided by d4j entities until using DI")
        lateinit var client: GatewayDiscordClient
            private set

        @JvmStatic
        fun main(args: Array<String>) {
            //Get settings
            val p = Properties()
            p.load(FileReader("settings.properties"))
            BotSettings.init(p)

            ///Start google authorization daemon
            Authorization.getAuth().init()

            //Load lang files
            Messages.reloadLangs().subscribe()

            //Register commands
            CommandExecutor.registerCommand(HelpCommand())
            CommandExecutor.registerCommand(DisCalCommand())
            CommandExecutor.registerCommand(CalendarCommand())
            CommandExecutor.registerCommand(AddCalendarCommand())
            CommandExecutor.registerCommand(TimeCommand())
            CommandExecutor.registerCommand(LinkCalendarCommand())
            CommandExecutor.registerCommand(EventListCommand())
            CommandExecutor.registerCommand(EventCommand())
            CommandExecutor.registerCommand(RsvpCommand())
            CommandExecutor.registerCommand(AnnouncementCommand())
            CommandExecutor.registerCommand(DevCommand())

            //Start some of the daemon threads
            KeepAliveHandler.startKeepAlive(60) // 60 seconds

            TimeManager.getManager().init()

            //Start Spring
            try {
                SpringApplicationBuilder(Application::class.java)
                        .properties("spring.config.name:client")
                        .profiles(BotSettings.PROFILE.get())
                        .build()
                        .run(*args)
            } catch (e: Exception) {
                e.printStackTrace()
                LogFeed.log(LogObject.forException("Spring error", "by 'PANIC! at the Client'", e, DisCalClient::class.java))
                exitProcess(4)
            }

            //Login
            DiscordClientBuilder.create(BotSettings.TOKEN.get())
                    .build().gateway()
                    .setEnabledIntents(getIntents())
                    .setSharding(getStrategy())
                    .setStoreService(getStores())
                    .setInitialPresence { Presence.doNotDisturb(Activity.playing("Booting Up!")) }
                    .withGateway { client ->
                        DisCalClient.client = client

                        //Register listeners
                        val onReady = client.on(ReadyEvent::class.java)
                                .flatMap(ReadyEventListener::handle)
                                .then()

                        val onTextChannelDelete = client
                                .on(TextChannelDeleteEvent::class.java, ChannelDeleteListener::handle)
                                .then()

                        val onRoleDelete = client
                                .on(RoleDeleteEvent::class.java, RoleDeleteListener::handle)
                                .then()

                        val onCommand = client
                                .on(MessageCreateEvent::class.java, MessageCreateListener::handle)
                                .then()

                        val startAnnouncement = Flux.interval(Duration.ofMinutes(5))
                                .onBackpressureBuffer()
                                .flatMap {
                                    AnnouncementThread(client).run().doOnError {
                                        LogFeed.log(LogObject.forException("announcement err", it, this::class.java))
                                    }.onErrorResume { Mono.empty() }
                                }

                        Mono.`when`(onReady, onTextChannelDelete, onRoleDelete, onCommand, startAnnouncement)
                    }.block()
        }
    }


    @PreDestroy
    fun onShutdown() {
        LogFeed.log(LogObject.forStatus("Shutting down shard", "Shutting down shard"))

        TimeManager.getManager().shutdown()
        DatabaseManager.disconnectFromMySQL()

        client.logout().subscribe()
    }
}

private fun getStrategy(): ShardingStrategy {
    return ShardingStrategy.builder()
            .count(Application.getShardCount())
            .indices(Application.getShardIndex().toInt())
            .build()
}

private fun getStores(): StoreService {
    return if (BotSettings.USE_REDIS_STORES.get().equals("true", ignoreCase = true)) {
        val uri = RedisURI.Builder
                .redis(BotSettings.REDIS_HOSTNAME.get(), BotSettings.REDIS_PORT.get().toInt())
                .withPassword(BotSettings.REDIS_PASSWORD.get())
                .build()

        val rss = RedisStoreService.Builder()
                .redisClient(RedisClient.create(uri))
                .build()

        MappingStoreService.create()
                .setMappings(rss, GuildData::class.java, MessageData::class.java)
                .setFallback(JdkStoreService())
    } else JdkStoreService()
}

private fun getIntents(): IntentSet {
    return IntentSet.of(
            Intent.GUILDS,
            Intent.GUILD_MEMBERS,
            Intent.GUILD_MESSAGES,
            Intent.GUILD_MESSAGE_REACTIONS,
            Intent.DIRECT_MESSAGES,
            Intent.DIRECT_MESSAGE_REACTIONS
    )
}
