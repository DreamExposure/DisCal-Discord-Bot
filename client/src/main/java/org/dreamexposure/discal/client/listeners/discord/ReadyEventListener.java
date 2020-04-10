package org.dreamexposure.discal.client.listeners.discord;

import org.dreamexposure.discal.client.DisCalClient;
import org.dreamexposure.discal.client.message.MessageManager;
import org.dreamexposure.discal.core.logger.Logger;
import org.dreamexposure.discal.core.utils.GlobalConst;

import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.object.util.Image;

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
		Logger.getLogger().debug("Ready!", false);
		try {
			GlobalConst.iconUrl = DisCalClient.getClient().getApplicationInfo().block().getIcon(Image.Format.PNG).get();

			MessageManager.reloadLangs();

			Logger.getLogger().debug("[ReadyEvent] Connection success! Session ID: " + event.getSessionId(), false);

			Logger.getLogger().status("Ready Event Success!", null);
		} catch (Exception e) {
			Logger.getLogger().exception("BAD!!!", e, true, ReadyEventListener.class);
		}
	}
}