package org.dreamexposure.discal.server.listeners;

import com.google.common.eventbus.Subscribe;
import org.dreamexposure.discal.core.object.BotSettings;
import org.dreamexposure.discal.core.object.network.discal.ConnectedClient;
import org.dreamexposure.discal.server.DisCalServer;
import org.dreamexposure.novautils.events.network.pubsub.PubSubReceiveEvent;

/**
 * @author NovaFox161
 * Date Created: 9/8/2018
 * For Project: DisCal-Discord-Bot
 * Author Website: https://www.novamaday.com
 * Company Website: https://www.dreamexposure.org
 * Contact: nova@dreamexposure.org
 */
@SuppressWarnings({"Duplicates", "UnstableApiUsage"})
public class PubSubListener {
	@Subscribe
	public static void handle(PubSubReceiveEvent event) {
		//Handle keep alive...
		if (event.getChannelName().equalsIgnoreCase(BotSettings.PUBSUB_PREFIX.get() + "/ToServer/KeepAlive")) {
			if (DisCalServer.getNetworkInfo().clientExists(event.getClient())) {
				//In network, update info...
				ConnectedClient cc = DisCalServer.getNetworkInfo().getClient(event.getClient());

				cc.setLastKeepAlive(System.currentTimeMillis());
				cc.setConnectedServers(event.getData().getInt("Server-Count"));
				cc.setUptime(event.getData().getString("Uptime"));
				cc.setMemUsed(event.getData().getDouble("Mem-Used"));
			} else {
				//Not in network, add info...
				ConnectedClient cc = new ConnectedClient(event.getClient());

				cc.setLastKeepAlive(System.currentTimeMillis());
				cc.setConnectedServers(event.getData().getInt("Server-Count"));
				cc.setUptime(event.getData().getString("Uptime"));
				cc.setMemUsed(event.getData().getDouble("Mem-Used"));

				DisCalServer.getNetworkInfo().addClient(cc);
			}
		}
	}
}
