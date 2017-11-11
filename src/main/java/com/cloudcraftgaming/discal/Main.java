package com.cloudcraftgaming.discal;

import com.cloudcraftgaming.discal.api.database.DatabaseManager;
import com.cloudcraftgaming.discal.api.file.ReadFile;
import com.cloudcraftgaming.discal.api.message.MessageManager;
import com.cloudcraftgaming.discal.api.network.google.Authorization;
import com.cloudcraftgaming.discal.api.object.BotSettings;
import com.cloudcraftgaming.discal.bot.internal.consolecommand.ConsoleCommandExecutor;
import com.cloudcraftgaming.discal.bot.internal.network.discordpw.UpdateListData;
import com.cloudcraftgaming.discal.bot.listeners.ReadyEventListener;
import com.cloudcraftgaming.discal.bot.module.command.*;
import com.cloudcraftgaming.discal.web.endpoints.v1.AnnouncementEndpoint;
import com.cloudcraftgaming.discal.web.endpoints.v1.CalendarEndpoint;
import com.cloudcraftgaming.discal.web.endpoints.v1.GuildEndpoint;
import com.cloudcraftgaming.discal.web.endpoints.v1.RsvpEndpoint;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;

import static spark.Spark.*;

/**
 * Created by Nova Fox on 1/2/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
@SuppressWarnings("ThrowableNotThrown")
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

		//Register Spark endpoints...
		before("/api/*", (request, response) -> {
			if (!request.requestMethod().equalsIgnoreCase("POST")) {
				System.out.println("Denied '" + request.requestMethod() + "' access from: " + request.ip());
				halt(405, "Method not allowed");
			}
			//Check authorization
			if (request.headers().contains("Authorization") && !request.headers("Authorization").equals("API_KEY")) {
				halt(401, "Unauthorized");
			}
			if (!request.contentType().equalsIgnoreCase("application/json")) {
				halt(400, "Bad Request");
			}
		});

		path("/api/v1/discal", () -> {
			before("/*", (q, a) -> System.out.println("Received API call from: " + q.ip() + "; Host:" + q.host()));
			path("/guild", () -> {
				path("/settings", () -> {
					post("/get", GuildEndpoint::getSettings);
					post("/update", GuildEndpoint::updateSettings);
				});
			});
			path("/announcement", () -> {
				post("/get", AnnouncementEndpoint::getAnnouncement);
				post("/create", AnnouncementEndpoint::createAnnouncement);
				post("/update", AnnouncementEndpoint::updateAnnouncement);
				post("/delete", AnnouncementEndpoint::deleteAnnouncement);
				post("/list", AnnouncementEndpoint::listAnnouncements);
			});
			path("/calendar", () -> {
				post("/get", CalendarEndpoint::getCalendar);
				post("/list", CalendarEndpoint::listCalendars);
			});
			path("/rsvp", () -> {
				post("/get", RsvpEndpoint::getRsvp);
				post("/update", RsvpEndpoint::updateRsvp);
			});
		});
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