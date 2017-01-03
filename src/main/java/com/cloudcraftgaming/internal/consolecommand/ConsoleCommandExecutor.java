package com.cloudcraftgaming.internal.consolecommand;

import com.cloudcraftgaming.Main;
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
                    System.out.println();
                }
                if (input.startsWith("status")) {
                    cmdValid = true;
                    String status = input.replaceAll("status ", "");
                    Main.client.changeStatus(Status.game(status));

                    System.out.println("Status changed! Check discord.");
                    System.out.println();
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
        Main.disconnectFromMySQL();
        System.exit(0);
    }
}