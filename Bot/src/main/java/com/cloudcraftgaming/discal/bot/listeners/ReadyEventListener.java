package com.cloudcraftgaming.discal.bot.listeners;

import com.cloudcraftgaming.discal.Main;
import com.cloudcraftgaming.discal.api.DisCalAPI;
import com.cloudcraftgaming.discal.api.message.MessageManager;
import com.cloudcraftgaming.discal.bot.internal.network.discordbots.UpdateDisBotData;
import com.cloudcraftgaming.discal.bot.internal.network.discordpw.UpdateDisPwData;
import com.cloudcraftgaming.discal.bot.internal.service.TimeManager;
import com.cloudcraftgaming.discal.logger.Logger;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.ReadyEvent;

/**
 * Created by Nova Fox on 1/2/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
@SuppressWarnings("unused")
public class ReadyEventListener {
	@EventSubscriber
	public void onReadyEvent(ReadyEvent event) {
		Logger.getLogger().debug("Ready!");
		try {
			TimeManager.getManager().init();

			UpdateDisBotData.init();
			UpdateDisPwData.init();

			MessageManager.reloadLangs();

			try {
				DisCalAPI.getAPI().iconUrl = DisCalAPI.getAPI().getClient().getGuildByID(266063520112574464L).getIconURL();
			} catch (Exception e) {
				Logger.getLogger().exception(null, "Fuck a duck.", e, Main.class, true);
			}

			Logger.getLogger().debug("[ReadyEvent] Connection success!");
		} catch (Exception e) {
			Logger.getLogger().exception(null, "BAD!!!", e, this.getClass(), false);
		}
	}
}