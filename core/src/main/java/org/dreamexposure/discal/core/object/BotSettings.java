package org.dreamexposure.discal.core.object;

import java.util.Properties;

/**
 * Created by Nova Fox on 11/10/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
public enum BotSettings {
    SQL_MASTER_HOST, SQL_MASTER_PORT,
    SQL_MASTER_USER, SQL_MASTER_PASS,

    SQL_SLAVE_HOST, SQL_SLAVE_PORT,
    SQL_SLAVE_USER, SQL_SLAVE_PASS,

    SQL_DB, SQL_PREFIX,

    REDIS_PASSWORD, REDIS_HOSTNAME, REDIS_PORT,
    PUBSUB_PREFIX,

    COM_USER, COM_PASS, COM_SUB_DOMAIN,

    TOKEN, SECRET, ID,

    GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET, CREDENTIALS_COUNT,

    SHARD_COUNT, SHARD_INDEX,

    LANG_FOLDER, LOG_FOLDER, CREDENTIAL_FOLDER,

    PW_TOKEN, DBO_TOKEN,

    TIME_OUT, PORT,

    REDIR_URI, REDIR_URL,

    INVITE_URL, SUPPORT_INVITE,

    API_URL, API_URL_INTERNAL,

    DEBUG_WEBHOOK, ERROR_WEBHOOK, STATUS_WEBHOOK,

    USE_REDIS_STORES, USE_WEBHOOKS, UPDATE_SITES, USE_RESTART_SERVICE,

    BOT_API_TOKEN,

    RESTART_IP, RESTART_PORT, RESTART_SSH_KEY, RESTART_USER, RESTART_CMD,

    PROFILE;


    private String val;

    public static void init(final Properties properties) {
        for (final BotSettings bs : values()) {
            bs.set(properties.getProperty(bs.name()));
        }
    }

    public String get() {
        return this.val;
    }

    public void set(final String _val) {
        this.val = _val;
    }
}