package org.dreamexposure.discal.server;

import org.dreamexposure.discal.core.calendar.CalendarAuth;
import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.logger.LogFeed;
import org.dreamexposure.discal.core.logger.object.LogObject;
import org.dreamexposure.discal.core.network.google.Authorization;
import org.dreamexposure.discal.core.object.BotSettings;
import org.dreamexposure.discal.core.object.network.discal.NetworkInfo;
import org.dreamexposure.discal.server.network.dbotsgg.UpdateDBotsData;
import org.dreamexposure.discal.server.network.discal.NetworkMediator;
import org.dreamexposure.discal.server.network.topgg.UpdateTopStats;
import org.dreamexposure.discal.server.utils.Authentication;
import org.dreamexposure.novautils.database.DatabaseInfo;
import org.dreamexposure.novautils.database.DatabaseSettings;
import org.flywaydb.core.Flyway;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.session.SessionAutoConfiguration;
import org.springframework.boot.system.ApplicationPid;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;

@SpringBootApplication(exclude = SessionAutoConfiguration.class)
public class DisCalServer {
    private static final NetworkInfo networkInfo = new NetworkInfo();
    private static DiscordClient client;

    public static void main(final String[] args) throws IOException {
        //Get settings
        final Properties p = new Properties();
        p.load(new FileReader("settings.properties"));
        BotSettings.init(p);

        if (args.length > 1 && "-forceNewAuth".equalsIgnoreCase(args[0])) {
            //Forcefully start a browser for google account authorization.
            CalendarAuth.getCalendarService(Integer.parseInt(args[1])).block(); //Block until auth completes...

            //Kill the running instance as this is only meant for generating new credentials... Illegal State basically.
            System.exit(100);
        }

        //Handle database migrations
        handleMigrations(args.length > 0 && "--repair".equalsIgnoreCase(args[0]));

        //Start Google authorization daemon
        Authorization.getAuth().init();

        client = DiscordClientBuilder.create(BotSettings.TOKEN.get()).build();

        //Start Spring
        try {
            final SpringApplication app = new SpringApplication(DisCalServer.class);
            app.setAdditionalProfiles(BotSettings.PROFILE.get());
            app.run(args);
        } catch (final Exception e) {
            e.printStackTrace();
            LogFeed.log(LogObject
                .forException("SPRING ERROR", "by 'PANIC! At The API'", e, DisCalServer.class));
        }

        //Start network monitoring
        NetworkMediator.get().init();

        //Handle the rest of the bullshit
        UpdateTopStats.init();
        UpdateDBotsData.init();
        Authentication.init();

        //Save pid...
        networkInfo.setPid(new ApplicationPid().toString());

        //Add shutdown hooks...
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LogFeed.log(LogObject.forStatus("API shutting down", "Server/API shutting down..."));
            Authentication.shutdown();
            NetworkMediator.get().shutdown();
            UpdateTopStats.shutdown();
            UpdateDBotsData.shutdown();
            DatabaseManager.disconnectFromMySQL();
        }));

        LogFeed.log(LogObject.forStatus("Started Server/API", "Server and API are now online"));
    }

    public static DiscordClient getClient() {
        return client;
    }

    public static NetworkInfo getNetworkInfo() {
        return networkInfo;
    }

    private static void handleMigrations(final boolean repair) {
        final Map<String, String> placeholders = new HashMap<>();
        placeholders.put("prefix", BotSettings.SQL_PREFIX.get());

        final DatabaseSettings settings = new DatabaseSettings(
            BotSettings.SQL_MASTER_HOST.get(),
            BotSettings.SQL_MASTER_PORT.get(),
            BotSettings.SQL_DB.get(),
            BotSettings.SQL_MASTER_USER.get(),
            BotSettings.SQL_MASTER_PASS.get(),
            BotSettings.SQL_PREFIX.get()
        );
        final DatabaseInfo info = org.dreamexposure.novautils.database.DatabaseManager
            .connectToMySQL(settings);

        try {
            final Flyway flyway = Flyway.configure()
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
                sm = flyway.migrate().migrationsExecuted;

            org.dreamexposure.novautils.database.DatabaseManager.disconnectFromMySQL(info);
            LogFeed.log(LogObject.forDebug("Migrations Successful", sm + " migrations applied!"));
        } catch (final Exception e) {
            LogFeed.log(LogObject.forException("Migrations failure", e, DisCalServer.class));
            e.printStackTrace();
            System.exit(2);
        }
    }
}