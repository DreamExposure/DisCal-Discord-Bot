package org.dreamexposure.discal.server;

import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.logger.Logger;
import org.dreamexposure.discal.core.network.google.Authorization;
import org.dreamexposure.discal.core.object.BotSettings;
import org.dreamexposure.discal.core.object.network.discal.NetworkInfo;
import org.dreamexposure.discal.server.listeners.CrossTalkListener;
import org.dreamexposure.discal.server.network.discordbots.UpdateDisBotData;
import org.dreamexposure.discal.server.network.discordpw.UpdateDisPwData;
import org.dreamexposure.novautils.event.EventManager;
import org.dreamexposure.novautils.network.crosstalk.ServerSocketHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

//TODO: Add methods to convert WebGuild, WebRole, WebChannel, and Announcement to/from JSON <- note for self.

@SpringBootApplication
public class DisCalServer {
	private static NetworkInfo networkInfo = new NetworkInfo();

	public static void main(String[] args) throws IOException {
		//Get settings
		Properties p = new Properties();
		p.load(new FileReader(new File("settings.properties")));
		BotSettings.init(p);

		//Init logger
		Logger.getLogger().init();

		//Register DisCal events and listeners
		EventManager.get().registerEventListener(new CrossTalkListener());

		//Connect to MySQL
		DatabaseManager.getManager().connectToMySQL();
		DatabaseManager.getManager().createTables();

		//Start Google authorization daemon
		Authorization.getAuth().init();

		//Start Spring
		if (BotSettings.RUN_API.get().equalsIgnoreCase("true")) {
			try {
				//DiscordAccountHandler.getHandler().init();
				SpringApplication.run(DisCalServer.class, args);
			} catch (Exception e) {
				e.printStackTrace();
				Logger.getLogger().exception(null, "'Spring ERROR' by 'PANIC! AT THE WEBSITE'", e, DisCalServer.class);
			}
		}

		//Start CrossTalk Server
		ServerSocketHandler.setValues(Integer.valueOf(BotSettings.CROSSTALK_SERVER_PORT.get()));
		ServerSocketHandler.initListener();

		//Handle the rest of the bullshit
		UpdateDisBotData.init();
		UpdateDisPwData.init();
	}

	public static NetworkInfo getNetworkInfo() {
		return networkInfo;
	}
}