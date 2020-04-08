package org.dreamexposure.discal.core.utils;

import org.dreamexposure.discal.core.database.DatabaseManager;

import java.util.UUID;

import discord4j.core.object.util.Snowflake;

/**
 * Created by Nova Fox on 11/10/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
public class AnnouncementUtils {
	/**
	 * Checks if the announcement exists.
	 *
	 * @param value The announcement ID.
	 * @return <code>true</code> if the announcement exists, else <code>false</code>.
	 */
	public static Boolean announcementExists(String value, Snowflake guildId) {
		try {
			UUID id = UUID.fromString(value);
			return DatabaseManager.getAnnouncement(id, guildId).block() != null;
		} catch (Exception e) {
			return false;
		}
	}
}