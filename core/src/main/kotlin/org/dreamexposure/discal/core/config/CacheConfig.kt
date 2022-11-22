package org.dreamexposure.discal.core.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import java.time.Duration

@Configuration
class CacheConfig(
    @Value("\${bot.cache.prefix:discal}")
    private val prefix: String,
    @Value("\${bot.cache.ttl-minutes.settings:60}")
    private val settingsTtl: Long,
) {
    // Cache name constants
    private val settingsCacheName = "$prefix.settingsCache"


    // Redis caching
    @Bean
    @ConditionalOnProperty("bot.cache.redis", havingValue = "true")
    fun redisCache(connection: RedisConnectionFactory): RedisCacheManager {
        return RedisCacheManager.builder(connection)
            .withCacheConfiguration(settingsCacheName,
                RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(settingsTtl))
            )
            .build()
    }

    // In-memory fallback caching
}
