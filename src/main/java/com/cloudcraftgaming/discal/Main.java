package com.cloudcraftgaming.discal;

import com.cloudcraftgaming.discal.database.DatabaseManager;
import com.cloudcraftgaming.discal.database.MySQL;
import com.cloudcraftgaming.discal.eventlisteners.ReadyEventListener;
import com.cloudcraftgaming.discal.internal.consolecommand.ConsoleCommandExecutor;
import com.cloudcraftgaming.discal.internal.email.EmailSender;
import com.cloudcraftgaming.discal.internal.file.ReadFile;
import com.cloudcraftgaming.discal.internal.network.discordpw.UpdateListData;
import com.cloudcraftgaming.discal.module.announcement.Announcer;
import com.cloudcraftgaming.discal.module.command.*;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;

/**
 * Created by Nova Fox on 1/2/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
@SuppressWarnings("SameParameterValue")
public class Main {
    public static String version = "Beta 2.2.0";
    public static IDiscordClient client;

    public static void main(String[] args) {
        if (args.length < 3) // Needs a bot token provided
            throw new IllegalArgumentException("The Bot Token, MySQL file, or Email file has not be specified!");

        //Setup email debugging
        EmailSender.getSender().init(args[2]);

        client = createClient(args[0], true);
        if (client == null)
            throw new NullPointerException("Failed to log in! Client cannot be null!");

        if (args.length > 3)
            UpdateListData.init(args[3]);

        //Connect to MySQL
        MySQL mySQL = ReadFile.readDatabaseSettings(args[1]);
        DatabaseManager.getManager().connectToMySQL(mySQL);
        DatabaseManager.getManager().createTables();

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
        executor.registerCommand(new AnnouncementCommand());

        //Init a few more modules
        Announcer.getAnnouncer().init();

        //Accept commands
        ConsoleCommandExecutor.init();
    }

    /**
     * Creates the DisCal bot client.
     * @param token The Bot Token.
     * @param login Whether or not to login.
     * @return The client if successful, otherwise <code>null</code>.
     */
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
            EmailSender.getSender().sendExceptionEmail(e, Main.class);
        }
        return null;
    }

    /**
     * Gets The {@link IUser} Object of DisCal.
     * @return The {@link IUser} Object of DisCal.
     */
    public static IUser getSelfUser() {
        return client.getOurUser();
    }
}