package com.cloudcraftgaming.discal.api;

import sx.blah.discord.api.IDiscordClient;

public class DisCalAPI {
	private static DisCalAPI instance;

	private static IDiscordClient client;

	private DisCalAPI() {
	}

	public static DisCalAPI getAPI() {
		if (instance == null)
			instance = new DisCalAPI();

		return instance;
	}

	public void init(IDiscordClient _client) {
		client = _client;
	}

	public IDiscordClient getClient() {
		return client;
	}
}