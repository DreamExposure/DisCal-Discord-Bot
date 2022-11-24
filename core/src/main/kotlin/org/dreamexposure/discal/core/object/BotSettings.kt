package org.dreamexposure.discal.core.`object`

import java.util.*

enum class BotSettings {
    SQL_HOST,
    SQL_PORT,
    SQL_USER,
    SQL_PASS,
    SQL_DB,
    SQL_PREFIX,

    GOOGLE_CLIENT_ID,
    GOOGLE_CLIENT_SECRET,
    CREDENTIALS_COUNT,
    CREDENTIALS_KEY,

    SHARD_COUNT,
    SHARD_INDEX,

    TIME_OUT,

    INVITE_URL,
    SUPPORT_INVITE,

    BASE_URL,
    CAM_URL,

    USE_RESTART_SERVICE,

    BOT_API_TOKEN;

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
