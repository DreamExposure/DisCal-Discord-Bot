package org.dreamexposure.discal.client.listeners.discord;

import discord4j.core.event.domain.lifecycle.ReadyEvent;
import org.dreamexposure.discal.client.message.MessageManager;
import org.dreamexposure.discal.client.module.announcement.AnnouncementThreader;
import org.dreamexposure.discal.client.service.TimeManager;
import org.dreamexposure.discal.core.logger.Logger;

/**
 * @author NovaFox161
 * Date Created: 9/8/2018
 * For Project: DisCal-Discord-Bot
 * Author Website: https://www.novamaday.com
 * Company Website: https://www.dreamexposure.org
 * Contact: nova@dreamexposure.org
 */
public class ReadyEventListener {
	public static void handle(ReadyEvent event) {
		Logger.getLogger().debug("Ready!");
		try {
			TimeManager.getManager().init();

			//START ANNOUNCEMENT THREADER HERE
			AnnouncementThreader.getThreader().init();

			MessageManager.reloadLangs();

			Logger.getLogger().debug("[ReadyEvent] Connection success!");
		} catch (Exception e) {
			Logger.getLogger().exception(null, "BAD!!!", e, ReadyEventListener.class);
		}
	}
}