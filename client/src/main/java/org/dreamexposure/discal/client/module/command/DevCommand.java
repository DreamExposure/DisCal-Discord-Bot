package org.dreamexposure.discal.client.module.command;

import discord4j.core.DiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.util.Snowflake;
import discord4j.core.spec.EmbedCreateSpec;
import org.dreamexposure.discal.client.DisCalClient;
import org.dreamexposure.discal.client.message.MessageManager;
import org.dreamexposure.discal.core.crypto.KeyGenerator;
import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.enums.network.DisCalRealm;
import org.dreamexposure.discal.core.enums.network.PubSubReason;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.command.CommandInfo;
import org.dreamexposure.discal.core.object.web.UserAPIAccount;
import org.dreamexposure.discal.core.utils.GlobalConst;
import org.dreamexposure.novautils.network.pubsub.PubSubManager;
import org.json.JSONObject;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.ArrayList;
import java.util.function.Consumer;

/**
 * Created by Nova Fox on 4/4/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
@SuppressWarnings({"OptionalGetWithoutIsPresent", "Duplicates"})
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
				GuildSettings settings = DatabaseManager.getManager().getSettings(Snowflake.of(args[1]));
				settings.setPatronGuild(!settings.isPatronGuild());
				DatabaseManager.getManager().updateSettings(settings);

				MessageManager.sendMessageAsync("Guild connected to this shard. isPatronGuild value updated!", event);
				return;
			}

			//Just send this across the network with Pub/Sub... and let the changes propagate
			JSONObject request = new JSONObject();

			request.put("Reason", PubSubReason.HANDLE.name());
			request.put("Realm", DisCalRealm.GUILD_IS_PATRON);
			request.put("Guild-Id", args[1]);

			PubSubManager.get().publish("DisCal/ToClient/All", DisCalClient.clientId(), request);

			MessageManager.sendMessageAsync("DisCal will update the isPatron status of the guild (if connected). Please allow some time for this to propagate across the network!", event);
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
				GuildSettings settings = DatabaseManager.getManager().getSettings(Snowflake.of(args[1]));
				settings.setDevGuild(!settings.isDevGuild());
				DatabaseManager.getManager().updateSettings(settings);

				MessageManager.sendMessageAsync("Guild connected to this shard. isDevGuild value updated!", event);
				return;
			}

			//Just send this across the network with Pub/Sub... and let the changes propagate
			JSONObject request = new JSONObject();

			request.put("Reason", PubSubReason.HANDLE.name());
			request.put("Realm", DisCalRealm.GUILD_IS_DEV);
			request.put("Guild-Id", args[1]);

			PubSubManager.get().publish("DisCal/ToClient/All", DisCalClient.clientId(), request);

			MessageManager.sendMessageAsync("DisCal will update the isDevGuild status of the guild (if connected). Please allow some time for this to propagate across the network!", event);
		} else {
			MessageManager.sendMessageAsync("Please specify the ID of the guild to set as a dev guild with `!dev dev <ID>`", event);
		}
	}

	private void moduleMaxCalendars(String[] args, MessageCreateEvent event) {
		if (args.length == 3) {
			try {
				int mc = Integer.valueOf(args[2]);
				mc = Math.abs(mc);

				try {
					Long.valueOf(args[1]);
				} catch (NumberFormatException ignore) {
					MessageManager.sendMessageAsync("Specified ID is not a valid LONG", event);
					return;
				}

				//Check if its on this shard...
				if (DisCalClient.getClient().getGuildById(Snowflake.of(args[1])).block() != null) {
					GuildSettings settings = DatabaseManager.getManager().getSettings(Snowflake.of(args[1]));
					settings.setMaxCalendars(mc);
					DatabaseManager.getManager().updateSettings(settings);

					MessageManager.sendMessageAsync("Guild connected to this shard. Max calendar value has been updated!", event);
					return;
				}

				//Just send this across the network with Pub/Sub... and let the changes propagate
				JSONObject request = new JSONObject();

				request.put("Reason", PubSubReason.HANDLE.name());
				request.put("Realm", DisCalRealm.GUILD_MAX_CALENDARS);
				request.put("Guild-Id", args[1]);
				request.put("Max-Calendars", mc);

				PubSubManager.get().publish("DisCal/ToClient/All", DisCalClient.clientId(), request);

				MessageManager.sendMessageAsync("DisCal will update the max calendar limit of the specified guild (if connected). Please allow some time for this to propagate across the network!", event);
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

			//Just send this across the network with Pub/Sub... and let the changes propagate
			JSONObject request = new JSONObject();

			request.put("Reason", PubSubReason.HANDLE.name());
			request.put("Realm", DisCalRealm.GUILD_LEAVE);
			request.put("Guild-Id", args[1]);

			PubSubManager.get().publish("DisCal/ToClient/All", DisCalClient.clientId(), request);

			MessageManager.sendMessageAsync("DisCal will leave the specified guild (if connected). Please allow some time for this to propagate across the network!", event);
		} else {
			MessageManager.sendMessageAsync("Please specify the ID of the guild to leave with `!dev leave <ID>`", event);
		}
	}

	private void moduleReloadLangs(MessageCreateEvent event) {
		MessageManager.reloadLangs();

		//Just send this across the network with Pub/Sub... and let the changes propagate
		JSONObject request = new JSONObject();

		request.put("Reason", PubSubReason.HANDLE.name());
		request.put("Realm", DisCalRealm.BOT_LANGS);

		PubSubManager.get().publish("DisCal/ToClient/All", DisCalClient.clientId(), request);

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

			if (DatabaseManager.getManager().updateAPIAccount(account)) {
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

			UserAPIAccount account = DatabaseManager.getManager().getAPIAccount(key);
			account.setBlocked(true);

			if (DatabaseManager.getManager().updateAPIAccount(account))
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