package org.dreamexposure.discal.client.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import discord4j.common.JacksonResources
import discord4j.common.store.Store
import discord4j.common.store.legacy.LegacyStoreLayout
import discord4j.core.DiscordClientBuilder
import discord4j.core.GatewayDiscordClient
import discord4j.core.event.domain.Event
import discord4j.core.`object`.presence.ClientActivity
import discord4j.core.`object`.presence.ClientPresence
import discord4j.core.shard.MemberRequestFilter
import discord4j.core.shard.ShardingStrategy
import discord4j.discordjson.json.GuildData
import discord4j.discordjson.json.MessageData
import discord4j.gateway.intent.Intent
import discord4j.gateway.intent.IntentSet
import discord4j.rest.RestClient
import discord4j.store.api.mapping.MappingStoreService
import discord4j.store.api.service.StoreService
import discord4j.store.jdk.JdkStoreService
import discord4j.store.redis.RedisClusterStoreService
import discord4j.store.redis.RedisStoreService
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.cluster.RedisClusterClient
import kotlinx.coroutines.reactor.mono
import org.dreamexposure.discal.Application
import org.dreamexposure.discal.client.listeners.discord.EventListener
import org.dreamexposure.discal.core.serializers.SnowflakeMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import reactor.kotlin.core.publisher.toFlux

@Configuration
class DiscordConfig {

    @Bean
    @Primary
    fun objectMapper(): ObjectMapper {
        // Use d4j's object mapper
        return JacksonResources.create().objectMapper
            .registerKotlinModule()
            .registerModule(JavaTimeModule())
            .registerModule(SnowflakeMapper())
    }

    @Bean
    fun discordGatewayClient(
        @Value("\${bot.secret.token}") token: String,
        listeners: List<EventListener<*>>,
        stores: StoreService
    ): GatewayDiscordClient {
        return DiscordClientBuilder.create(token)
            .build().gateway()
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
    fun discordRestClient(gatewayDiscordClient: GatewayDiscordClient): RestClient {
        return gatewayDiscordClient.restClient
    }

    @Bean
    fun discordStores(
        @Value("\${bot.cache.redis:false}") useRedis: Boolean,
        @Value("\${spring.redis.host:null}") redisHost: String?,
        @Value("\${spring.redis.port:null}") redisPort: String?,
        @Value("\${spring.redis.password:null}") redisPassword: CharSequence?,
        @Value("\${redis.cluster:false}") redisCluster: Boolean,
    ): StoreService {
        return if (useRedis) {
            val uriBuilder = RedisURI.Builder
                .redis(redisHost, redisPort!!.toInt())
            if (redisPassword != null) uriBuilder.withPassword(redisPassword)

            val rss = if (redisCluster) {
                RedisClusterStoreService.Builder()
                    .redisClient(RedisClusterClient.create(uriBuilder.build()))
                    .build()
            } else {
                RedisStoreService.Builder()
                    .redisClient(RedisClient.create(uriBuilder.build()))
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
