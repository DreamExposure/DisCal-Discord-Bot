package org.dreamexposure.discal.core.logger;

import org.dreamexposure.discal.core.logger.interfaces.Logger;
import org.dreamexposure.discal.core.logger.loggers.DiscordLogger;
import org.dreamexposure.discal.core.logger.loggers.FileLogger;
import org.dreamexposure.discal.core.logger.object.LogObject;
import org.dreamexposure.discal.core.logger.threads.LoggerThread;
import org.dreamexposure.discal.core.object.BotSettings;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import club.minnced.discord.webhook.WebhookClient;

@SuppressWarnings("Duplicates")
public class LogFeed {
    private final static String exceptionsFile;
    private final static String debugFile;

    private final static BlockingQueue<LogObject> fileQueue;
    private final static BlockingQueue<LogObject> discordQueue;

    private final static boolean useWebhooks;

    static {
        fileQueue = new LinkedBlockingQueue<>();
        discordQueue = new LinkedBlockingQueue<>();

        useWebhooks = BotSettings.USE_WEBHOOKS.get().equalsIgnoreCase("true");

        Thread fileLogThread;
        Thread discordLogThread = null;

        //Create webhook clients.
        if (useWebhooks) {
            WebhookClient debug = WebhookClient.withUrl(BotSettings.DEBUG_WEBHOOK.get());
            WebhookClient exception = WebhookClient.withUrl(BotSettings.ERROR_WEBHOOK.get());
            WebhookClient status = WebhookClient.withUrl(BotSettings.STATUS_WEBHOOK.get());

            Logger discordLogger = new DiscordLogger(debug, exception, status);
            discordLogThread = new Thread(new LoggerThread(discordQueue, discordLogger));
        }

        //Create files for file logger.
        String timestamp = new SimpleDateFormat("dd-MM-yyyy-hh.mm.ss")
                .format(System.currentTimeMillis());

        exceptionsFile = BotSettings.LOG_FOLDER.get() + "/" + timestamp + "-exceptions.log";
        debugFile = BotSettings.LOG_FOLDER.get() + "/" + timestamp + "-debug.log";

        try {
            //Write to files to init
            PrintWriter exceptions = new PrintWriter(exceptionsFile, "UTF-8");
            exceptions.println("INIT --- " + timestamp + " ---");
            exceptions.close();

            PrintWriter debug = new PrintWriter(debugFile, "UTF-8");
            debug.println("INIT --- " + timestamp + " ---");
            debug.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(4);
        }

        //Create file logger and thread
        Logger fileLogger = new FileLogger(exceptionsFile, debugFile);
        fileLogThread = new Thread(new LoggerThread(fileQueue, fileLogger));

        //Set threads as daemons
        fileLogThread.setDaemon(true);
        if (discordLogThread != null)
            discordLogThread.setDaemon(true);

        //Start logging threads
        fileLogThread.start();
        if (discordLogThread != null)
            discordLogThread.start();
    }

    public static void log(LogObject log) {
        fileQueue.offer(log);
        if (useWebhooks)
            discordQueue.offer(log);
    }

    private LogFeed() {
    } //Prevent initialization
}