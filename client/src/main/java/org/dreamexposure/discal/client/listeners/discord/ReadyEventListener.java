package org.dreamexposure.discal.client.listeners.discord;

import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.object.util.Image;
import org.dreamexposure.discal.client.DisCalClient;
import org.dreamexposure.discal.client.message.MessageManager;
import org.dreamexposure.discal.client.module.announcement.AnnouncementThreader;
import org.dreamexposure.discal.client.service.KeepAliveHandler;
import org.dreamexposure.discal.client.service.TimeManager;
import org.dreamexposure.discal.core.logger.Logger;
import org.dreamexposure.discal.core.utils.GlobalConst;

/**
 * @author NovaFox161
 * Date Created: 9/8/2018
 * For Project: DisCal-Discord-Bot
 * Author Website: https://www.novamaday.com
 * Company Website: https://www.dreamexposure.org
 * Contact: nova@dreamexposure.org
 */
@SuppressWarnings({"OptionalGetWithoutIsPresent", "ConstantConditions"})
public class ReadyEventListener {

	public static void handle(ReadyEvent event) {
		Logger.getLogger().debug("Ready!");
		try {
			//Start keep-alive
			KeepAliveHandler.startKeepAlive(60);

			TimeManager.getManager().init();

			//Lets test the new announcement multi-threader...
			AnnouncementThreader.getThreader().init();

			GlobalConst.iconUrl = DisCalClient.getClient().getApplicationInfo().block().getIcon(Image.Format.PNG).get();

			MessageManager.reloadLangs();

			Logger.getLogger().debug("[ReadyEvent] Connection success! Session ID: " + event.getSessionId());
		} catch (Exception e) {
			Logger.getLogger().exception(null, "BAD!!!", e, ReadyEventListener.class);
		}
	}
}