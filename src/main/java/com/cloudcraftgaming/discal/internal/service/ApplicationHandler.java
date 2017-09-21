package com.cloudcraftgaming.discal.internal.service;

import com.cloudcraftgaming.discal.Main;
import com.cloudcraftgaming.discal.database.DatabaseManager;
import com.cloudcraftgaming.discal.module.announcement.Announcer;
import com.cloudcraftgaming.discal.utils.ExceptionHandler;
import sx.blah.discord.util.DiscordException;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.List;

/**
 * Created by Nova Fox on 6/21/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class ApplicationHandler {

	/**
	 * Sun property pointing the main class and its arguments.
	 * Might not be defined on non Hotspot VM implementations.
	 */
	private static final String SUN_JAVA_COMMAND = "sun.java.command";

	/**
	 * Restart the current Java application
	 *
	 * Code provided by: https://dzone.com/articles/programmatically-restart-java
	 *
	 * @param runBeforeRestart some custom code to be run before restarting
	 */
	public static void restartApplication(Runnable runBeforeRestart) {
		try {
			// java binary
			String java = System.getProperty("java.home") + "/bin/java";
			// vm arguments
			List<String> vmArguments = ManagementFactory.getRuntimeMXBean().getInputArguments();
			StringBuilder vmArgsOneLine = new StringBuilder();
			for (String arg : vmArguments) {
				// if it's the agent argument : we ignore it otherwise the
				// address of the old application and the new one will be in conflict
				if (!arg.contains("-agentlib")) {
					vmArgsOneLine.append(arg);
					vmArgsOneLine.append(" ");
				}
			}
			// init the command to execute, add the vm args
			final StringBuffer cmd = new StringBuffer("\"" + java + "\" " + vmArgsOneLine);

			// program main and program arguments
			String[] mainCommand = System.getProperty(SUN_JAVA_COMMAND).split(" ");
			// program main is a jar
			if (mainCommand[0].endsWith(".jar")) {
				// if it's a jar, add -jar mainJar
				cmd.append("-jar ").append(new File(mainCommand[0]).getPath());
			} else {
				// else it's a .class, add the classpath and mainClass
				cmd.append("-cp \"").append(System.getProperty("java.class.path")).append("\" ").append(mainCommand[0]);
			}
			// finally add program arguments
			for (int i = 1; i < mainCommand.length; i++) {
				cmd.append(" ");
				cmd.append(mainCommand[i]);
			}
			// execute the command in a shutdown hook, to be sure that all the
			// resources have been disposed before restarting the application
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    Runtime.getRuntime().exec(cmd.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }));
			// execute some custom code before restarting
			if (runBeforeRestart != null) {
				runBeforeRestart.run();
			}

			//MY CODE: Gracefully exit processes:
			System.out.println("Restarting Discord bot!");
			try {
				Main.client.logout();
			} catch (DiscordException e) {
				//No need to print, exiting anyway.
			}
			Announcer.getAnnouncer().shutdown();
			TimeManager.getManager().shutdown();
			AnnouncementQueueManager.getManager().shutdown();
			DatabaseManager.getManager().disconnectFromMySQL();

			// exit
			System.exit(0);
		} catch (Exception e) {
			// something went wrong
			ExceptionHandler.sendException(null, "Failed to restart bot!", e, ApplicationHandler.class);
		}
	}

	/**
	 * Exits the application gracefully and shuts down the bot gracefully.
	 */
	public static void exitApplication() {
		System.out.println("Shutting down Discord bot!");
		try {
			Main.client.logout();
		} catch (DiscordException e) {
			//No need to print, exiting anyway.
		}
		Announcer.getAnnouncer().shutdown();
		TimeManager.getManager().shutdown();
		AnnouncementQueueManager.getManager().shutdown();
		DatabaseManager.getManager().disconnectFromMySQL();
		System.exit(0);
	}
}