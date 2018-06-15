package com.cloudcraftgaming.discal.api;

import sx.blah.discord.api.IDiscordClient;

public class DisCalAPI {
	private static DisCalAPI instance;

	private static IDiscordClient client;

	//final global variables.
	public String iconUrl;
	public final long novaId = 130510525770629121L;
	public final long xaanitId = 233611560545812480L;
	public final long calId = 142107863307780097L;
	public final long dreamId = 282662248365817867L;

	private DisCalAPI() {
	}

	public static DisCalAPI getAPI() {
		if (instance == null)
			instance = new DisCalAPI();

		return instance;
	}

	public void init(IDiscordClient _client) {
		client = _client;
		iconUrl = client.getGuildByID(266063520112574464L).getIconURL();
	}

	public IDiscordClient getClient() {
		return client;
	}
}