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


	TOKEN, SECRET, ID,
	SHARD_COUNT, SHARD_INDEX,
	LANG_PATH,
	PW_TOKEN, DBO_TOKEN, UPDATE_SITES,
	GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET,
	RUN_API, TIME_OUT, PORT, REDIR_URI, REDIR_URL,
	LOG_FOLDER,
	CROSSTALK_SERVER_PORT, CROSSTALK_SERVER_HOST,
	CROSSTALK_CLIENT_PORT, CROSSTALK_CLIENT_HOST,

	REDIS_PASSWORD, REDIS_HOSTNAME, REDIS_PORT,

	USE_REDIS_STORES, USE_WEBHOOKS,

	DEBUG_WEBHOOK, ERROR_WEBHOOK, STATUS_WEBHOOK;

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