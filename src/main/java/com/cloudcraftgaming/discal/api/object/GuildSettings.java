package com.cloudcraftgaming.discal.api.object;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Nova Fox on 11/10/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
public class GuildSettings {
	private final long guildID;

	private boolean externalCalendar;
	private String privateKey;

	private String encryptedAccessToken;
	private String encryptedRefreshToken;

	private String controlRole;
	private String discalChannel;

	private boolean simpleAnnouncements;
	private String lang;
	private String prefix;

	private boolean patronGuild;
	private boolean devGuild;
	private Integer maxCalendars;

	private boolean twelveHour;

	private final ArrayList<String> dmAnnouncements = new ArrayList<>();

	public GuildSettings(long _guildId) {
		guildID = _guildId;

		externalCalendar = false;
		privateKey = "N/a";

		encryptedAccessToken = "N/a";
		encryptedRefreshToken = "N/a";

		controlRole = "everyone";
		discalChannel = "all";

		simpleAnnouncements = false;
		lang = "ENGLISH";
		prefix = "!";

		patronGuild = false;
		devGuild = false;
		maxCalendars = 1;

		twelveHour = true;
	}

	//Getters
	public long getGuildID() {
		return guildID;
	}

	public boolean useExternalCalendar() {
		return externalCalendar;
	}

	public String getPrivateKey() {
		return privateKey;
	}

	public String getEncryptedAccessToken() {
		return encryptedAccessToken;
	}

	public String getEncryptedRefreshToken() {
		return encryptedRefreshToken;
	}

	public String getControlRole() {
		return controlRole;
	}

	public String getDiscalChannel() {
		return discalChannel;
	}

	public boolean usingSimpleAnnouncements() {
		return simpleAnnouncements;
	}

	public String getLang() {
		return lang;
	}

	public String getPrefix() {
		return prefix;
	}

	public boolean isPatronGuild() {
		return patronGuild;
	}

	public boolean isDevGuild() {
		return devGuild;
	}

	public Integer getMaxCalendars() {
		return maxCalendars;
	}

	public boolean useTwelveHour() {
		return twelveHour;
	}

	public ArrayList<String> getDmAnnouncements() {
		return dmAnnouncements;
	}

	public String getDmAnnouncementsString() {
		StringBuilder users = new StringBuilder();
		Integer i = 0;
		for (String sub : dmAnnouncements) {
			if (i == 0) {
				users = new StringBuilder(sub);
			} else {
				users.append(",").append(sub);
			}
			i++;
		}
		return users.toString();
	}

	//Setters
	public void setUseExternalCalendar(boolean _useExternal) {
		externalCalendar = _useExternal;
	}

	public void setPrivateKey(String _privateKey) {
		privateKey = _privateKey;
	}

	public void setEncryptedAccessToken(String _access) {
		encryptedAccessToken = _access;
	}

	public void setEncryptedRefreshToken(String _refresh) {
		encryptedRefreshToken = _refresh;
	}

	public void setControlRole(String _controlRole) {
		controlRole = _controlRole;
	}

	public void setDiscalChannel(String _discalChannel) {
		discalChannel = _discalChannel;
	}

	public void setSimpleAnnouncements(boolean _simpleAnnouncements) {
		simpleAnnouncements = _simpleAnnouncements;
	}

	public void setLang(String _lang) {
		lang = _lang;
	}

	public void setPrefix(String _prefix) {
		prefix = _prefix;
	}

	public void setPatronGuild(boolean _patronGuild) {
		patronGuild = _patronGuild;
	}

	public void setDevGuild(boolean _devGuild) {
		devGuild = _devGuild;
	}

	public void setMaxCalendars(Integer _maxCalendars) {
		maxCalendars = _maxCalendars;
	}

	public void setTwelveHour(boolean _twelveHour) {
		twelveHour = _twelveHour;
	}

	public void setDmAnnouncementsFromString(String userList) {
		String[] subs = userList.split(",");
		Collections.addAll(dmAnnouncements, subs);
	}
}