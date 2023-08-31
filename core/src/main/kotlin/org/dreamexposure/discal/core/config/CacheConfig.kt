package org.dreamexposure.discal.core.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.dreamexposure.discal.CalendarCache
import org.dreamexposure.discal.CredentialsCache
import org.dreamexposure.discal.OauthStateCache
import org.dreamexposure.discal.core.cache.JdkCacheRepository
import org.dreamexposure.discal.core.cache.RedisCacheRepository
import org.dreamexposure.discal.core.extensions.asMinutes
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory

@Configuration
class CacheConfig {
    // Cache name constants
    private val prefix = Config.CACHE_PREFIX.getString()
    private val settingsCacheName = "$prefix.settingsCache"
    private val credentialsCacheName = "$prefix.credentialsCache"
    private val oauthStateCacheName = "$prefix.oauthStateCache"
    private val calendarCacheName = "$prefix.calendarCache"

    private val settingsTtl = Config.CACHE_TTL_SETTINGS_MINUTES.getLong().asMinutes()
    private val credentialsTll = Config.CACHE_TTL_CREDENTIALS_MINUTES.getLong().asMinutes()
    private val oauthStateTtl = Config.CACHE_TTL_OAUTH_STATE_MINUTES.getLong().asMinutes()
    private val calendarTtl = Config.CACHE_TTL_CALENDAR_MINUTES.getLong().asMinutes()


    // Redis caching
    @Bean
    @ConditionalOnProperty("bot.cache.redis", havingValue = "true")
    fun redisCache(connection: RedisConnectionFactory): RedisCacheManager {
        return RedisCacheManager.builder(connection)
            .withCacheConfiguration(settingsCacheName,
                RedisCacheConfiguration.defaultCacheConfig().entryTtl(settingsTtl)
            ).withCacheConfiguration(credentialsCacheName,
                RedisCacheConfiguration.defaultCacheConfig().entryTtl(credentialsTll)
            ).withCacheConfiguration(oauthStateCacheName,
                RedisCacheConfiguration.defaultCacheConfig().entryTtl(oauthStateTtl)
            ).withCacheConfiguration(calendarCacheName,
                RedisCacheConfiguration.defaultCacheConfig().entryTtl(calendarTtl)
            ).build()
    }

    @Bean
    @Primary
    @ConditionalOnProperty("bot.cache.redis", havingValue = "true")
    fun credentialsRedisCache(cacheManager: RedisCacheManager, objectMapper: ObjectMapper): CredentialsCache =
        RedisCacheRepository(cacheManager, objectMapper, credentialsCacheName)

    @Bean
    @Primary
    @ConditionalOnProperty("bot.cache.redis", havingValue = "true")
    fun oauthStateRedisCache(cacheManager: RedisCacheManager, objectMapper: ObjectMapper): OauthStateCache =
        RedisCacheRepository(cacheManager, objectMapper, oauthStateCacheName)

    @Bean
    @Primary
    @ConditionalOnProperty("bot.cache.redis", havingValue = "true")
    fun calendarRedisCache(cacheManager: RedisCacheManager, objectMapper: ObjectMapper): CalendarCache =
        RedisCacheRepository(cacheManager, objectMapper, calendarCacheName)


    // In-memory fallback caching
    @Bean
    fun credentialsFallbackCache(): CredentialsCache = JdkCacheRepository(settingsTtl)

    @Bean
    fun oauthStateFallbackCache(): OauthStateCache = JdkCacheRepository(settingsTtl)

    @Bean
    fun calendarFallbackCache(): CalendarCache = JdkCacheRepository(calendarTtl)
}
