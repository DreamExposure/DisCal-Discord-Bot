package org.dreamexposure.discal.client;

import org.dreamexposure.discal.client.listeners.discal.CrossTalkEventListener;
import org.dreamexposure.discal.client.listeners.discord.ReadyEventListener;
import org.dreamexposure.discal.client.message.MessageManager;
import org.dreamexposure.discal.client.module.command.*;
import org.dreamexposure.discal.client.service.KeepAliveHandler;
import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.logger.Logger;
import org.dreamexposure.discal.core.network.google.Authorization;
import org.dreamexposure.discal.core.object.BotSettings;
import org.dreamexposure.novautils.event.EventManager;
import org.dreamexposure.novautils.network.crosstalk.ClientSocketHandler;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventDispatcher;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class DisCalClient {
	private static IDiscordClient client;

	public static void main(String[] args) throws IOException {
		//Get settings
		Properties p = new Properties();
		p.load(new FileReader(new File("settings.properties")));
		BotSettings.init(p);

		//Init logger
		Logger.getLogger().init();

		//Handle client setup
		client = createClient();

		//Register discord events
		EventDispatcher dispatcher = client.getDispatcher();
		dispatcher.registerListener(new ReadyEventListener());

		//Register discal events
		EventManager.get().init();
		EventManager.get().getEventBus().register(new CrossTalkEventListener());

		//Login
		client.login();

		//Register commands
		CommandExecutor executor = CommandExecutor.getExecutor().enable();
		executor.registerCommand(new HelpCommand());
		executor.registerCommand(new DisCalCommand());
		executor.registerCommand(new CalendarCommand());
		executor.registerCommand(new AddCalendarCommand());
		executor.registerCommand(new TimeCommand());
		executor.registerCommand(new LinkCalendarCommand());
		executor.registerCommand(new EventListCommand());
		executor.registerCommand(new EventCommand());
		executor.registerCommand(new RsvpCommand());
		executor.registerCommand(new AnnouncementCommand());
		executor.registerCommand(new DevCommand());

		//Connect to MySQL
		DatabaseManager.getManager().connectToMySQL();
		DatabaseManager.getManager().createTables();

		//Start Google authorization daemon
		Authorization.getAuth().init();

		//Load lang files
		MessageManager.reloadLangs();

		//Start CrossTalk client
		ClientSocketHandler.setValues(BotSettings.CROSSTALK_SERVER_HOST.get(), Integer.valueOf(BotSettings.CROSSTALK_SERVER_PORT.get()), BotSettings.CROSSTALK_CLIENT_HOST.get(), Integer.valueOf(BotSettings.CROSSTALK_CLIENT_PORT.get()));

		ClientSocketHandler.initListener();

		KeepAliveHandler.startKeepAlive(60);
	}

	/**
	 * Creates the DisCal bot client.
	 *
	 * @return The client if successful, otherwise <code>null</code>.
	 */
	private static IDiscordClient createClient() {
		ClientBuilder clientBuilder = new ClientBuilder().withToken(BotSettings.TOKEN.get());
		//In case of disconnects and shit
		clientBuilder.setMaxReconnectAttempts(10);
		clientBuilder.setMaxReconnectAttempts(10);
		//Handle shard count and index.
		clientBuilder.setShard(Integer.valueOf(BotSettings.SHARD_INDEX.get()), Integer.valueOf(BotSettings.SHARD_COUNT.get()));
		return clientBuilder.build();
	}

	//Public stuffs
	public static IDiscordClient getClient() {
		return client;
	}
}