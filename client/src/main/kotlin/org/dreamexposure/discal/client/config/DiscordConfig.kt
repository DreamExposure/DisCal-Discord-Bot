package org.dreamexposure.discal.client.config

import discord4j.common.store.Store
import discord4j.common.store.legacy.LegacyStoreLayout
import discord4j.core.DiscordClient
import discord4j.core.DiscordClientBuilder
import discord4j.core.GatewayDiscordClient
import discord4j.core.event.domain.Event
import discord4j.core.`object`.presence.ClientActivity
import discord4j.core.`object`.presence.ClientPresence
import discord4j.core.shard.MemberRequestFilter
import discord4j.core.shard.ShardingStrategy
import discord4j.discordjson.json.GuildData
import discord4j.discordjson.json.MessageData
import discord4j.gateway.GatewayReactorResources
import discord4j.gateway.intent.Intent
import discord4j.gateway.intent.IntentSet
import discord4j.store.api.mapping.MappingStoreService
import discord4j.store.api.service.StoreService
import discord4j.store.jdk.JdkStoreService
import discord4j.store.redis.RedisClusterStoreService
import discord4j.store.redis.RedisStoreDefaults
import discord4j.store.redis.RedisStoreService
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.cluster.RedisClusterClient
import kotlinx.coroutines.reactor.mono
import org.dreamexposure.discal.Application
import org.dreamexposure.discal.client.listeners.discord.EventListener
import org.dreamexposure.discal.core.config.Config
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.kotlin.core.publisher.toFlux

@Configuration
class DiscordConfig {

    @Bean
    fun discordGatewayClient(
        listeners: List<EventListener<*>>,
        stores: StoreService
    ): GatewayDiscordClient {
        return DiscordClientBuilder.create(Config.SECRET_BOT_TOKEN.getString())
            .build().gateway()
            .setGatewayReactorResources { resources ->
                GatewayReactorResources
                    .builder(resources)
                    .httpClient(resources.httpClient.metrics(true) { s -> s })
                    .build()
            }
            .setEnabledIntents(getIntents())
            .setSharding(getStrategy())
            .setStore(Store.fromLayout(LegacyStoreLayout.of(stores)))
            .setInitialPresence { ClientPresence.doNotDisturb(ClientActivity.playing("Booting Up!")) }
            .setMemberRequestFilter(MemberRequestFilter.none())
            .withEventDispatcher { dispatcher ->
                @Suppress("UNCHECKED_CAST")
                (listeners as Iterable<EventListener<Event>>).toFlux()
                    .flatMap {
                        dispatcher.on(it.genericType) { event -> mono { it.handle(event) } }
                    }
            }
            .login()
            .block()!!
    }

    @Bean
    fun discordClient(gatewayDiscordClient: GatewayDiscordClient): DiscordClient {
        return gatewayDiscordClient.rest()
    }

    @Bean
    fun discordStores(): StoreService {
        val useRedis = Config.CACHE_USE_REDIS_D4J.getBoolean()
        val redisHost = Config.REDIS_HOST.getString()
        val redisPassword = Config.REDIS_PASSWORD.getString().toCharArray()
        val redisPort = Config.REDIS_PORT.getInt()
        val redisCluster = Config.CACHE_REDIS_IS_CLUSTER.getBoolean()
        val prefix = Config.CACHE_PREFIX.getString()

        return if (useRedis) {
            val uriBuilder = RedisURI.Builder
                .redis(redisHost, redisPort)
            if (redisPassword.isNotEmpty()) uriBuilder.withPassword(redisPassword)

            val rss = if (redisCluster) {
                RedisClusterStoreService.Builder()
                    .redisClient(RedisClusterClient.create(uriBuilder.build()))
                    .keyPrefix("$prefix.${RedisStoreDefaults.DEFAULT_KEY_PREFIX}")
                    .build()
            } else {
                RedisStoreService.Builder()
                    .redisClient(RedisClient.create(uriBuilder.build()))
                    .keyPrefix("$prefix.${RedisStoreDefaults.DEFAULT_KEY_PREFIX}")
                    .build()
            }


            MappingStoreService.create()
                .setMappings(rss, GuildData::class.java, MessageData::class.java)
                .setFallback(JdkStoreService())
        } else JdkStoreService()
    }

    private fun getStrategy(): ShardingStrategy {
        return ShardingStrategy.builder()
            .count(Application.getShardCount())
            .indices(Application.getShardIndex())
            .build()
    }

    private fun getIntents(): IntentSet {
        return IntentSet.of(
            Intent.GUILDS,
            Intent.GUILD_MESSAGES,
            Intent.GUILD_MESSAGE_REACTIONS,
            Intent.DIRECT_MESSAGES,
            Intent.DIRECT_MESSAGE_REACTIONS
        )
    }
}
