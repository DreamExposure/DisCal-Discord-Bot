package org.dreamexposure.discal.core.utils;

import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.object.announcement.Announcement;

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
	public static Boolean announcementExists(String value, long guildId) {
		for (Announcement a : DatabaseManager.getManager().getAnnouncements(guildId)) {
			if (a.getAnnouncementId().toString().equals(value))
				return true;

		}
		return false;
	}
}