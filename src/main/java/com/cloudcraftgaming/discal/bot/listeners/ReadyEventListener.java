package com.cloudcraftgaming.discal.bot.listeners;

import com.cloudcraftgaming.discal.api.message.MessageManager;
import com.cloudcraftgaming.discal.bot.internal.network.discordbots.UpdateDisBotData;
import com.cloudcraftgaming.discal.bot.internal.network.discordpw.UpdateDisPwData;
import com.cloudcraftgaming.discal.bot.internal.service.TimeManager;
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
		TimeManager.getManager().init();

		UpdateDisBotData.init();
		UpdateDisPwData.init();

		MessageManager.reloadLangs();
	}
}