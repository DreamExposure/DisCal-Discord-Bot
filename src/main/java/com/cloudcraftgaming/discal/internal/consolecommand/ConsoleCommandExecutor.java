package com.cloudcraftgaming.discal.internal.consolecommand;

import com.cloudcraftgaming.discal.Main;
import com.cloudcraftgaming.discal.database.DatabaseManager;
import com.cloudcraftgaming.discal.module.announcement.Announcer;
import com.cloudcraftgaming.discal.module.misc.TimeManager;
import sx.blah.discord.Discord4J;
import sx.blah.discord.handle.obj.Status;
import sx.blah.discord.util.DiscordException;

/**
 * Created by Nova Fox on 1/2/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class ConsoleCommandExecutor {
    public static void init() {
        while (true) {
            System.out.println("Enter a command below: ");
            String input = System.console().readLine();
            Boolean cmdValid = false;
            if (input != null && !input.isEmpty()) {

                //Important commands first.
                if (input.equalsIgnoreCase("exit")) {
                    exitApplication();
                    return;
                }
                if (input.equalsIgnoreCase("?")) {
                    cmdValid = true;
                    System.out.println("Valid console commands: ");
                    System.out.println("exit");
                    System.out.println("status");
                    System.out.println("serverCount");
                    System.out.println("silence true/false");
                    System.out.println();
                }
                if (input.startsWith("status")) {
                    cmdValid = true;
                    String status = input.replaceAll("status ", "");
                    Main.client.changeStatus(Status.game(status));

                    System.out.println("Status changed! Check discord.");
                    System.out.println();
                }
                if (input.startsWith("serverCount")) {
                    cmdValid = true;
                    System.out.println("Server count: " + Main.client.getGuilds().size());
                }
                if (input.startsWith("silence")) {
                    cmdValid = true;
                    String value = input.replaceAll("silence ", "");
                    silenceConsole(value);
                }

                if (!cmdValid) {
                    System.out.println("Command not found! Use ? to list all commands.");
                    System.out.println();
                }
            }
        }
    }

    private static void exitApplication() {
        System.out.println("Shutting down Discord bot!");
        try {
            Main.client.logout();
        } catch (DiscordException e) {
            //No need to print, exiting anyway.
        }
        Announcer.getAnnouncer().shutdown();
        TimeManager.getManager().shutdown();
        DatabaseManager.getManager().disconnectFromMySQL();
        System.exit(0);
    }

    private static void silenceConsole(String value) {
        try {
            if (value.equalsIgnoreCase("true")) {
                ((Discord4J.Discord4JLogger) Discord4J.LOGGER).setLevel(Discord4J.Discord4JLogger.Level.INFO);
                System.out.println("Logger set to INFO only! Use 'silence false' to undo this action!");
            } else if (value.equalsIgnoreCase("false")) {
                ((Discord4J.Discord4JLogger) Discord4J.LOGGER).setLevel(Discord4J.Discord4JLogger.Level.INFO);
                System.out.println("Logger set to DEBUG! Use 'silence true' to undo this action!");
            } else {
                System.out.println("Values must be true or false!");
            }
        } catch (Exception e) {
            System.out.println("Failed to silence logger!");
        }
    }
}