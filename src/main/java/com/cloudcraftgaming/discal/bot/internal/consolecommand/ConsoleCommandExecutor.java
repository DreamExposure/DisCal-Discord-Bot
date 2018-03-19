package com.cloudcraftgaming.discal.bot.internal.consolecommand;

import com.cloudcraftgaming.discal.Main;
import com.cloudcraftgaming.discal.api.object.BotSettings;
import com.cloudcraftgaming.discal.bot.internal.service.ApplicationHandler;

/**
 * Created by Nova Fox on 1/2/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class ConsoleCommandExecutor {
	/**
	 * Initiates the listener for commands via the DisCal console window.
	 */
	public static void init() {
		if (BotSettings.ACCEPT_CLI.get().equalsIgnoreCase("true")) {
			while (true) {
				System.out.println("Enter a command below: ");
				String input = System.console().readLine();
				Boolean cmdValid = false;
				if (input != null && !input.isEmpty()) {

					//Important commands first.
					if (input.equalsIgnoreCase("exit")) {
						ApplicationHandler.exitApplication();
						return;
					}
					if (input.equalsIgnoreCase("restart")) {
						ApplicationHandler.restartApplication(null);
						return;
					}
					if (input.equalsIgnoreCase("?")) {
						cmdValid = true;
						System.out.println("Valid console commands: ");
						System.out.println("exit");
						System.out.println("restart");
						System.out.println("serverCount");
						System.out.println("silence true/false");
						System.out.println();
					}
					if (input.startsWith("serverCount")) {
						cmdValid = true;
						System.out.println("Server count: " + Main.client.getGuilds().size());
					}

					if (!cmdValid) {
						System.out.println("Command not found! Use ? to list all commands.");
						System.out.println();
					}
				}
			}
		}
	}
}