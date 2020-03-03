package org.dreamexposure.discal.server;

import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.logger.Logger;
import org.dreamexposure.discal.core.network.google.Authorization;
import org.dreamexposure.discal.core.object.BotSettings;
import org.dreamexposure.discal.core.object.network.discal.NetworkInfo;
import org.dreamexposure.discal.server.network.discal.NetworkMediator;
import org.dreamexposure.discal.server.network.discordbots.UpdateDisBotData;
import org.dreamexposure.discal.server.network.discordpw.UpdateDisPwData;
import org.dreamexposure.discal.server.utils.Authentication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.session.SessionAutoConfiguration;
import org.springframework.boot.system.ApplicationPid;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

@SpringBootApplication(exclude = SessionAutoConfiguration.class)
public class DisCalServer {
	private static NetworkInfo networkInfo = new NetworkInfo();

	public static void main(String[] args) throws IOException {
		//Get settings
		Properties p = new Properties();
		p.load(new FileReader(new File("settings.properties")));
		BotSettings.init(p);

		//Init logger
		Logger.getLogger().init();

		//Connect to MySQL
		DatabaseManager.getManager().connectToMySQL();
		DatabaseManager.getManager().handleMigrations();

		//Start Google authorization daemon
		Authorization.getAuth().init();

		//Start Spring
		try {
			SpringApplication app = new SpringApplication(DisCalServer.class);
			app.setAdditionalProfiles(BotSettings.PROFILE.get());
			app.run(args);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.getLogger().exception(null, "'Spring ERROR' by 'PANIC! AT THE API'", e, true, DisCalServer.class);
		}

		//Start network monitoring
		NetworkMediator.get().init();

		//Handle the rest of the bullshit
		UpdateDisBotData.init();
		UpdateDisPwData.init();
		Authentication.init();

		//Save pid...
		networkInfo.setPid(new ApplicationPid().toString());

		//Add shutdown hooks...
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			Logger.getLogger().status("Shutting down API", "API Shutting down...");
			Authentication.shutdown();
			NetworkMediator.get().shutdown();
			UpdateDisBotData.shutdown();
			UpdateDisPwData.shutdown();
			DatabaseManager.getManager().disconnectFromMySQL();
		}));
	}

	public static NetworkInfo getNetworkInfo() {
		return networkInfo;
	}
}