package org.dreamexposure.discal.core.`object`

import java.util.*

enum class BotSettings {
    SQL_HOST,
    SQL_PORT,
    SQL_USER,
    SQL_PASS,
    SQL_DB,
    SQL_PREFIX,

    REDIS_HOSTNAME,
    REDIS_PORT,
    REDIS_PASSWORD,
    REDIS_USE_PASSWORD,

    TOKEN,
    SECRET,
    ID,

    GOOGLE_CLIENT_ID,
    GOOGLE_CLIENT_SECRET,
    CREDENTIALS_COUNT,
    CREDENTIALS_KEY,

    SHARD_COUNT,
    SHARD_INDEX,

    D_BOTS_GG_TOKEN,
    TOP_GG_TOKEN,

    TIME_OUT,

    REDIR_URI,
    REDIR_URL,

    INVITE_URL,
    SUPPORT_INVITE,

    BASE_URL,
    API_URL,
    CAM_URL,

    DEFAULT_WEBHOOK,
    STATUS_WEBHOOK,

    USE_REDIS_STORES,
    USE_WEBHOOKS,
    UPDATE_SITES,
    USE_RESTART_SERVICE,

    BOT_API_TOKEN,

    PROFILE;

    private var value: String? = null

    companion object {
        fun init(properties: Properties) {
            values().forEach {
                try {
                    it.value = properties.getProperty(it.name)
                } catch (npe: NullPointerException) {
                    throw IllegalStateException("Settings not valid! Check console for more information", npe)
                }
            }
        }
    }

    fun get() = this.value!!
}
