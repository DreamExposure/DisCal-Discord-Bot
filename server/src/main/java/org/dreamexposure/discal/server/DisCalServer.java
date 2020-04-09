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
import org.dreamexposure.novautils.database.DatabaseInfo;
import org.dreamexposure.novautils.database.DatabaseSettings;
import org.flywaydb.core.Flyway;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.session.SessionAutoConfiguration;
import org.springframework.boot.system.ApplicationPid;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.object.data.stored.GuildBean;
import discord4j.core.object.data.stored.MessageBean;
import discord4j.store.api.mapping.MappingStoreService;
import discord4j.store.jdk.JdkStoreService;
import discord4j.store.redis.RedisStoreService;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;

@SpringBootApplication(exclude = SessionAutoConfiguration.class)
public class DisCalServer {
	private static NetworkInfo networkInfo = new NetworkInfo();
	private static DiscordClient client;

	public static void main(String[] args) throws IOException {
		//Get settings
		Properties p = new Properties();
		p.load(new FileReader(new File("settings.properties")));
		BotSettings.init(p);

		//Init logger
		Logger.getLogger().init();

		//Handle database migrations
		if (args.length > 0 && args[0].equalsIgnoreCase("--repair"))
			handleMigrations(true);
		else
			handleMigrations(false);

		//Start Google authorization daemon
		Authorization.getAuth().init();

		client = createClient();

		//Start Spring
		try {
			SpringApplication app = new SpringApplication(DisCalServer.class);
			app.setAdditionalProfiles(BotSettings.PROFILE.get());
			app.run(args);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.getLogger().exception("'Spring ERROR' by 'PANIC! AT THE API'", e, true,
					DisCalServer.class);
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
			DatabaseManager.disconnectFromMySQL();
		}));

		Logger.getLogger().status("Started", "Server and API are now online");
	}

	private static DiscordClient createClient() {
		DiscordClientBuilder clientBuilder = new DiscordClientBuilder(BotSettings.TOKEN.get());
		clientBuilder.setShardCount(Integer.valueOf(BotSettings.SHARD_COUNT.get()));

		//Redis info + store service for caching
		if (BotSettings.USE_REDIS_STORES.get().equalsIgnoreCase("true")) {
			RedisURI uri = RedisURI.Builder
					.redis(BotSettings.REDIS_HOSTNAME.get(),
							Integer.parseInt(BotSettings.REDIS_PORT.get()))
					.withPassword(BotSettings.REDIS_PASSWORD.get())
					.build();

			RedisStoreService rss = new RedisStoreService(RedisClient.create(uri));

			MappingStoreService mss = MappingStoreService.create()
					.setMappings(rss, GuildBean.class, MessageBean.class)
					.setFallback(new JdkStoreService());

			clientBuilder.setStoreService(mss);
		} else {
			clientBuilder.setStoreService(new JdkStoreService());
		}

		return clientBuilder.build();
	}

	public static DiscordClient getClient() {
		return client;
	}

	public static NetworkInfo getNetworkInfo() {
		return networkInfo;
	}

	private static void handleMigrations(boolean repair) {
		Map<String, String> placeholders = new HashMap<>();
		placeholders.put("prefix", BotSettings.SQL_PREFIX.get());

		DatabaseSettings settings = new DatabaseSettings(
				BotSettings.SQL_MASTER_HOST.get(),
				BotSettings.SQL_MASTER_PORT.get(),
				BotSettings.SQL_DB.get(),
				BotSettings.SQL_MASTER_USER.get(),
				BotSettings.SQL_MASTER_PASS.get(),
				BotSettings.SQL_PREFIX.get()
		);
		DatabaseInfo info = org.dreamexposure.novautils.database.DatabaseManager
				.connectToMySQL(settings);

		try {
			Flyway flyway = Flyway.configure()
					.dataSource(info.getSource())
					.cleanDisabled(true)
					.baselineOnMigrate(true)
					.table(BotSettings.SQL_PREFIX.get() + "schema_history")
					.placeholders(placeholders)
					.load();

			int sm = 0;
			if (repair)
				flyway.repair();
			else
				sm = flyway.migrate();


			org.dreamexposure.novautils.database.DatabaseManager.disconnectFromMySQL(info);
			Logger.getLogger().debug("Migrations Successful, " + sm + " migrations applied!", true);
		} catch (Exception e) {
			Logger.getLogger().exception("Migrations Failure", e, true, DisCalServer.class);
			System.exit(2);
		}
	}
}