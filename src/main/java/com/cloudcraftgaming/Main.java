package com.cloudcraftgaming;

import com.cloudcraftgaming.internal.consolecommand.ConsoleCommandExecutor;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.util.DiscordException;

/**
 * Created by Nova Fox on 1/2/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
@SuppressWarnings("SameParameterValue")
public class Main {
    public static IDiscordClient client;

    public static void main(String[] args) {
        if (args.length < 1) // Needs a bot token provided
            throw new IllegalArgumentException("The Bot Token has not be specified!");

        client = createClient(args[0], true);
        if (client == null)
            throw new NullPointerException("Failed to log in! Client cannot be null!");

        //Register events
        EventDispatcher dispatcher = client.getDispatcher();

        //Accept commands
        ConsoleCommandExecutor.init();
    }

    private static IDiscordClient createClient(String token, boolean login) {
        ClientBuilder clientBuilder = new ClientBuilder(); // Creates the ClientBuilder instance
        clientBuilder.withToken(token); // Adds the login info to the builder
        try {
            if (login) {
                return clientBuilder.login();
            } else {
                return clientBuilder.build();
            }
        } catch (DiscordException e) {
            e.printStackTrace();
        }
        return null;
    }
}