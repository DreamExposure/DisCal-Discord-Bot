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
    private val settingsTtl = Config.CACHE_TTL_SETTINGS_MINUTES.getLong().asMinutes()
    private val credentialsTll = Config.CACHE_TTL_CREDENTIALS_MINUTES.getLong().asMinutes()
    private val oauthStateTtl = Config.CACHE_TTL_OAUTH_STATE_MINUTES.getLong().asMinutes()
    private val calendarTtl = Config.CACHE_TTL_CALENDAR_MINUTES.getLong().asMinutes()
    private val rsvpTtl = Config.CACHE_TTL_RSVP_MINUTES.getLong().asMinutes()
    private val staticMessageTtl = Config.CACHE_TTL_STATIC_MESSAGE_MINUTES.getLong().asMinutes()
    private val announcementTll = Config.CACHE_TTL_ANNOUNCEMENT_MINUTES.getLong().asMinutes()
    private val wizardTtl = Config.TIMING_WIZARD_TIMEOUT_MINUTES.getLong().asMinutes()
    private val calendarTokenTtl = Config.CACHE_TTL_CALENDAR_TOKEN_MINUTES.getLong().asMinutes()
    private val eventTtl = Config.CACHE_TTL_EVENTS_MINUTES.getLong().asMinutes()
    private val networkStatusTtl = Config.CACHE_TTL_NETWORK_STATUS_MINUTES.getLong().asMinutes()


    // Redis caching
    @Bean
    @Primary
    @ConditionalOnProperty("bot.cache.redis", havingValue = "true")
    fun guildSettingsRedisCache(objectMapper: ObjectMapper, redisTemplate: ReactiveStringRedisTemplate): GuildSettingsCache =
        RedisStringCacheRepository(objectMapper, redisTemplate, "GuildSettings", settingsTtl)

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
    fun calendarMetadataRedisCache(objectMapper: ObjectMapper, redisTemplate: ReactiveStringRedisTemplate): CalendarMetadataCache =
        RedisStringCacheRepository(objectMapper, redisTemplate, "CalendarMetadata", calendarTtl)

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

    @Bean
    @Primary
    @ConditionalOnProperty("bot.cache.redis", havingValue = "true")
    fun announcementRedisCache(objectMapper: ObjectMapper, redisTemplate: ReactiveStringRedisTemplate): AnnouncementCache =
        RedisStringCacheRepository(objectMapper, redisTemplate, "Announcements", announcementTll)

    @Bean
    @Primary
    @ConditionalOnProperty("bot.cache.redis", havingValue = "true")
    fun announcementWizardRedisCache(objectMapper: ObjectMapper, redisTemplate: ReactiveStringRedisTemplate): AnnouncementWizardStateCache =
        RedisStringCacheRepository(objectMapper, redisTemplate, "AnnouncementWizards", wizardTtl)

    @Bean
    @Primary
    @ConditionalOnProperty("bot.cache.redis", havingValue = "true")
    fun eventWizardRedisCache(objectMapper: ObjectMapper, redisTemplate: ReactiveStringRedisTemplate): EventWizardStateCache =
        RedisStringCacheRepository(objectMapper, redisTemplate, "EventWizards", wizardTtl)

    @Bean
    @Primary
    @ConditionalOnProperty("bot.cache.redis", havingValue = "true")
    fun calendarWizardRedisCache(objectMapper: ObjectMapper, redisTemplate: ReactiveStringRedisTemplate): CalendarWizardStateCache =
        RedisStringCacheRepository(objectMapper, redisTemplate, "CalendarWizards", wizardTtl)

    @Bean
    @Primary
    @ConditionalOnProperty("bot.cache.redis", havingValue = "true")
    fun eventRedisCache(objectMapper: ObjectMapper, redisTemplate: ReactiveStringRedisTemplate): EventCache =
        RedisStringCacheRepository(objectMapper, redisTemplate, "Events", eventTtl)

    @Bean
    @Primary
    @ConditionalOnProperty("bot.cache.redis", havingValue = "true")
    fun networkStatusRedisCache(objectMapper: ObjectMapper, redisTemplate: ReactiveStringRedisTemplate): NetworkStatusCache =
        RedisStringCacheRepository(objectMapper, redisTemplate, "NetworkStatus", networkStatusTtl)


    // In-memory fallback caching
    @Bean
    fun guildSettingsFallbackCache(): GuildSettingsCache = JdkCacheRepository(settingsTtl)

    @Bean
    fun credentialsFallbackCache(): CredentialsCache = JdkCacheRepository(credentialsTll)

    @Bean
    fun oauthStateFallbackCache(): OauthStateCache = JdkCacheRepository(oauthStateTtl)

    @Bean
    fun calendarMetadataFallbackCache(): CalendarMetadataCache = JdkCacheRepository(calendarTtl)

    @Bean
    fun calendarFallbackCache(): CalendarCache = JdkCacheRepository(calendarTtl)

    @Bean
    fun rsvpFallbackCache(): RsvpCache = JdkCacheRepository(rsvpTtl)

    @Bean
    fun staticMessageFallbackCache(): StaticMessageCache = JdkCacheRepository(staticMessageTtl)

    @Bean
    fun announcementFallbackCache(): AnnouncementCache = JdkCacheRepository(announcementTll)

    @Bean
    fun announcementWizardFallbackCache(): AnnouncementWizardStateCache = JdkCacheRepository(wizardTtl)

    @Bean
    fun eventWizardFallbackCache(): EventWizardStateCache = JdkCacheRepository(wizardTtl)

    @Bean
    fun calendarWizardFallbackCache(): CalendarWizardStateCache = JdkCacheRepository(wizardTtl)

    @Bean
    fun calendarTokenFallbackCache(): CalendarTokenCache = JdkCacheRepository(calendarTokenTtl)

    @Bean
    fun eventFallbackCache(): EventCache = JdkCacheRepository(eventTtl)

    @Bean
    fun networkStatusFallbackCache(): NetworkStatusCache = JdkCacheRepository(networkStatusTtl)
}
