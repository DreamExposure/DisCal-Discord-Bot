package org.dreamexposure.discal.core.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.dreamexposure.discal.*
import org.dreamexposure.discal.core.cache.JdkCacheRepository
import org.dreamexposure.discal.core.cache.RedisStringCacheRepository
import org.dreamexposure.discal.core.extensions.asMinutes
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.core.ReactiveStringRedisTemplate

@Configuration
class CacheConfig {
    private val credentialsTll = Config.CACHE_TTL_CREDENTIALS_MINUTES.getLong().asMinutes()
    private val oauthStateTtl = Config.CACHE_TTL_OAUTH_STATE_MINUTES.getLong().asMinutes()
    private val calendarTtl = Config.CACHE_TTL_CALENDAR_MINUTES.getLong().asMinutes()
    private val rsvpTtl = Config.CACHE_TTL_RSVP_MINUTES.getLong().asMinutes()
    private val staticMessageTtl = Config.CACHE_TTL_STATIC_MESSAGE_MINUTES.getLong().asMinutes()


    // Redis caching
    @Bean
    @Primary
    @ConditionalOnProperty("bot.cache.redis", havingValue = "true")
    fun credentialsRedisCache(objectMapper: ObjectMapper, redisTemplate: ReactiveStringRedisTemplate): CredentialsCache =
        RedisStringCacheRepository(objectMapper, redisTemplate, "Credentials", credentialsTll)

    @Bean
    @Primary
    @ConditionalOnProperty("bot.cache.redis", havingValue = "true")
    fun oauthStateRedisCache(objectMapper: ObjectMapper, redisTemplate: ReactiveStringRedisTemplate): OauthStateCache =
    RedisStringCacheRepository(objectMapper, redisTemplate, "OauthStates", oauthStateTtl)

    @Bean
    @Primary
    @ConditionalOnProperty("bot.cache.redis", havingValue = "true")
    fun calendarRedisCache(objectMapper: ObjectMapper, redisTemplate: ReactiveStringRedisTemplate): CalendarCache =
        RedisStringCacheRepository(objectMapper, redisTemplate, "Calendars", calendarTtl)

    @Bean
    @Primary
    @ConditionalOnProperty("bot.cache.redis", havingValue = "true")
    fun rsvpRedisCache(objectMapper: ObjectMapper, redisTemplate: ReactiveStringRedisTemplate): RsvpCache =
        RedisStringCacheRepository(objectMapper, redisTemplate, "Rsvps", rsvpTtl)

    @Bean
    @Primary
    @ConditionalOnProperty("bot.cache.redis", havingValue = "true")
    fun staticMessageRedisCache(objectMapper: ObjectMapper, redisTemplate: ReactiveStringRedisTemplate): StaticMessageCache =
        RedisStringCacheRepository(objectMapper, redisTemplate, "StaticMessages", staticMessageTtl)


    // In-memory fallback caching
    @Bean
    fun credentialsFallbackCache(): CredentialsCache = JdkCacheRepository(credentialsTll)

    @Bean
    fun oauthStateFallbackCache(): OauthStateCache = JdkCacheRepository(oauthStateTtl)

    @Bean
    fun calendarFallbackCache(): CalendarCache = JdkCacheRepository(calendarTtl)

    @Bean
    fun rsvpFallbackCache(): RsvpCache = JdkCacheRepository(rsvpTtl)

    @Bean
    fun staticMessageFallbackCache(): StaticMessageCache = JdkCacheRepository(staticMessageTtl)
}
