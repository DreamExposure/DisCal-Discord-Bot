package com.cloudcraftgaming;

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
    public static void main(String[] args) {
        if (args.length < 1) // Needs a bot token provided
            throw new IllegalArgumentException("This bot needs at least 1 argument!");

        IDiscordClient client = createClient(args[0], true);
        EventDispatcher dispatcher = client.getDispatcher();
    }

    private static IDiscordClient createClient(String token, boolean login) {
        ClientBuilder clientBuilder = new ClientBuilder(); // Creates the ClientBuilder instance
        clientBuilder.withToken(token); // Adds the login info to the builder
        try {
            if (login) {
                return clientBuilder.login(); // Creates the client instance and logs the client in
            } else {
                return clientBuilder.build(); // Creates the client instance but it doesn't log the client in yet, you would have to call client.login() yourself
            }
        } catch (DiscordException e) { // This is thrown if there was a problem building the client
            e.printStackTrace();
        }
        return null;
    }
}