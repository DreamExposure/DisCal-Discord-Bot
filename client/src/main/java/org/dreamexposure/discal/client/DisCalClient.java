package org.dreamexposure.discal.client;

import org.dreamexposure.discal.client.listeners.discord.ChannelDeleteListener;
import org.dreamexposure.discal.client.listeners.discord.MessageCreateListener;
import org.dreamexposure.discal.client.listeners.discord.ReadyEventListener;
import org.dreamexposure.discal.client.listeners.discord.RoleDeleteListener;
import org.dreamexposure.discal.client.message.Messages;
import org.dreamexposure.discal.client.module.announcement.AnnouncementThread;
import org.dreamexposure.discal.client.module.command.AddCalendarCommand;
import org.dreamexposure.discal.client.module.command.AnnouncementCommand;
import org.dreamexposure.discal.client.module.command.CalendarCommand;
import org.dreamexposure.discal.client.module.command.CommandExecutor;
import org.dreamexposure.discal.client.module.command.DevCommand;
import org.dreamexposure.discal.client.module.command.DisCalCommand;
import org.dreamexposure.discal.client.module.command.EventCommand;
import org.dreamexposure.discal.client.module.command.EventListCommand;
import org.dreamexposure.discal.client.module.command.HelpCommand;
import org.dreamexposure.discal.client.module.command.LinkCalendarCommand;
import org.dreamexposure.discal.client.module.command.RsvpCommand;
import org.dreamexposure.discal.client.module.command.TimeCommand;
import org.dreamexposure.discal.client.service.KeepAliveHandler;
import org.dreamexposure.discal.client.service.TimeManager;
import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.logger.LogFeed;
import org.dreamexposure.discal.core.logger.object.LogObject;
import org.dreamexposure.discal.core.network.google.Authorization;
import org.dreamexposure.discal.core.object.BotSettings;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.session.SessionAutoConfiguration;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.util.Properties;

import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.channel.TextChannelDeleteEvent;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.role.RoleDeleteEvent;
import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.Presence;
import discord4j.core.shard.ShardingStrategy;
import discord4j.discordjson.json.GuildData;
import discord4j.discordjson.json.MessageData;
import discord4j.store.api.mapping.MappingStoreService;
import discord4j.store.api.service.StoreService;
import discord4j.store.jdk.JdkStoreService;
import discord4j.store.redis.RedisStoreService;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootApplication(exclude = SessionAutoConfiguration.class)
public class DisCalClient {
    private static GatewayDiscordClient client;

    public static void main(String[] args) throws IOException {
        //Get settings
        Properties p = new Properties();
        p.load(new FileReader(new File("settings.properties")));
        BotSettings.init(p);

        //Start Google authorization daemon
        Authorization.getAuth().init();

        //Load lang files
        Messages.reloadLangs().subscribe();

        //Register commands
        CommandExecutor.registerCommand(new HelpCommand());
        CommandExecutor.registerCommand(new DisCalCommand());
        CommandExecutor.registerCommand(new CalendarCommand());
        CommandExecutor.registerCommand(new AddCalendarCommand());
        CommandExecutor.registerCommand(new TimeCommand());
        CommandExecutor.registerCommand(new LinkCalendarCommand());
        CommandExecutor.registerCommand(new EventListCommand());
        CommandExecutor.registerCommand(new EventCommand());
        CommandExecutor.registerCommand(new RsvpCommand());
        CommandExecutor.registerCommand(new AnnouncementCommand());
        CommandExecutor.registerCommand(new DevCommand());

        //Start some of the daemon threads
        KeepAliveHandler.startKeepAlive(60);

        TimeManager.getManager().init();

        //Start Spring
        if (BotSettings.RUN_API.get().equalsIgnoreCase("true")) {
            try {
                SpringApplication app = new SpringApplication(DisCalClient.class);
                app.setAdditionalProfiles(BotSettings.PROFILE.get());
                app.run(args);
            } catch (Exception e) {
                e.printStackTrace();
                LogFeed.log(LogObject
                    .forException("Spring Error", "by 'PANIC! at the backend coms!'", e,
                        DisCalClient.class));
            }
        }

        //Add shutdown hooks...
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LogFeed.log(LogObject.forStatus("Shutting down Shard", "Shard shutting down"));

            TimeManager.getManager().shutdown();
            DatabaseManager.disconnectFromMySQL();

            client.logout().subscribe();
        }));

        //Login
        DiscordClientBuilder.create(BotSettings.TOKEN.get())
            .build().gateway()
            .setSharding(getStrategy())
            .setStoreService(getStores())
            .setInitialStatus(shard -> Presence.online(Activity.playing("Booting Up!")))
            .withGateway(client -> {
                DisCalClient.client = client;

                //Register listeners
                Mono<Void> onReady = client.on(ReadyEvent.class)
                    .flatMap(ReadyEventListener::handle)
                    .then();

                Mono<Void> onTextChannelDelete = client.on(TextChannelDeleteEvent.class)
                    .flatMap(ChannelDeleteListener::handle)
                    .then();

                Mono<Void> onRoleDelete = client.on(RoleDeleteEvent.class)
                    .flatMap(RoleDeleteListener::handle)
                    .then();

                Mono<Void> onCommand = client.on(MessageCreateEvent.class)
                    .flatMap(MessageCreateListener::handle)
                    .then();

                Mono<Void> startAnnouncement = Flux.interval(Duration.ofMinutes(5))
                    .onBackpressureBuffer()
                    .flatMap(i -> new AnnouncementThread(client).run())
                    .then();


                return Mono.when(onReady, onTextChannelDelete, onRoleDelete, onCommand, startAnnouncement);
            }).block();
    }

    private static ShardingStrategy getStrategy() {
        return ShardingStrategy.builder()
            .count(Integer.parseInt(BotSettings.SHARD_COUNT.get()))
            .indices(Integer.parseInt(BotSettings.SHARD_INDEX.get()))
            .build();
    }

    private static StoreService getStores() {
        if (BotSettings.USE_REDIS_STORES.get().equalsIgnoreCase("true")) {
            RedisURI uri = RedisURI.Builder
                .redis(BotSettings.REDIS_HOSTNAME.get(), Integer.parseInt(BotSettings.REDIS_PORT.get()))
                .withPassword(BotSettings.REDIS_PASSWORD.get())
                .build();

            RedisStoreService rss = new RedisStoreService.Builder()
                .redisClient(RedisClient.create(uri))
                .build();

            return MappingStoreService.create()
                .setMappings(rss, GuildData.class, MessageData.class)
                .setFallback(new JdkStoreService());
        } else {
            return new JdkStoreService();
        }
    }

    //Public stuffs
    @Deprecated
    public static GatewayDiscordClient getClient() {
        return client;
    }
}