package com.cloudcraftgaming;

import com.cloudcraftgaming.database.DatabaseManager;
import com.cloudcraftgaming.database.MySQL;
import com.cloudcraftgaming.eventlisteners.ReadyEventListener;
import com.cloudcraftgaming.internal.calendar.CalendarAuth;
import com.cloudcraftgaming.internal.consolecommand.ConsoleCommandExecutor;
import com.cloudcraftgaming.internal.email.EmailSender;
import com.cloudcraftgaming.internal.file.ReadFile;
import com.cloudcraftgaming.module.command.*;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;

import java.io.IOException;

/**
 * Created by Nova Fox on 1/2/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
@SuppressWarnings("SameParameterValue")
public class Main {
    public static String version = "Beta 1.1.2";
    public static IDiscordClient client;

    public static void main(String[] args) {
        if (args.length < 3) // Needs a bot token provided
            throw new IllegalArgumentException("The Bot Token, MySQL file, or Email file has not be specified!");

        //Setup email debugging
        EmailSender.getSender().init(args[2]);

        client = createClient(args[0], true);
        if (client == null)
            throw new NullPointerException("Failed to log in! Client cannot be null!");

        //Connect to MySQL
        MySQL mySQL = ReadFile.readDatabaseSettings(args[1]);
        DatabaseManager.getManager().connectToMySQL(mySQL);
        DatabaseManager.getManager().createTables();

        //Connect to Google Calendar
        try {
            CalendarAuth.init(args);
        } catch (IOException e) {
            e.printStackTrace();
            EmailSender.getSender().sendExceptionEmail(e);
        }

        //Register events
        EventDispatcher dispatcher = client.getDispatcher();
        dispatcher.registerListener(new ReadyEventListener());

        //Register modules
        CommandExecutor executor = CommandExecutor.getExecutor().enable(client);
        executor.registerCommand(new HelpCommand());
        executor.registerCommand(new DisCalCommand());
        executor.registerCommand(new CalendarCommand());
        executor.registerCommand(new LinkCalendarCommand());
        executor.registerCommand(new EventListCommand());
        executor.registerCommand(new EventCommand());

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
            EmailSender.getSender().sendExceptionEmail(e);
        }
        return null;
    }

    public static IUser getSelfUser() {
        return client.getOurUser();
    }
}