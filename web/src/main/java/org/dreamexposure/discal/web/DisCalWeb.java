package org.dreamexposure.discal.web;

import org.dreamexposure.discal.core.calendar.CalendarAuth;
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
    public static void main(final String[] args) throws IOException {
        //Get settings
        final Properties p = new Properties();
        p.load(new FileReader(new File("settings.properties")));
        BotSettings.Companion.init(p);

        if (args.length > 1 && "-forceNewAuth".equalsIgnoreCase(args[0])) {
            //Forcefully start a browser for google account authorization.
            CalendarAuth.getCalendarService(Integer.parseInt(args[1])).block(); //Block until auth completes...

            //Kill the running instance as this is only meant for generating new credentials... Illegal State basically.
            System.exit(100);
        }

        //Start Google authorization daemon
        Authorization.getAuth().init();

        //Start Spring
        try {
            DiscordAccountHandler.getHandler().init();
            final SpringApplication app = new SpringApplication(DisCalWeb.class);
            app.setAdditionalProfiles(BotSettings.PROFILE.get());
            app.run(args);
        } catch (final Exception e) {
            e.printStackTrace();
            LogFeed.log(LogObject.forException("'Spring error", "by 'PANIC! AT THE WEBSITE'", e, DisCalWeb.class));
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
