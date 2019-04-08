package org.dreamexposure.discal.server.listeners;

import com.google.common.eventbus.Subscribe;
import org.dreamexposure.discal.core.logger.Logger;
import org.dreamexposure.discal.core.object.network.discal.ConnectedClient;
import org.dreamexposure.discal.server.DisCalServer;
import org.dreamexposure.novautils.events.network.crosstalk.CrossTalkReceiveEvent;

/**
 * @author NovaFox161
 * Date Created: 9/8/2018
 * For Project: DisCal-Discord-Bot
 * Author Website: https://www.novamaday.com
 * Company Website: https://www.dreamexposure.org
 * Contact: nova@dreamexposure.org
 */
@SuppressWarnings({"Duplicates", "UnstableApiUsage"})
public class CrossTalkListener {
	@Subscribe
	public static void handle(CrossTalkReceiveEvent event) {
		//Handle keep alive...
		if (event.getData().has("Reason") && event.getData().getString("Reason").equalsIgnoreCase("Keep-Alive")) {
			Logger.getLogger().debug("Received Keep Alive for Client: " + event.getClientIndex(), false);
			if (DisCalServer.getNetworkInfo().clientExists(event.getClientIndex())) {
				//In network, update info...
				ConnectedClient cc = DisCalServer.getNetworkInfo().getClient(event.getClientIndex());

				cc.setLastKeepAlive(System.currentTimeMillis());
				cc.setConnectedServers(event.getData().getInt("Server-Count"));
				cc.setUptime(event.getData().getString("Uptime"));
				cc.setMemUsed(event.getData().getDouble("Mem-Used"));

			} else {
				//Not in network, add info...
				ConnectedClient cc = new ConnectedClient(event.getClientIndex(), event.getClientIp(), event.getClientPort());

				cc.setLastKeepAlive(System.currentTimeMillis());
				cc.setConnectedServers(event.getData().getInt("Server-Count"));
				cc.setUptime(event.getData().getString("Uptime"));
				cc.setMemUsed(event.getData().getDouble("Mem-Used"));

				DisCalServer.getNetworkInfo().getClients().add(cc);

				Logger.getLogger().status("Client Connected to Network", "Shard Index of Connected Client: " + cc.getClientIndex());
			}
		}

		//TODO: Handle the rest of the stuff we may have to handle....
	}
}
