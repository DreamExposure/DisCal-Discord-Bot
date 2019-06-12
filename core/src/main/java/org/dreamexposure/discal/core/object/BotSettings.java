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

	GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET,

	SHARD_COUNT, SHARD_INDEX,

	LANG_PATH, LOG_FOLDER,

	PW_TOKEN, DBO_TOKEN,

	TIME_OUT, PORT, REDIR_URI, REDIR_URL,

	DEBUG_WEBHOOK, ERROR_WEBHOOK, STATUS_WEBHOOK,

	USE_REDIS_STORES, USE_WEBHOOKS, UPDATE_SITES, RUN_API;


	private String val;

	BotSettings() {
	}

	public static void init(Properties properties) {
		for (BotSettings s: values()) {
			s.set(properties.getProperty(s.name()));
		}
	}

	public String get() {
		return val;
	}

	public void set(String _val) {
		val = _val;
	}
}