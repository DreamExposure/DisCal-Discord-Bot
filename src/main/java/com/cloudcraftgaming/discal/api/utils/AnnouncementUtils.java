package com.cloudcraftgaming.discal.api.utils;

import com.cloudcraftgaming.discal.api.database.DatabaseManager;
import com.cloudcraftgaming.discal.api.object.announcement.Announcement;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

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
	 * @param event The event received.
	 * @return <code>true</code> if the announcement exists, else <code>false</code>.
	 */
	public static Boolean announcementExists(String value, MessageReceivedEvent event) {
		for (Announcement a: DatabaseManager.getManager().getAnnouncements(event.getGuild().getLongID())) {
			if (a.getAnnouncementId().toString().equals(value))
				return true;

		}
		return false;
	}
}