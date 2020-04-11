package org.dreamexposure.discal.client.module.command;

import org.dreamexposure.discal.client.DisCalClient;
import org.dreamexposure.discal.client.message.MessageManager;
import org.dreamexposure.discal.core.crypto.KeyGenerator;
import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.enums.network.DisCalRealm;
import org.dreamexposure.discal.core.logger.LogFeed;
import org.dreamexposure.discal.core.logger.object.LogObject;
import org.dreamexposure.discal.core.object.BotSettings;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.command.CommandInfo;
import org.dreamexposure.discal.core.object.web.UserAPIAccount;
import org.dreamexposure.discal.core.utils.GlobalConst;
import org.dreamexposure.discal.core.utils.GuildUtils;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import discord4j.core.DiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.util.Snowflake;
import discord4j.core.spec.EmbedCreateSpec;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Nova Fox on 4/4/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
@SuppressWarnings({"OptionalGetWithoutIsPresent", "Duplicates"})
public class DevCommand implements ICommand {

	private final ScriptEngine factory = new ScriptEngineManager().getEngineByName("nashorn");

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
		CommandInfo ci = new CommandInfo(
				"dev",
				"Used for developer commands. Only able to be used by registered developers",
				"!dev <function> (value)"
		);

		ci.getSubCommands().put("reloadLangs", "Reloads the lang files across the network.");
		ci.getSubCommands().put("patron", "Sets a guild as a patron.");
		ci.getSubCommands().put("dev", "Sets a guild as a test/dev guild.");
		ci.getSubCommands().put("maxcal", "Sets the max amount of calendars a guild may have.");
		ci.getSubCommands().put("leave", "Leaves the specified guild.");
		ci.getSubCommands().put("eval", "Evaluates the given code.");
		ci.getSubCommands().put("api-register", "Register new API key");
		ci.getSubCommands().put("api-block", "Block API usage by key");
		ci.getSubCommands().put("settings", "Checks the settings of the specified Guild.");

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
	public boolean issueCommand(String[] args, MessageCreateEvent event, GuildSettings settings) {
		if (event.getMember().get().getId().equals(GlobalConst.novaId) || event.getMember().get().getId().equals(GlobalConst.xaanitId) || event.getMember().get().getId().equals(GlobalConst.calId) || event.getMember().get().getId().equals(GlobalConst.dreamId)) {
			if (args.length < 1) {
				MessageManager.sendMessageAsync("Please specify the function you would like to execute. To view valid functions use `!help dev`", event);
			} else {
				switch (args[0].toLowerCase()) {
					case "reloadlangs":
						moduleReloadLangs(event);
						break;
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
					case "eval":
						moduleEval(event);
						break;
					case "api-register":
						registerApiKey(args, event);
						break;
					case "api-block":
						blockAPIKey(args, event);
						break;
					case "settings":
						moduleCheckSettings(args, event);
						break;
					default:
						MessageManager.sendMessageAsync("Invalid sub command! Use `!help dev` to view valid sub commands!", event);
						break;
				}
			}
		} else {
			MessageManager.sendMessageAsync("You are not a registered DisCal developer! If this is a mistake please contact Nova!", event);
		}
		return false;
	}

