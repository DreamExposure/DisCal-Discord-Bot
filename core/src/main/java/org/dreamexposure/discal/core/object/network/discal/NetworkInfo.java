package org.dreamexposure.discal.core.object.network.discal;

import java.util.ArrayList;
import java.util.List;

/**
 * @author NovaFox161
 * Date Created: 9/8/2018
 * For Project: DisCal-Discord-Bot
 * Author Website: https://www.novamaday.com
 * Company Website: https://www.dreamexposure.org
 * Contact: nova@dreamexposure.org
 */
public class NetworkInfo {
	private List<ConnectedClient> clients = new ArrayList<>();

	//Getters
	public List<ConnectedClient> getClients() {
		return clients;
	}

	public boolean clientExists(int clientIndex) {
		for (ConnectedClient cc : clients) {
			if (cc.getClientIndex() == clientIndex)
				return true;
		}
		return false;
	}

	public ConnectedClient getClient(int clientIndex) {
		for (ConnectedClient cc : clients) {
			if (cc.getClientIndex() == clientIndex)
				return cc;
		}
		return null;
	}

	public int getTotalGuildCount() {
		int count = 0;
		for (ConnectedClient cc : clients) {
			count += cc.getConnectedServers();
		}

		return count;
	}

	//Setters
}