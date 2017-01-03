package com.cloudcraftgaming;

import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.handle.obj.Status;
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
            throw new IllegalArgumentException("The Bot Token has not be specified!");

        IDiscordClient client = createClient(args[0], true);
        if (client == null)
            throw new NullPointerException("Failed to log in! Client cannot be null!");

        //Set client defaults
        client.changeStatus(Status.game("Google Calendar"));

        //Register events
        EventDispatcher dispatcher = client.getDispatcher();

        //Accept commands
        while (true) {
            System.out.println("Enter a command below: ");
            String input = System.console().readLine();
            Boolean cmdValid = false;

            if ("exit".equalsIgnoreCase(input)) {
                System.out.println("Shutting down Discord bot!");
                try {
                    client.logout();
                } catch (DiscordException e) {
                    //No need to print, exiting anyway.
                }
                System.exit(0);
            }
            if ("?".equalsIgnoreCase(input)) {
                cmdValid = true;
                System.out.println("Valid console commands: ");
                System.out.println("exit");
            }

            if (!cmdValid) {
                System.out.println("Command not found! Use ? to list all commands.");
                System.out.println();
            }
        }
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