	private void modulePatron(String[] args, MessageCreateEvent event) {
		if (args.length == 2) {
			try {
				Long.valueOf(args[1]);
			} catch (NumberFormatException ignore) {
				MessageManager.sendMessageAsync("Specified ID is not a valid LONG", event);
				return;
			}

			//Check if its on this shard...
			if (DisCalClient.getClient().getGuildById(Snowflake.of(args[1])).block() != null) {
				GuildSettings settings = DatabaseManager.getSettings(Snowflake.of(args[1])).block();
				settings.setPatronGuild(!settings.isPatronGuild());
				DatabaseManager.updateSettings(settings).subscribe();

				MessageManager.sendMessageAsync("Guild connected to this shard. isPatronGuild value updated!", event);
				return;
			}

			//Just send this direct to the appropriate client
			try {
				JSONObject requestJson = new JSONObject();

				requestJson.put("realm", DisCalRealm.GUILD_IS_PATRON.name());
				requestJson.put("guild_id", Snowflake.of(args[1]).asLong());

				int shardIndex = GuildUtils.findShard(Snowflake.of(args[1]));

				OkHttpClient client = new OkHttpClient();
				RequestBody requestBody = RequestBody.create(GlobalConst.JSON, requestJson.toString());
				Request request = new Request.Builder()
						.url(BotSettings.COM_SUB_DOMAIN.get() + shardIndex + ".discalbot.com/api/v1/com/bot/action/handle")
						.header("Authorization", BotSettings.BOT_API_TOKEN.get())
						.post(requestBody)
						.build();

				MessageManager.sendMessageAsync("Attempting to update...", event);

				Response response = client.newCall(request).execute();

				if (response.isSuccessful()) {
					MessageManager.sendMessageAsync("Successfully updated!", event);
				} else {
					MessageManager.sendMessageAsync("Failed to update, please check the logs", event);
				}
			} catch (Exception e) {
				LogFeed.log(LogObject
						.forException("Failed to handle patron update", e, this.getClass()));
				MessageManager.sendMessageAsync("An error occurred, it has been logged", event);
			}
		} else {
			MessageManager.sendMessageAsync("Please specify the ID of the guild to set as a patron guild with `!dev patron <ID>`", event);
		}
	}

	@SuppressWarnings("all")
	private void moduleEval(MessageCreateEvent event) {
		Guild guild = event.getGuild().block();
		Member user = event.getMember().get();
		Message message = event.getMessage();
		DiscordClient client = event.getClient();
		MessageChannel channel = event.getMessage().getChannel().block();
		String input = message.getContent().get().substring(message.getContent().get().indexOf("eval") + 5).replaceAll("`", "");
		Object o = null;
		factory.put("guild", guild);
		factory.put("channel", channel);
		factory.put("user", user);
		factory.put("message", message);
		factory.put("command", this);
		factory.put("client", client);
		factory.put("builder", new EmbedCreateSpec());
		factory.put("cUser", client.getSelf());

		try {
			o = factory.eval(input);
		} catch (Exception ex) {
			Consumer<EmbedCreateSpec> embed = spec -> {
				spec.setAuthor("DisCal", GlobalConst.discalSite, GlobalConst.iconUrl);
				spec.setTitle("Error");
				spec.setDescription(ex.getMessage());
				spec.setFooter("Eval failed", null);
				spec.setColor(GlobalConst.discalColor);
			};
			MessageManager.sendMessageAsync(embed, event);
			return;
		}

		Object finalO = o;
		Consumer<EmbedCreateSpec> embed = spec -> {
			spec.setAuthor("DisCal", GlobalConst.discalSite, GlobalConst.iconUrl);
			spec.setTitle("Success! -- Eval Output.");
			spec.setColor(GlobalConst.discalColor);
			spec.setDescription(finalO == null ? "No output, object is null" : finalO.toString());
			spec.addField("Input", "```java\n" + input + "\n```", false);
			spec.setFooter("Eval successful!", null);
		};
		MessageManager.sendMessageAsync(embed, event);
	}

