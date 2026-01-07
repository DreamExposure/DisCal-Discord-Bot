package org.dreamexposure.discal.client.config

import discord4j.common.ReactorResources
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
import discord4j.gateway.intent.Intent
import discord4j.gateway.intent.IntentSet
import discord4j.store.api.mapping.MappingStoreService
import discord4j.store.api.service.StoreService
import discord4j.store.jdk.JdkStoreService
import discord4j.store.redis.RedisClusterStoreService
import discord4j.store.redis.RedisStoreService
import io.lettuce.core.ClientOptions
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.SslOptions
import io.lettuce.core.cluster.ClusterClientOptions
import io.lettuce.core.cluster.RedisClusterClient
import kotlinx.coroutines.reactor.mono
import org.dreamexposure.discal.Application
import org.dreamexposure.discal.client.listeners.discord.EventListener
import org.dreamexposure.discal.core.config.Config
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.kotlin.core.publisher.toFlux
import java.io.File

@Configuration
class DiscordConfig {

    @Bean
    fun discordGatewayClient(
        listeners: List<EventListener<*>>,
        stores: StoreService
    ): GatewayDiscordClient {
        return DiscordClientBuilder.create(Config.SECRET_BOT_TOKEN.getString())
            .setReactorResources(ReactorResources.builder()
                .httpClient(ReactorResources.DEFAULT_HTTP_CLIENT.get().metrics(Config.INTEGRATIONS_REACTOR_METRICS.getBoolean()) { s -> s })
                .build()
            ).build().gateway()
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
        val useRedis = Config.CACHE_USE_REDIS.getBoolean()
        val redisHost = Config.REDIS_HOST.getString()
        val redisPort = Config.REDIS_PORT.getInt()
        val useSSL = Config.REDIS_SSL.getBoolean()
        val trustStoreFile = Config.REDIS_SSL_TRUSTSTORE_FILE.getString()
        val trustStorePassword = Config.REDIS_SSL_TRUSTSTORE_PASSWORD.getString()
        val redisDatabase = Config.REDIS_DATABASE.getInt()
        val redisUser = Config.REDIS_USERNAME.getString()
        val redisPassword = Config.REDIS_PASSWORD.getString().toCharArray()
        val isRedisCluster = Config.CACHE_REDIS_IS_CLUSTER.getBoolean()

        return if (useRedis) {
            val uriBuilder = RedisURI.Builder
                .redis(redisHost, redisPort)
                .withSsl(useSSL)
            if (redisDatabase > -1) uriBuilder.withDatabase(redisDatabase)
            if (redisUser.isNotEmpty() && redisPassword.isNotEmpty()) uriBuilder.withAuthentication(redisUser, redisPassword)
            else if (redisPassword.isNotEmpty()) uriBuilder.withPassword(redisPassword)

            val sslOptions = SslOptions.builder()
                .jdkSslProvider()
                .truststore(File(trustStoreFile), trustStorePassword)
                .build()
            val rss = if (isRedisCluster) {
                val client = RedisClusterClient.create(uriBuilder.build())
                client.setOptions(
                    ClusterClientOptions.builder()
                    .sslOptions(sslOptions)
                    .build()
                )

                RedisClusterStoreService.Builder()
                    .redisClient(client)
                    .build()
            } else {
                val client = RedisClient.create(uriBuilder.build())
                client.options = ClientOptions.builder()
                    .sslOptions(sslOptions)
                    .build()

                RedisStoreService.Builder()
                    .redisClient(client)
                    .build()
            }

            MappingStoreService.create()
                .setFallback(rss)
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
