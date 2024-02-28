package org.dreamexposure.discal.core.config

import java.io.FileReader
import java.util.*

enum class Config(private val key: String, private var value: Any? = null) {
    // Basic spring settings
    APP_NAME("spring.application.name"),

    // Database settings, to be removed once DatabaseManager is retired
    SQL_URL("spring.r2dbc.url"),
    SQL_USERNAME("spring.r2dbc.username"),
    SQL_PASSWORD("spring.r2dbc.password"),

    // Redis cache settings
    REDIS_HOST("spring.data.redis.host"),
    REDIS_PORT("spring.data.redis.port"),
    REDIS_PASSWORD("spring.data.redis.password", ""),
    CACHE_REDIS_IS_CLUSTER("redis.cluster", false),
    CACHE_USE_REDIS("bot.cache.redis", false),
    CACHE_USE_REDIS_D4J("bot.cache.redis.d4j", false),

    CACHE_PREFIX("bot.cache.prefix", "discal"),
    CACHE_TTL_SETTINGS_MINUTES("bot.cache.ttl-minutes.settings", 60),
    CACHE_TTL_CREDENTIALS_MINUTES("bot.cache.ttl-minutes.credentials", 120),
    CACHE_TTL_ACCOUNTS_MINUTES("bot.cache.ttl-minutes.accounts", 60),
    CACHE_TTL_OAUTH_STATE_MINUTES("bot.cache.ttl-minutes.oauth.state", 5),
    CACHE_TTL_CALENDAR_MINUTES("bot.cache.ttl-minutes.calendar", 120),
    CACHE_TTL_RSVP_MINUTES("bot.cache.ttl-minutes.rsvp", 60),
    CACHE_TTL_STATIC_MESSAGE_MINUTES("bot.cache.ttl-minutes.static-messages", 60),

    // Security configuration

    // Bot secrets
    SECRET_DISCAL_API_KEY("bot.secret.api-token"),
    SECRET_BOT_TOKEN("bot.secret.token"),
    SECRET_CLIENT_SECRET("bot.secret.client-secret"),

    SECRET_GOOGLE_CLIENT_ID("bot.secret.google.client.id"),
    SECRET_GOOGLE_CLIENT_SECRET("bot.secret.google.client.secret"),
    SECRET_GOOGLE_CREDENTIAL_KEY("bot.secret.google.credential.key"),
    SECRET_GOOGLE_CREDENTIAL_COUNT("bot.secret.google.credential.count"),

    SECRET_WEBHOOK_DEFAULT("bot.secret.default-webhook"),
    SECRET_WEBHOOK_STATUS("bot.secret.status-webhook"),

    SECRET_INTEGRATION_D_BOTS_GG_TOKEN("bot.secret.token.d-bots-gg"),
    SECRET_INTEGRATION_TOP_GG_TOKEN("bot.secret.token.top-gg"),

    // Various URLs
    URL_BASE("bot.url.base"),
    URL_API("bot.url.api"),
    URL_CAM("bot.url.cam"),
    URL_SUPPORT("bot.url.support", "https://discord.gg/2TFqyuy"),
    URL_INVITE("bot.url.invite"),
    URL_DISCORD_REDIRECT("bot.url.discord.redirect"),

    // UI and UX
    EMBED_RSVP_WAITLIST_DISPLAY_LENGTH("bot.ui.embed.rsvp.waitlist.length", 3),

    // Everything else
    SHARD_COUNT("bot.sharding.count"),
    SHARD_INDEX("bot.sharding.index", 0),
    RESTART_SERVICE_ENABLED("bot.services.restart", false),
    HEARTBEAT_INTERVAL("bot.timing.heartbeat.seconds", 120),

    DISCORD_APP_ID("bot.discord-app-id"),

    LOGGING_WEBHOOKS_USE("bot.logging.webhooks.use", false),
    LOGGING_WEBHOOKS_ALL_ERRORS("bot.logging.webhooks.all-error", false),

    INTEGRATIONS_UPDATE_BOT_LIST_SITES("bot.integrations.update-bot-sites", false),


    ;

    companion object {
        fun init() {
            val props = Properties()
            props.load(FileReader("application.properties"))

            entries.forEach { it.value = props.getProperty(it.key, it.value?.toString()) }
        }
    }

    fun getString() = value.toString()

    fun getInt() = getString().toInt()

    fun getLong() = getString().toLong()

    fun getBoolean() = getString().toBoolean()
}
