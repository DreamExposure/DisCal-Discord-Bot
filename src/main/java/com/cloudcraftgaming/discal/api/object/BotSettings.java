package com.cloudcraftgaming.discal.api.object;

import java.util.Properties;

/**
 * Created by Nova Fox on 11/10/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
public enum BotSettings {
	SQL_HOST, SQL_USER, SQL_PASSWORD,
	SQL_DB, SQL_PORT, SQL_PREFIX, TOKEN, SECRET, ID,
	LANG_PATH, PW_TOKEN, DBO_TOKEN, UPDATE_SITES, GOOGLE_CLIENT_ID,
	GOOGLE_CLIENT_SECRET, RUN_API, PORT;

	private String val;

	BotSettings() {
	}

	public static void init(Properties properties) {
		for (BotSettings s : values()) {
			s.set(properties.getProperty(s.name()));
		}
	}

	public String get() {
		return val;
	}

	public void set(String val) {
		this.val = val;
	}
}