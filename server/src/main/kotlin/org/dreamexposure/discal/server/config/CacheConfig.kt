package org.dreamexposure.discal.server.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.dreamexposure.discal.NetworkStatusCache
import org.dreamexposure.discal.core.cache.JdkCacheRepository
import org.dreamexposure.discal.core.cache.RedisStringCacheRepository
import org.dreamexposure.discal.core.config.Config
import org.dreamexposure.discal.core.extensions.asMinutes
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.core.ReactiveStringRedisTemplate

@Configuration
class CacheConfig {
    private val networkStatusTtl = Config.CACHE_TTL_NETWORK_STATUS_MINUTES.getLong().asMinutes()

    // Redis Caching
    @Bean
    @Primary
    @ConditionalOnProperty("bot.cache.redis", havingValue = "true")
    fun networkStatusRedisCache(objectMapper: ObjectMapper, redisTemplate: ReactiveStringRedisTemplate): NetworkStatusCache =
        RedisStringCacheRepository(objectMapper, redisTemplate, "NetworkStatus", networkStatusTtl)

    // In-memory fallback caching
    @Bean
    fun networkStatusFallbackCache(): NetworkStatusCache = JdkCacheRepository(networkStatusTtl)
}
