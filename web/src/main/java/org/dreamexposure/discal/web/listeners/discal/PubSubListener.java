package org.dreamexposure.discal.web.listeners.discal;

import com.google.common.eventbus.Subscribe;

import org.dreamexposure.discal.core.object.BotSettings;
import org.dreamexposure.discal.core.object.network.discal.ConnectedClient;
import org.dreamexposure.discal.web.DisCalWeb;
import org.dreamexposure.novautils.events.network.pubsub.PubSubReceiveEvent;

@SuppressWarnings({"Duplicates", "UnstableApiUsage"})
public class PubSubListener {
	@Subscribe
	public static void handle(PubSubReceiveEvent event) {
		//Handle keep alive...
		if (event.getChannelName().equalsIgnoreCase(BotSettings.PUBSUB_PREFIX.get() + "/ToServer/KeepAlive")) {
			if (DisCalWeb.getNetworkInfo().clientExists(event.getClient())) {
				//In network, update info...
				ConnectedClient cc = DisCalWeb.getNetworkInfo().getClient(event.getClient());

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

				DisCalWeb.getNetworkInfo().addClient(cc);
			}
		}
	}
}
