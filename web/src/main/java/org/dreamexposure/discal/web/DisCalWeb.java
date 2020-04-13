package org.dreamexposure.discal.web;

import org.dreamexposure.discal.core.logger.LogFeed;
import org.dreamexposure.discal.core.logger.object.LogObject;
import org.dreamexposure.discal.core.network.google.Authorization;
import org.dreamexposure.discal.core.object.BotSettings;
import org.dreamexposure.discal.web.handler.DiscordAccountHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.session.SessionAutoConfiguration;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

@SpringBootApplication(exclude = SessionAutoConfiguration.class)
public class DisCalWeb {
    public static void main(String[] args) throws IOException {
        //Get settings
        Properties p = new Properties();
        p.load(new FileReader(new File("settings.properties")));
        BotSettings.init(p);

        //Start Google authorization daemon
        Authorization.getAuth().init();

        //Start Spring
        try {
            DiscordAccountHandler.getHandler().init();
            SpringApplication app = new SpringApplication(DisCalWeb.class);
            app.setAdditionalProfiles(BotSettings.PROFILE.get());
            app.run(args);
        } catch (Exception e) {
            e.printStackTrace();
            LogFeed.log(LogObject
                    .forException("'Spring error", "by 'PANIC! AT THE WEBSITE'", e, DisCalWeb.class));
            System.exit(4);
        }

        //Add shutdown hooks (probably won't work, but worth a shot for graceful shutdown)
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LogFeed.log(LogObject.forStatus("Website shutting down", "Website shutting down..."));
            DiscordAccountHandler.getHandler().shutdown();
        }));

        LogFeed.log(LogObject.forStatus("Started", "Website is now online!"));
    }
}