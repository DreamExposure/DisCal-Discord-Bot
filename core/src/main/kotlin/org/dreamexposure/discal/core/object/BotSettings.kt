package org.dreamexposure.discal.core.`object`

import java.util.*

enum class BotSettings {
    SQL_MASTER_HOST,
    SQL_MASTER_PORT,
    SQL_MASTER_USER,
    SQL_MASTER_PASS,

    SQL_SLAVE_HOST,
    SQL_SLAVE_PORT,
    SQL_SLAVE_USER,
    SQL_SLAVE_PASS,

    SQL_DB,
    SQL_PREFIX,

    REDIS_PASSWORD,
    REDIS_HOSTNAME,
    REDIS_PORT,
    PUBSUB_PREFIX,

    COM_USER,
    COM_PASS,
    COM_SUB_DOMAIN,

    TOKEN,
    SECRET,
    ID,

    GOOGLE_CLIENT_ID,
    GOOGLE_CLIENT_SECRET,
    CREDENTIALS_COUNT,

    SHARD_COUNT,
    SHARD_INDEX,

    LANG_FOLDER,
    LOG_FOLDER,
    CREDENTIAL_FOLDER,

    D_BOTS_GG_TOKEN,
    TOP_GG_TOKEN,

    TIME_OUT,
    PORT,

    REDIR_URI,
    REDIR_URL,

    INVITE_URL,
    SUPPORT_INVITE,

    API_URL,
    API_URL_INTERNAL,

    DEBUG_WEBHOOK,
    ERROR_WEBHOOK,
    STATUS_WEBHOOK,

    USE_REDIS_STORES,
    USE_WEBHOOKS,
    UPDATE_SITES,
    USE_RESTART_SERVICE,

    BOT_API_TOKEN,

    RESTART_IP,
    RESTART_PORT,
    RESTART_SSH_KEY,
    RESTART_USER,
    RESTART_CMD,

    PROFILE;

    private var value: String? = null

    companion object {
        fun init(properties: Properties) {
            values().forEach {
                try {
                    it.value = properties.getProperty(it.name)
                } catch (npe: NullPointerException) {
                    println("NPE On settings property | name: ${it.name} | Value: ${it.value}")

                    throw IllegalStateException("Settings not valid! Check console for more information")
                }
            }
        }
    }

    fun get() = this.value!!
}
