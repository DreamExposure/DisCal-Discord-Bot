package org.dreamexposure.discal.client.listeners.discord;

import org.dreamexposure.discal.client.DisCalClient;
import org.dreamexposure.discal.client.message.MessageManager;
import org.dreamexposure.discal.client.module.announcement.AnnouncementThreader;
import org.dreamexposure.discal.client.service.KeepAliveHandler;
import org.dreamexposure.discal.client.service.TimeManager;
import org.dreamexposure.discal.core.logger.Logger;
import org.dreamexposure.discal.core.utils.GlobalConst;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.ReadyEvent;

/**
 * @author NovaFox161
 * Date Created: 9/8/2018
 * For Project: DisCal-Discord-Bot
 * Author Website: https://www.novamaday.com
 * Company Website: https://www.dreamexposure.org
 * Contact: nova@dreamexposure.org
 */
public class ReadyEventListener {
	@EventSubscriber
	public void onReadyEvent(ReadyEvent event) {
		Logger.getLogger().debug("Ready!");
		try {
			//Start keep-alive
			KeepAliveHandler.startKeepAlive(60);

			TimeManager.getManager().init();

			//Lets test the new announcement multi-threader...
			AnnouncementThreader.getThreader().init();

			GlobalConst.iconUrl = DisCalClient.getClient().getOurUser().getAvatarURL();

			MessageManager.reloadLangs();

			Logger.getLogger().debug("[ReadyEvent] Connection success!");
		} catch (Exception e) {
			Logger.getLogger().exception(null, "BAD!!!", e, this.getClass());
		}
	}
}