	private void moduleDevGuild(String[] args, MessageCreateEvent event) {
		if (args.length == 2) {
			try {
				Long.valueOf(args[1]);
			} catch (NumberFormatException ignore) {
				MessageManager.sendMessageAsync("Specified ID is not a valid LONG", event);
				return;
			}
			//Check if its on this shard...
			if (DisCalClient.getClient().getGuildById(Snowflake.of(args[1])).block() != null) {
				GuildSettings settings = DatabaseManager.getSettings(Snowflake.of(args[1])).block();
				settings.setDevGuild(!settings.isDevGuild());
				DatabaseManager.updateSettings(settings).subscribe();

				MessageManager.sendMessageAsync("Guild connected to this shard. isDevGuild value updated!", event);
				return;
			}

			//Just send this direct to the appropriate client
			try {
				JSONObject requestJson = new JSONObject();

				requestJson.put("realm", DisCalRealm.GUILD_IS_DEV.name());
				requestJson.put("guild_id", Snowflake.of(args[1]).asLong());

				int shardIndex = GuildUtils.findShard(Snowflake.of(args[1]));

				OkHttpClient client = new OkHttpClient();
				RequestBody requestBody = RequestBody.create(GlobalConst.JSON, requestJson.toString());
				Request request = new Request.Builder()
						.url(BotSettings.COM_SUB_DOMAIN.get() + shardIndex + ".discalbot.com/api/v1/com/bot/action/handle")
						.header("Authorization", BotSettings.BOT_API_TOKEN.get())
						.post(requestBody)
						.build();

				MessageManager.sendMessageAsync("Attempting to update...", event);

				Response response = client.newCall(request).execute();

				if (response.isSuccessful()) {
					MessageManager.sendMessageAsync("Successfully updated!", event);
				} else {
					MessageManager.sendMessageAsync("Failed to update, please check the logs", event);
				}
			} catch (Exception e) {
				LogFeed.log(LogObject
						.forException("Failed to handle dev update", e, this.getClass()));
				MessageManager.sendMessageAsync("An error occurred, it has been logged", event);
			}
		} else {
			MessageManager.sendMessageAsync("Please specify the ID of the guild to set as a dev guild with `!dev dev <ID>`", event);
		}
	}

	private void moduleMaxCalendars(String[] args, MessageCreateEvent event) {
		if (args.length == 3) {
			try {
				int mc = Integer.parseInt(args[2]);
				mc = Math.abs(mc);

				try {
					Long.valueOf(args[1]);
				} catch (NumberFormatException ignore) {
					MessageManager.sendMessageAsync("Specified ID is not a valid LONG", event);
					return;
				}

				//Check if its on this shard...
				if (DisCalClient.getClient().getGuildById(Snowflake.of(args[1])).block() != null) {
					GuildSettings settings = DatabaseManager.getSettings(Snowflake.of(args[1])).block();
					settings.setMaxCalendars(mc);
					DatabaseManager.updateSettings(settings).subscribe();

					MessageManager.sendMessageAsync("Guild connected to this shard. Max calendar value has been updated!", event);
					return;
				}

				//Just send this direct to the appropriate client
				try {
					JSONObject requestJson = new JSONObject();

					requestJson.put("realm", DisCalRealm.GUILD_MAX_CALENDARS.name());
					requestJson.put("max_calendars", mc);
					requestJson.put("guild_id", Snowflake.of(args[1]).asLong());

					int shardIndex = GuildUtils.findShard(Snowflake.of(args[1]));

					OkHttpClient client = new OkHttpClient();
					RequestBody requestBody = RequestBody.create(GlobalConst.JSON, requestJson.toString());
					Request request = new Request.Builder()
							.url(BotSettings.COM_SUB_DOMAIN.get() + shardIndex + ".discalbot.com/api/v1/com/bot/action/handle")
							.header("Authorization", BotSettings.BOT_API_TOKEN.get())
							.post(requestBody)
							.build();

					MessageManager.sendMessageAsync("Attempting to update...", event);

					Response response = client.newCall(request).execute();

					if (response.isSuccessful()) {
						MessageManager.sendMessageAsync("Successfully updated!", event);
					} else {
						MessageManager.sendMessageAsync("Failed to update, please check the logs", event);
					}
				} catch (Exception e) {
					LogFeed.log(LogObject
							.forException("Failed to handle max cal update", e, this.getClass()));
					MessageManager.sendMessageAsync("An error occurred, it has been logged", event);
				}
			} catch (NumberFormatException e) {
				MessageManager.sendMessageAsync("Max Calendar amount must be a valid Integer!", event);
			}
		} else {
			MessageManager.sendMessageAsync("Please specify the ID of the guild and calendar amount with `!dev maxcal <ID> <amount>`", event);
		}
	}

