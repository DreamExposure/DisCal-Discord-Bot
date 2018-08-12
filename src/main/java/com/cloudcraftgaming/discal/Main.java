package com.cloudcraftgaming.discal;

import com.cloudcraftgaming.discal.api.DisCalAPI;
import com.cloudcraftgaming.discal.api.database.DatabaseManager;
import com.cloudcraftgaming.discal.api.message.MessageManager;
import com.cloudcraftgaming.discal.api.network.google.Authorization;
import com.cloudcraftgaming.discal.api.object.BotSettings;
import com.cloudcraftgaming.discal.bot.internal.consolecommand.ConsoleCommandExecutor;
import com.cloudcraftgaming.discal.bot.listeners.ReadyEventListener;
import com.cloudcraftgaming.discal.bot.module.command.*;
import com.cloudcraftgaming.discal.logger.Logger;
import com.cloudcraftgaming.discal.web.handler.DiscordAccountHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.util.DiscordException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by Nova Fox on 1/2/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
@SuppressWarnings("ThrowableNotThrown")
@SpringBootApplication
public class Main {
	public static String version = "2.1.0";

	public static void main(String[] args) throws IOException {
		//Get bot settings
		Properties p = new Properties();
		p.load(new FileReader(new File("settings.properties")));
		BotSettings.init(p);

		//Init logger
		Logger.getLogger().init();

		//Log in to discord
		DisCalAPI api = DisCalAPI.getAPI();

		api.init(createClient(BotSettings.TOKEN.get()));
		if (api.getClient() == null)
			throw new NullPointerException("Failed to build! Client cannot be null!");

		//Register events
		EventDispatcher dispatcher = api.getClient().getDispatcher();
		dispatcher.registerListener(new ReadyEventListener());

		api.getClient().login();

		Authorization.getAuth().init();

		//Connect to MySQL
		DatabaseManager.getManager().connectToMySQL();
		DatabaseManager.getManager().createTables();

		//Start Spring (catch any issues from it so only the site goes down without affecting bot....)
		if (BotSettings.RUN_API.get().equalsIgnoreCase("true")) {
			try {
				DiscordAccountHandler.getHandler().init();
				SpringApplication.run(Main.class, args);
			} catch (Exception e) {
				e.printStackTrace();
				Logger.getLogger().exception(null, "'Spring ERROR' by 'PANIC! AT THE WEBSITE'", e, Main.class, true);
			}
		}

		//Register modules
		CommandExecutor executor = CommandExecutor.getExecutor().enable();
		executor.registerCommand(new HelpCommand());
		executor.registerCommand(new DisCalCommand());
		executor.registerCommand(new CalendarCommand());
		executor.registerCommand(new AddCalendarCommand());
		executor.registerCommand(new LinkCalendarCommand());
		executor.registerCommand(new TimeCommand());
		executor.registerCommand(new EventListCommand());
		executor.registerCommand(new EventCommand());
		executor.registerCommand(new RsvpCommand());
		executor.registerCommand(new AnnouncementCommand());
		executor.registerCommand(new DevCommand());

		//Load language files.
		MessageManager.loadLangs();

		//Accept commands
		ConsoleCommandExecutor.init();
	}

	/**
	 * Creates the DisCal bot client.
	 *
	 * @param token The Bot Token.
	 * @return The client if successful, otherwise <code>null</code>.
	 */
	private static IDiscordClient createClient(String token) {
		ClientBuilder clientBuilder = new ClientBuilder();
		clientBuilder.withToken(token).withRecommendedShardCount();
		try {
			return clientBuilder.build();
		} catch (DiscordException e) {
			e.printStackTrace();
		}
		return null;
	}
}