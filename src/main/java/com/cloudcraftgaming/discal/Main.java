package com.cloudcraftgaming.discal;

import com.cloudcraftgaming.discal.api.DisCalAPI;
import com.cloudcraftgaming.discal.api.database.DatabaseManager;
import com.cloudcraftgaming.discal.api.file.ReadFile;
import com.cloudcraftgaming.discal.api.message.MessageManager;
import com.cloudcraftgaming.discal.api.network.google.Authorization;
import com.cloudcraftgaming.discal.api.object.BotSettings;
import com.cloudcraftgaming.discal.eventlisteners.ReadyEventListener;
import com.cloudcraftgaming.discal.internal.consolecommand.ConsoleCommandExecutor;
import com.cloudcraftgaming.discal.internal.network.discordpw.UpdateListData;
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
public class Main {
	public static String version = "1.1.0";
    public static IDiscordClient client;
    public static BotSettings botSettings;

    public static void main(String[] args) {
        if (args.length < 1) // Needs a bot token provided
            throw new IllegalArgumentException("BotSettings file has not been specified!!");

        //Get bot settings
        botSettings = ReadFile.readBotSettings(args[0]);

        client = createClient(botSettings.getBotToken());
        if (client == null)
            throw new NullPointerException("Failed to log in! Client cannot be null!");

		DisCalAPI.init(client, botSettings);

        UpdateListData.init(botSettings);

        Authorization.getAuth().init(botSettings);

        //Connect to MySQL
        DatabaseManager.getManager().connectToMySQL(botSettings);
        DatabaseManager.getManager().createTables();

        //Register events
        EventDispatcher dispatcher = client.getDispatcher();
        dispatcher.registerListener(new ReadyEventListener());

        //Register modules
        CommandExecutor executor = CommandExecutor.getExecutor().enable();
        executor.registerCommand(new HelpCommand());
        executor.registerCommand(new DisCalCommand());
        executor.registerCommand(new CalendarCommand());
        executor.registerCommand(new AddCalendarCommand());
        executor.registerCommand(new LinkCalendarCommand());
        executor.registerCommand(new TimeCommand());
        executor.registerCommand(new EventListCommand());
        executor.registerCommand(new EventCommand());
        executor.registerCommand(new RsvpCommand());
        executor.registerCommand(new AnnouncementCommand());
        executor.registerCommand(new DevCommand());

        //Accept commands
        ConsoleCommandExecutor.init();

		//Load language files.
		MessageManager.loadLangs();
	}

    /**
     * Creates the DisCal bot client.
     *
     * @param token The Bot Token.
     * @return The client if successful, otherwise <code>null</code>.
     */
    private static IDiscordClient createClient(String token) {
        ClientBuilder clientBuilder = new ClientBuilder(); // Creates the ClientBuilder instance
        clientBuilder.withToken(token).withRecommendedShardCount(); // Adds the login info to the builder
        try {
			return clientBuilder.login();
        } catch (DiscordException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Gets The {@link IUser} Object of DisCal.
     *
     * @return The {@link IUser} Object of DisCal.
     */
    public static IUser getSelfUser() {
        return client.getOurUser();
    }
}