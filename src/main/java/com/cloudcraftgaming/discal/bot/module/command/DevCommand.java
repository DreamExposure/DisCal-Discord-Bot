package com.cloudcraftgaming.discal.bot.module.command;

import com.cloudcraftgaming.discal.api.DisCalAPI;
import com.cloudcraftgaming.discal.api.crypto.KeyGenerator;
import com.cloudcraftgaming.discal.api.database.DatabaseManager;
import com.cloudcraftgaming.discal.api.message.MessageManager;
import com.cloudcraftgaming.discal.api.object.GuildSettings;
import com.cloudcraftgaming.discal.api.object.calendar.CalendarData;
import com.cloudcraftgaming.discal.api.object.command.CommandInfo;
import com.cloudcraftgaming.discal.api.object.web.UserAPIAccount;
import com.cloudcraftgaming.discal.api.utils.CalendarUtils;
import com.cloudcraftgaming.discal.bot.internal.service.ApplicationHandler;
import com.cloudcraftgaming.discal.logger.Logger;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.IShard;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.RequestBuffer;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.ArrayList;

/**
 * Created by Nova Fox on 4/4/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class DevCommand implements ICommand {

	private ScriptEngine factory = new ScriptEngineManager().getEngineByName("nashorn");

	/**
	 * Gets the command this Object is responsible for.
	 *
	 * @return The command this Object is responsible for.
	 */
	@Override
	public String getCommand() {
		return "dev";
	}

	/**
	 * Gets the short aliases of the command this object is responsible for.
	 * </br>
	 * This will return an empty ArrayList if none are present
	 *
	 * @return The aliases of the command.
	 */
	@Override
	public ArrayList<String> getAliases() {
		return new ArrayList<>();
	}

	/**
	 * Gets the info on the command (not sub command) to be used in help menus.
	 *
	 * @return The command info.
	 */
	@Override
	public CommandInfo getCommandInfo() {
		CommandInfo ci = new CommandInfo("dev");
		ci.setDescription("Used for developer commands. Only able to be used by registered developers");
		ci.setExample("!dev <function> (value)");
		ci.getSubCommands().put("patron", "Sets a guild as a patron.");
		ci.getSubCommands().put("dev", "Sets a guild as a test/dev guild.");
		ci.getSubCommands().put("maxcal", "Sets the max amount of calendars a guild may have.");
		ci.getSubCommands().put("leave", "Leaves the specified guild.");
		ci.getSubCommands().put("listguilds", "Lists ALL guilds.");
		ci.getSubCommands().put("reloadlangs", "Reloads the lang files for changes.");
		ci.getSubCommands().put("cleanupdatabase", "Cleans up the entire database.");
		ci.getSubCommands().put("restart", "Completely restarts the bot application.");
		ci.getSubCommands().put("reload", "Logs out and then logs in every shard.");
		ci.getSubCommands().put("shutdown", "Shuts down the bot application.");
		ci.getSubCommands().put("eval", "Evaluates the given code.");
		ci.getSubCommands().put("testShards", "Tests to make sure all shards respond.");
		ci.getSubCommands().put("api-register", "Register new API key");
		ci.getSubCommands().put("api-block", "Block API usage by key");

		return ci;
	}

	/**
	 * Issues the command this Object is responsible for.
	 *
	 * @param args  The command arguments.
	 * @param event The event received.
	 * @return <code>true</code> if successful, else <code>false</code>.
	 */
	@Override
	public boolean issueCommand(String[] args, MessageReceivedEvent event, GuildSettings settings) {
		if (event.getAuthor().getLongID() == DisCalAPI.getAPI().novaId || event.getAuthor().getLongID() == DisCalAPI.getAPI().xaanitId || event.getAuthor().getLongID() == DisCalAPI.getAPI().calId || event.getAuthor().getLongID() == DisCalAPI.getAPI().dreamId) {
			if (args.length < 1) {
				MessageManager.sendMessage("Please specify the function you would like to execute. To view valid functions use `!help dev`", event);
			} else {
				switch (args[0].toLowerCase()) {
					case "patron":
						modulePatron(args, event);
						break;
					case "dev":
						moduleDevGuild(args, event);
						break;
					case "maxcal":
						moduleMaxCalendars(args, event);
						break;
					case "leave":
						moduleLeaveGuild(args, event);
						break;
					case "listguilds":
						moduleListGuilds(event);
						break;
					case "reloadlangs":
						moduleReloadLangs(event);
						break;
					case "cleanupdatabase":
						moduleCleanupDatabase(event);
						break;
					case "restart":
						moduleRestart(event);
						break;
					case "reload":
						moduleReload(event);
						break;
					case "shutdown":
						moduleShutdown(event);
						break;
					case "eval":
						moduleEval(event);
						break;
					case "testshards":
						moduleTestShards(event);
						break;
					case "api-register":
						registerApiKey(args, event);
						break;
					case "api-block":
						blockAPIKey(args, event);
						break;
					default:
						MessageManager.sendMessage("Invalid sub command! Use `!help dev` to view valid sub commands!", event);
						break;
				}
			}
		} else {
			MessageManager.sendMessage("You are not a registered DisCal developer! If this is a mistake please contact Nova!", event);
		}
		return false;
	}

	private void modulePatron(String[] args, MessageReceivedEvent event) {
		if (args.length == 2) {
			long guildId = Long.valueOf(args[1]);
			if (DisCalAPI.getAPI().getClient().getGuildByID(guildId) != null) {
				GuildSettings settings = DatabaseManager.getManager().getSettings(guildId);
				settings.setPatronGuild(!settings.isPatronGuild());

				boolean isPatron = settings.isPatronGuild();

				DatabaseManager.getManager().updateSettings(settings);

				MessageManager.sendMessage("Guild with ID: `" + guildId + "` is patron set to: `" + isPatron + "`", event);
			} else {
				MessageManager.sendMessage("Guild not found or is not connected to DisCal!", event);
			}
		} else {
			MessageManager.sendMessage("Please specify the ID of the guild to set as a patron guild with `!dev patron <ID>`", event);
		}
	}

	@SuppressWarnings("all")
	private void moduleEval(MessageReceivedEvent event) {
		IGuild guild = event.getGuild();
		IUser user = event.getAuthor();
		IMessage message = event.getMessage();
		IDiscordClient client = event.getClient();
		IChannel channel = event.getChannel();
		String input = message.getContent().substring(message.getContent().indexOf("eval") + 5).replaceAll("`", "");
		Object o = null;
		factory.put("guild", guild);
		factory.put("channel", channel);
		factory.put("user", user);
		factory.put("message", message);
		factory.put("command", this);
		factory.put("client", client);
		factory.put("builder", new EmbedBuilder());
		factory.put("cUser", client.getOurUser());

		try {
			o = factory.eval(input);
		} catch (Exception ex) {
			EmbedBuilder em = new EmbedBuilder();
			em.withAuthorIcon(guild.getIconURL());
			em.withAuthorName("Error");
			em.withDesc(ex.getMessage());
			em.withFooterText("Eval failed");
			em.withColor(56, 138, 237);
			MessageManager.sendMessage(em.build(), channel);
			return;
		}

		EmbedBuilder em = new EmbedBuilder();
		em.withAuthorIcon(guild.getIconURL());
		em.withAuthorName("Success!");
		em.withColor(56, 138, 237);
		em.withTitle("Evaluation output.");
		em.withDesc(o == null ? "No output, object is null" : o.toString());
		em.appendField("Input", "```java\n" + input + "\n```", false);
		em.withFooterText("Eval successful!");
		MessageManager.sendMessage(em.build(), channel);
	}

	private void moduleDevGuild(String[] args, MessageReceivedEvent event) {
		if (args.length == 2) {
			long guildId = Long.valueOf(args[1]);
			if (DisCalAPI.getAPI().getClient().getGuildByID(guildId) != null) {
				GuildSettings settings = DatabaseManager.getManager().getSettings(guildId);
				settings.setDevGuild(!settings.isDevGuild());

				boolean isPatron = settings.isDevGuild();

				DatabaseManager.getManager().updateSettings(settings);

				MessageManager.sendMessage("Guild with ID: `" + guildId + "` is dev guild set to: `" + isPatron + "`", event);
			} else {
				MessageManager.sendMessage("Guild not found or is not connected to DisCal!", event);
			}
		} else {
			MessageManager.sendMessage("Please specify the ID of the guild to set as a dev guild with `!dev dev <ID>`", event);
		}
	}

	private void moduleMaxCalendars(String[] args, MessageReceivedEvent event) {
		if (args.length == 3) {
			long guildId = Long.valueOf(args[1]);
			try {
				int mc = Integer.valueOf(args[2]);
				mc = Math.abs(mc);
				if (DisCalAPI.getAPI().getClient().getGuildByID(guildId) != null) {
					GuildSettings settings = DatabaseManager.getManager().getSettings(guildId);
					settings.setMaxCalendars(mc);

					DatabaseManager.getManager().updateSettings(settings);

					MessageManager.sendMessage("Guild with ID: `" + guildId + "` max calendar count set to: `" + mc + "`", event);
				} else {
					MessageManager.sendMessage("Guild not found or is not connected to DisCal!", event);
				}
			} catch (NumberFormatException e) {
				MessageManager.sendMessage("Max Calendar amount must be a valid Integer!", event);
			}
		} else {
			MessageManager.sendMessage("Please specify the ID of the guild and calendar amount with `!dev maxcal <ID> <amount>`", event);
		}
	}

	private void moduleLeaveGuild(String[] args, MessageReceivedEvent event) {
		if (args.length == 2) {
			if (DisCalAPI.getAPI().getClient().getGuildByID(Long.valueOf(args[1])) != null) {
				RequestBuffer.request(() -> {
					try {
						DisCalAPI.getAPI().getClient().getGuildByID(Long.valueOf(args[1])).leave();
					} catch (DiscordException e) {
						Logger.getLogger().exception(event.getMessage().getAuthor(), "Failed to leave guild", e, this.getClass(), true);
					}
				});
				MessageManager.sendMessage("Left Guild!", event);
			} else {
				MessageManager.sendMessage("Guild not found!", event);
			}
		} else {
			MessageManager.sendMessage("Please specify the ID of the guild to leave with `!dev leave <ID>`", event);
		}
	}

	private void moduleListGuilds(MessageReceivedEvent event) {

		MessageManager.sendMessage("Sending a list of all Guilds! This may take awhile...", event);
		StringBuilder msg = new StringBuilder();

		for (IGuild g : DisCalAPI.getAPI().getClient().getGuilds()) {
			msg.append(MessageManager.lineBreak).append(g.getName()).append(" | ").append(g.getLongID()).append(" | Members: ").append(g.getTotalMemberCount()).append(" | Bots: ").append(botPercent(g)).append("%");

			if (msg.length() >= 1500) {
				MessageManager.sendMessage(msg.toString(), event);
				msg = new StringBuilder();
			}
		}
		MessageManager.sendMessage(msg.toString(), event);
		MessageManager.sendMessage("All Guilds listed!", event);
	}

	private void moduleReloadLangs(MessageReceivedEvent event) {

		MessageManager.sendMessage("Reloading lang files!", event);

		MessageManager.reloadLangs();

		MessageManager.sendMessage("All lang files reloaded!", event);
	}

	private void moduleCleanupDatabase(MessageReceivedEvent event) {
		MessageManager.sendMessage("Cleaning up database! This may take some time....", event);

		MessageManager.sendMessage("Cleaning out calendars....", event);
		for (CalendarData cd : DatabaseManager.getManager().getAllCalendars()) {
			if (DisCalAPI.getAPI().getClient().getGuildByID(cd.getGuildId()) == null) {
				//Guild not connected... delete calendar...
				CalendarUtils.deleteCalendar(cd, DatabaseManager.getManager().getSettings(cd.getGuildId()));
			}
		}
		MessageManager.sendMessage("Calendars cleaned up!", event);

		MessageManager.sendMessage("Cleaned up database!", event);
		//MessageManager.sendMessage("Disabled because I am a dumb", event);
	}

	private void moduleRestart(MessageReceivedEvent event) {
		MessageManager.sendMessage("Restarting DisCal! This may take a moment!", event);

		ApplicationHandler.restartApplication(null);
	}

	private void moduleReload(MessageReceivedEvent event) {
		IMessage msg = MessageManager.sendMessage("Reloading DisCal! This may take a moment!", event);

		for (IShard s : msg.getClient().getShards()) {
			s.logout();
			s.login();
		}
		MessageManager.sendMessage("DisCal successfully reloaded!", event);
	}

	private void moduleShutdown(MessageReceivedEvent event) {
		MessageManager.sendMessage("Shutting down DisCal! This may take a moment!", event);

		ApplicationHandler.exitApplication();
	}

	private void moduleTestShards(MessageReceivedEvent event) {
		MessageManager.sendMessage("Testing shard responses...", event);

		StringBuilder r = new StringBuilder();
		for (IShard s : DisCalAPI.getAPI().getClient().getShards()) {
			r.append(s.getInfo()[0]).append(": ").append(s.isReady()).append("\n");
		}

		MessageManager.sendMessage(r.toString(), event);
	}

	private void registerApiKey(String[] args, MessageReceivedEvent event) {
		if (args.length == 2) {
			MessageManager.sendMessage("Registering new API key...", event);

			String userId = args[1];

			UserAPIAccount account = new UserAPIAccount();
			account.setUserId(userId);
			account.setAPIKey(KeyGenerator.csRandomAlphaNumericString(64));
			account.setTimeIssued(System.currentTimeMillis());
			account.setBlocked(false);
			account.setUses(0);

			if (DatabaseManager.getManager().updateAPIAccount(account)) {
				MessageManager.sendMessage("Check your DMs for the new API Key!", event);
				MessageManager.sendDirectMessage(account.getAPIKey(), event.getAuthor());
			} else {
				MessageManager.sendMessage("Error occurred! Could not register new API key!", event);
			}
		} else {
			MessageManager.sendMessage("Please specify the USER ID linked to the key!", event);
		}
	}

	private void blockAPIKey(String[] args, MessageReceivedEvent event) {
		if (args.length == 2) {
			MessageManager.sendMessage("Blocking API key...", event);

			String key = args[1];

			UserAPIAccount account = DatabaseManager.getManager().getAPIAccount(key);
			account.setBlocked(true);

			if (DatabaseManager.getManager().updateAPIAccount(account))
				MessageManager.sendMessage("Successfully blocked API key!", event);
			else
				MessageManager.sendMessage("Error occurred! Could not block API key!", event);
		} else {
			MessageManager.sendMessage("Please specify the API KEY!", event);
		}
	}

	private long botPercent(IGuild g) {
		return g.getUsers().stream().filter(IUser::isBot).count() * 100 / g.getTotalMemberCount();
	}
}