	private void moduleLeaveGuild(String[] args, MessageCreateEvent event) {
		if (args.length == 2) {
			try {
				Long.valueOf(args[1]);
			} catch (NumberFormatException ignore) {
				MessageManager.sendMessageAsync("Specified ID is not a valid LONG", event);
				return;
			}

			//Check if its on this shard...
			Guild g = DisCalClient.getClient().getGuildById(Snowflake.of(args[1])).block();
			if (g != null) {
				g.leave().subscribe();

				MessageManager.sendMessageAsync("Guild connected to this shard has been left!", event);
				return;
			}

			//Just send this direct to the appropriate client
			try {
				JSONObject requestJson = new JSONObject();

				requestJson.put("realm", DisCalRealm.GUILD_LEAVE.name());
				requestJson.put("guild_id", Snowflake.of(args[1]).asLong());

				int shardIndex = GuildUtils.findShard(Snowflake.of(args[1]));

				OkHttpClient client = new OkHttpClient();
				RequestBody requestBody = RequestBody.create(GlobalConst.JSON, requestJson.toString());
				Request request = new Request.Builder()
						.url(BotSettings.COM_SUB_DOMAIN.get() + shardIndex + ".discalbot.com/api/v1/com/bot/action/handle")
						.header("Authorization", BotSettings.BOT_API_TOKEN.get())
						.post(requestBody)
						.build();

				MessageManager.sendMessageAsync("Attempting to update...", event);

				Response response = client.newCall(request).execute();

				if (response.isSuccessful()) {
					MessageManager.sendMessageAsync("Successfully updated!", event);
				} else {
					MessageManager.sendMessageAsync("Failed to update, please check the logs", event);
				}
			} catch (Exception e) {
				LogFeed.log(LogObject
						.forException("Failed to handle dev update", e, this.getClass()));
				MessageManager.sendMessageAsync("An error occurred, it has been logged", event);
			}
		} else {
			MessageManager.sendMessageAsync("Please specify the ID of the guild to leave with `!dev leave <ID>`", event);
		}
	}

	private void moduleReloadLangs(final MessageCreateEvent event) {
		MessageManager.sendMessageAsync("Sending lang reload request to all clients async, this may take time...", event);

		Thread thread = new Thread(() -> {
			JSONObject requestJson = new JSONObject();

			requestJson.put("realm", DisCalRealm.BOT_LANGS.name());

			OkHttpClient client = new OkHttpClient.Builder()
					.connectTimeout(5, TimeUnit.SECONDS)
					.build();

			for (int i = 0; i < Integer.parseInt(BotSettings.SHARD_COUNT.get()); i++) {
				try {
					RequestBody requestBody = RequestBody.create(GlobalConst.JSON, requestJson.toString());
					Request request = new Request.Builder()
							.url(BotSettings.COM_SUB_DOMAIN.get() + i + ".discalbot.com/api/v1/com/bot/action/handle")
							.header("Authorization", BotSettings.BOT_API_TOKEN.get())
							.post(requestBody)
							.build();
					client.newCall(request).execute();
				} catch (Exception e) {
					LogFeed.log(LogObject
							.forException("Lang reload failed", "Shard: " + i, e, this.getClass()));
				}
			}

			//Once all operations are done.
			MessageManager.sendMessageAsync("All shards have handled the lang reload request", event);
		});

		thread.setDaemon(true);
		thread.start();


		MessageManager.sendMessageAsync("Reloading lang files! Please give this time to propagate across the network.", event);
	}


	private void registerApiKey(String[] args, MessageCreateEvent event) {
		if (args.length == 2) {
			MessageManager.sendMessageAsync("Registering new API key...", event);

			String userId = args[1];

			UserAPIAccount account = new UserAPIAccount();
			account.setUserId(userId);
			account.setAPIKey(KeyGenerator.csRandomAlphaNumericString(64));
			account.setTimeIssued(System.currentTimeMillis());
			account.setBlocked(false);
			account.setUses(0);

			if (DatabaseManager.updateAPIAccount(account).block()) {
				MessageManager.sendMessageAsync("Check your DMs for the new API Key!", event);
				MessageManager.sendDirectMessageAsync(account.getAPIKey(), event.getMember().get());
			} else {
				MessageManager.sendMessageAsync("Error occurred! Could not register new API key!", event);
			}
		} else {
			MessageManager.sendMessageAsync("Please specify the USER ID linked to the key!", event);
		}
	}

