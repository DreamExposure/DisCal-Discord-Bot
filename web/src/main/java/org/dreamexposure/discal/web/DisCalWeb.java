package org.dreamexposure.discal.web;

import org.dreamexposure.discal.core.logger.Logger;
import org.dreamexposure.discal.core.network.google.Authorization;
import org.dreamexposure.discal.core.object.BotSettings;
import org.dreamexposure.discal.core.object.network.discal.NetworkInfo;
import org.dreamexposure.discal.web.handler.DiscordAccountHandler;
import org.dreamexposure.discal.web.listeners.discal.PubSubListener;
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
public class DisCalWeb {
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

		//Start Google authorization daemon
		Authorization.getAuth().init();

		//Start Spring
		try {
			DiscordAccountHandler.getHandler().init();
			SpringApplication.run(DisCalWeb.class, args);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.getLogger().exception(null, "'Spring ERROR' by 'PANIC! AT THE WEBSITE'", e, true, DisCalWeb.class);
		}

		//Start Redis PubSub Listeners
		PubSubManager.get().init(BotSettings.REDIS_HOSTNAME.get(), Integer.parseInt(BotSettings.REDIS_PORT.get()), "N/a", BotSettings.REDIS_PASSWORD.get());
		//We must register each channel we want to use. This is super important.
		PubSubManager.get().register(-1, BotSettings.PUBSUB_PREFIX.get() + "/ToServer/KeepAlive");
	}

	public static NetworkInfo getNetworkInfo() {
		return networkInfo;
	}
}