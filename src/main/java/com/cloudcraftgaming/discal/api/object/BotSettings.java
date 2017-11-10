package com.cloudcraftgaming.discal.api.object;

/**
 * Created by Nova Fox on 11/10/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
public class BotSettings {
	private String botToken;

	private String dbHostName;
	private String dbPort;
	private String dbDatabase;
	private String dbPrefix;
	private String dbUser;
	private String dbPass;

	private String langPath;

	private String botsPwToken;

	private String googleClientId;
	private String googleClientSecret;

	private boolean runDatabaseUpdater;

	//Getters
	public String getBotToken() {
		return botToken;
	}

	public String getDbHostName() {
		return dbHostName;
	}

	public String getDbPort() {
		return dbPort;
	}

	public String getDbDatabase() {
		return dbDatabase;
	}

	public String getDbPrefix() {
		return dbPrefix;
	}

	public String getDbUser() {
		return dbUser;
	}

	public String getDbPass() {
		return dbPass;
	}

	public String getLangPath() {
		return langPath;
	}

	public String getBotsPwToken() {
		return botsPwToken;
	}

	public String getGoogleClientId() {
		return googleClientId;
	}

	public String getGoogleClientSecret() {
		return googleClientSecret;
	}

	public boolean shouldRunDatabaseUpdater() {
		return runDatabaseUpdater;
	}

	//Setters
	public void setBotToken(String _botToken) {
		botToken = _botToken;
	}

	public void setDbHostName(String _dbHostName) {
		dbHostName = _dbHostName;
	}

	public void setDbPort(String _port) {
		dbPort = _port;
	}

	public void setDbDatabase(String _database) {
		dbDatabase = _database;
	}

	public void setDbPrefix(String _prefix) {
		dbPrefix = _prefix;
	}

	public void setDbUser(String _user) {
		dbUser = _user;
	}

	public void setDbPass(String _pass) {
		dbPass = _pass;
	}

	public void setLangPath(String _path) {
		langPath = _path;
	}

	public void setBotsPwToken(String _token) {
		botsPwToken = _token;
	}

	public void setGoogleClientId(String _id) {
		googleClientId = _id;
	}

	public void setGoogleClientSecret(String _secret) {
		googleClientSecret = _secret;
	}

	public void setRunDatabaseUpdater(boolean _run) {
		runDatabaseUpdater = _run;
	}
}