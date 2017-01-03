package com.cloudcraftgaming;

import com.cloudcraftgaming.database.DatabaseInfo;
import com.cloudcraftgaming.database.MySQL;
import com.cloudcraftgaming.eventlisteners.ReadyEventListener;
import com.cloudcraftgaming.internal.calendar.CalendarAuth;
import com.cloudcraftgaming.internal.consolecommand.ConsoleCommandExecutor;
import com.cloudcraftgaming.internal.file.ReadFile;
import com.cloudcraftgaming.module.command.CommandExecutor;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.util.DiscordException;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by Nova Fox on 1/2/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
@SuppressWarnings("SameParameterValue")
public class Main {
    public static IDiscordClient client;
    public static DatabaseInfo databaseInfo;

    public static void main(String[] args) {
        if (args.length < 2) // Needs a bot token provided
            throw new IllegalArgumentException("The Bot Token & MySQL file has not be specified!");

        client = createClient(args[0], true);
        if (client == null)
            throw new NullPointerException("Failed to log in! Client cannot be null!");

        //Connect to MySQL
        MySQL mySQL = ReadFile.readDatabaseSettings(args[1]);
        connectToMySQL(mySQL);

        //Connect to Google Calendar
        try {
            CalendarAuth.init(args);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Register events
        EventDispatcher dispatcher = client.getDispatcher();
        dispatcher.registerListener(new ReadyEventListener());

        //Register modules
        new CommandExecutor().enable(client);

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

    private static void connectToMySQL(MySQL mySQL) {
        try {
            Connection mySQLConnection = mySQL.openConnection();
            databaseInfo = new DatabaseInfo(mySQL, mySQLConnection, mySQL.getPrefix());
            System.out.println("Connected to MySQL database!");
        } catch (Exception e) {
            System.out.println("Failed to connect to MySQL database! Is it properly configured?");
            e.printStackTrace();
        }
    }

    public static void disconnectFromMySQL() {
        if (databaseInfo != null) {
            try {
                databaseInfo.getMySQL().closeConnection();
                System.out.println("Successfully disconnected from MySQL Database!");
            } catch (SQLException e) {
                System.out.println("MySQL Connection may not have closed properly! Data may be invalidated!");
            }
        }
    }
}