	private void blockAPIKey(String[] args, MessageCreateEvent event) {
		if (args.length == 2) {
			MessageManager.sendMessageAsync("Blocking API key...", event);

			String key = args[1];

			UserAPIAccount account = DatabaseManager.getAPIAccount(key).block();
			account.setBlocked(true);

			if (DatabaseManager.updateAPIAccount(account).block())
				MessageManager.sendMessageAsync("Successfully blocked API key!", event);
			else
				MessageManager.sendMessageAsync("Error occurred! Could not block API key!", event);
		} else {
			MessageManager.sendMessageAsync("Please specify the API KEY!", event);
		}
	}

	private void moduleCheckSettings(String[] args, MessageCreateEvent event) {
		if (args.length == 2) {
			//String id = args[1];

			MessageManager.sendMessageAsync("HEY! This command is being redone cuz of networking!", event);

			//TODO: Send/Receive from Pub/Sub.
			/*
			try {

				IGuild guild = DisCalAPI.getAPI().getClient().getGuildByID(Long.valueOf(id));

				if (guild != null) {
					GuildSettings settings = DatabaseManager.getManager().getSettings(guild.getLongID());

					EmbedBuilder em = new EmbedBuilder();
					em.withAuthorIcon(DisCalAPI.getAPI().iconUrl);
					em.withAuthorName("DisCal");
					em.withTitle(MessageManager.getMessage("Embed.DisCal.Settings.Title", settings));
					em.appendField(MessageManager.getMessage("Embed.DisCal.Settings.ExternalCal", settings), String.valueOf(settings.useExternalCalendar()), true);
					if (RoleUtils.roleExists(settings.getControlRole(), guild)) {
						em.appendField(MessageManager.getMessage("Embed.Discal.Settings.Role", settings), RoleUtils.getRoleNameFromID(settings.getControlRole(), guild), true);
					} else {
						em.appendField(MessageManager.getMessage("Embed.Discal.Settings.Role", settings), "everyone", true);
					}
					if (ChannelUtils.channelExists(settings.getDiscalChannel(), guild)) {
						em.appendField(MessageManager.getMessage("Embed.DisCal.Settings.Channel", settings), ChannelUtils.getChannelNameFromNameOrId(settings.getDiscalChannel(), guild.getLongID()), false);
					} else {
						em.appendField(MessageManager.getMessage("Embed.DisCal.Settings.Channel", settings), "All Channels", true);
					}
					em.appendField(MessageManager.getMessage("Embed.DisCal.Settings.SimpleAnn", settings), String.valueOf(settings.usingSimpleAnnouncements()), true);
					em.appendField(MessageManager.getMessage("Embed.DisCal.Settings.Patron", settings), String.valueOf(settings.isPatronGuild()), true);
					em.appendField(MessageManager.getMessage("Embed.DisCal.Settings.Dev", settings), String.valueOf(settings.isDevGuild()), true);
					em.appendField(MessageManager.getMessage("Embed.DisCal.Settings.MaxCal", settings), String.valueOf(settings.getMaxCalendars()), true);
					em.appendField(MessageManager.getMessage("Embed.DisCal.Settings.Language", settings), settings.getLang(), true);
					em.appendField(MessageManager.getMessage("Embed.DisCal.Settings.Prefix", settings), settings.getPrefix(), true);
					//TODO: Add translations...
					em.appendField("Using Branding", settings.isBranded() + "", true);
					em.withFooterText(MessageManager.getMessage("Embed.DisCal.Info.Patron", settings) + ": https://www.patreon.com/Novafox");
					em.withUrl("https://www.discalbot.com/");
					em.withColor(56, 138, 237);
					MessageManager.sendMessage(em.build(), event);
				} else {
					MessageManager.sendMessage("The specified guild is not connected to DisCal or does not Exist", event);
				}
			} catch (Exception e) {
				MessageManager.sendMessage("Guild ID must be of type long!", event);
			}
			*/
		} else {
			MessageManager.sendMessageAsync("Please specify the ID of the guild to check settings for!", event);
		}
	}
}