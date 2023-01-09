package org.dreamexposure.discal.core.config

import org.dreamexposure.discal.core.extensions.asMinutes
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory

@Configuration
class CacheConfig {
    // Cache name constants
    private val prefix = Config.CACHE_PREFIX.getString()
    private val settingsCacheName = "$prefix.settingsCache"

    private val settingsTtl = Config.CACHE_TTL_SETTINGS_MINUTES.getLong().asMinutes()


    // Redis caching
    @Bean
    @ConditionalOnProperty("bot.cache.redis", havingValue = "true")
    fun redisCache(connection: RedisConnectionFactory): RedisCacheManager {
        return RedisCacheManager.builder(connection)
            .withCacheConfiguration(settingsCacheName,
                RedisCacheConfiguration.defaultCacheConfig().entryTtl(settingsTtl)
            )
            .build()
    }

    // In-memory fallback caching
}
