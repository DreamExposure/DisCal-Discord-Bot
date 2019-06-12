package org.dreamexposure.discal.server;

import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.logger.Logger;
import org.dreamexposure.discal.core.network.google.Authorization;
import org.dreamexposure.discal.core.object.BotSettings;
import org.dreamexposure.discal.core.object.network.discal.NetworkInfo;
import org.dreamexposure.discal.server.handler.DiscordAccountHandler;
import org.dreamexposure.discal.server.listeners.PubSubListener;
import org.dreamexposure.discal.server.network.discordbots.UpdateDisBotData;
import org.dreamexposure.discal.server.network.discordpw.UpdateDisPwData;
import org.dreamexposure.novautils.event.EventManager;
import org.dreamexposure.novautils.network.pubsub.PubSubManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

@SpringBootApplication
@EnableRedisHttpSession
public class DisCalServer {
	private static NetworkInfo networkInfo = new NetworkInfo();

	public static void main(String[] args) throws IOException {
		//Get settings
		Properties p = new Properties();
		p.load(new FileReader(new File("settings.properties")));
		BotSettings.init(p);

		//Init logger
		Logger.getLogger().init();

		//Register DisCal events
		EventManager.get().init();
		EventManager.get().getEventBus().register(new PubSubListener());

		//Connect to MySQL
		DatabaseManager.getManager().connectToMySQL();
		DatabaseManager.getManager().createTables();

		//Start Google authorization daemon
		Authorization.getAuth().init();

		//Start Spring
		if (BotSettings.RUN_API.get().equalsIgnoreCase("true")) {
			try {
				DiscordAccountHandler.getHandler().init();
				SpringApplication.run(DisCalServer.class, args);
			} catch (Exception e) {
				e.printStackTrace();
				Logger.getLogger().exception(null, "'Spring ERROR' by 'PANIC! AT THE WEBSITE'", e, true, DisCalServer.class);
			}
		}

		//Start Redis PubSub Listeners
		PubSubManager.get().init(BotSettings.REDIS_HOSTNAME.get(), Integer.valueOf(BotSettings.REDIS_PORT.get()), "N/a", BotSettings.REDIS_PASSWORD.get());
		//We must register each channel we want to use. This is super important.
		PubSubManager.get().register(-1, "DisCal/ToServer/KeepAlive");

		//Handle the rest of the bullshit
		UpdateDisBotData.init();
		UpdateDisPwData.init();
	}

	public static NetworkInfo getNetworkInfo() {
		return networkInfo;
	}
}