package org.dreamexposure.discal.client.module.command;

import org.dreamexposure.discal.client.DisCalClient;
import org.dreamexposure.discal.client.message.MessageManager;
import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.object.BotSettings;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.command.CommandInfo;
import org.dreamexposure.discal.core.utils.*;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

import java.util.ArrayList;

/**
 * Created by Nova Fox on 1/5/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class DisCalCommand implements ICommand {

	/**
	 * Gets the command this Object is responsible for.
	 *
	 * @return The command this Object is responsible for.
	 */
	@Override
	public String getCommand() {
		return "Discal";
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
		CommandInfo info = new CommandInfo("event");
		info.setDescription("Used to configure DisCal");
		info.setExample("!DisCal (function) (value)");

		info.getSubCommands().put("settings", "Displays the bot's settings.");
		info.getSubCommands().put("role", "Sets the control role for the bot.");
		info.getSubCommands().put("channel", "Sets the channel bot commands can be used in.");
		info.getSubCommands().put("simpleannouncement", "Removes \"Advanced\" info from announcements.");
		info.getSubCommands().put("dmannouncement", "Allows the bot to DM a user an announcement.");
		info.getSubCommands().put("dmannouncements", "Alias for \"dmAnnouncement\"");
		info.getSubCommands().put("language", "Sets the bot's language.");
		info.getSubCommands().put("lang", "Sets the bot's language.");
		info.getSubCommands().put("prefix", "Sets the bot's prefix.");
		info.getSubCommands().put("invite", "Displays an invite to the support guild.");
		info.getSubCommands().put("dashboard", "Displays the link to the web control dashboard.");
		info.getSubCommands().put("brand", "Enables/Disables server branding.");

		return info;
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
		if (args.length < 1) {
			moduleDisCalInfo(event, settings);
		} else {
			switch (args[0].toLowerCase()) {
				case "discal":
					moduleDisCalInfo(event, settings);
					break;
				case "settings":
					moduleSettings(event, settings);
					break;
				case "role":
					moduleControlRole(args, event, settings);
					break;
				case "channel":
					moduleDisCalChannel(args, event, settings);
					break;
				case "simpleannouncement":
					moduleSimpleAnnouncement(event, settings);
					break;
				case "dmannouncement":
					moduleDmAnnouncements(event, settings);
					break;
				case "dmannouncements":
					moduleDmAnnouncements(event, settings);
					break;
				case "language":
					moduleLanguage(args, event, settings);
					break;
				case "lang":
					moduleLanguage(args, event, settings);
					break;
				case "prefix":
					modulePrefix(args, event, settings);
					break;
				case "invite":
					moduleInvite(event, settings);
					break;
				case "dashboard":
					moduleDashboard(event, settings);
					break;
				case "brand":
					moduleBrand(event, settings);
					break;
				default:
					MessageManager.sendMessageAsync(MessageManager.getMessage("Notification.Args.Invalid", settings), event);
					break;
			}
		}
		return false;
	}

	private void moduleDisCalInfo(MessageReceivedEvent event, GuildSettings settings) {
		IGuild guild = event.getGuild();

		EmbedBuilder em = new EmbedBuilder();
		em.withAuthorIcon(GlobalConst.discalSite);
		em.withAuthorName("DisCal");
		em.withAuthorUrl(GlobalConst.discalSite);
		em.withTitle(MessageManager.getMessage("Embed.DisCal.Info.Title", settings));
		em.appendField(MessageManager.getMessage("Embed.DisCal.Info.Developer", settings), "DreamExposure", true);
		em.appendField(MessageManager.getMessage("Embed.Discal.Info.Version", settings), GlobalConst.version, true);
		em.appendField(MessageManager.getMessage("Embed.DisCal.Info.Library", settings), "Discord4J, version 2.10.1", false);
		em.appendField(MessageManager.getMessage("Embed.DisCal.Info.TotalGuilds", settings), DisCalClient.getClient().getGuilds().size() + "", true);
		em.appendField(MessageManager.getMessage("Embed.DisCal.Info.TotalCalendars", settings), DatabaseManager.getManager().getCalendarCount() + "", true);
		em.appendField(MessageManager.getMessage("Embed.DisCal.Info.TotalAnnouncements", settings), DatabaseManager.getManager().getAnnouncementCount() + "", true);
		em.appendField(MessageManager.getMessage("Embed.DisCal.Info.Ping", "%shard%", (guild.getShard().getInfo()[0] + 1) + "/" + BotSettings.SHARD_COUNT.get(), settings), guild.getShard().getResponseTime() + "ms", false);
		em.withFooterText(MessageManager.getMessage("Embed.DisCal.Info.Patron", settings) + ": https://www.patreon.com/Novafox");
		em.withUrl("https://www.discalbot.com");
		em.withColor(GlobalConst.discalColor);
		MessageManager.sendMessageAsync(em.build(), event);
	}

	/**
	 * Sets the control role for the guild.
	 *
	 * @param args  The args of the command.
	 * @param event The event received.
	 */
	private void moduleControlRole(String[] args, MessageReceivedEvent event, GuildSettings settings) {
		if (PermissionChecker.hasSufficientRole(event)) {
			if (args.length > 1) {
				String roleName = GeneralUtils.getContent(args, 1);
				IRole controlRole;

				if (!"everyone".equalsIgnoreCase(roleName)) {
					controlRole = RoleUtils.getRoleFromID(roleName, event);

					if (controlRole != null) {
						settings.setControlRole(controlRole.getStringID());
						DatabaseManager.getManager().updateSettings(settings);
						//Send message.
						MessageManager.sendMessageAsync(MessageManager.getMessage("DisCal.ControlRole.Set", "%role%", controlRole.getName(), settings), event);

					} else {
						//Invalid role.
						MessageManager.sendMessageAsync(MessageManager.getMessage("DisCal.ControlRole.Invalid", settings), event);
					}
				} else {
					//Role is @everyone, set this so that anyone can control the bot.
					settings.setControlRole("everyone");
					DatabaseManager.getManager().updateSettings(settings);
					//Send message
					MessageManager.sendMessageAsync(MessageManager.getMessage("DisCal.ControlRole.Reset", settings), event);
				}
			} else {
				MessageManager.sendMessageAsync(MessageManager.getMessage("DisCal.ControlRole.Specify", settings), event);
			}
		} else {
			MessageManager.sendMessageAsync(MessageManager.getMessage("Notification.Perm.CONTROL_ROLE", settings), event);
		}
	}

	/**
	 * Sets the channel for the guild that DisCal can respond in.
	 *
	 * @param args  The command args
	 * @param event The event received.
	 */
	private void moduleDisCalChannel(String[] args, MessageReceivedEvent event, GuildSettings settings) {
		if (args.length == 2) {
			String channelName = args[1];
			if (channelName.equalsIgnoreCase("all")) {
				//Reset channel info.
				settings.setDiscalChannel("all");
				DatabaseManager.getManager().updateSettings(settings);
				MessageManager.sendMessageAsync(MessageManager.getMessage("DisCal.Channel.All", settings), event);
			} else {
				if (ChannelUtils.channelExists(channelName, event)) {
					IChannel channel = ChannelUtils.getChannelFromNameOrId(channelName, event);
					if (channel != null) {
						settings.setDiscalChannel(channel.getStringID());
						DatabaseManager.getManager().updateSettings(settings);
						MessageManager.sendMessageAsync(MessageManager.getMessage("DisCal.Channel.Set", "%channel%", channel.getName(), settings), event);
					} else {
						MessageManager.sendMessageAsync(MessageManager.getMessage("Discal.Channel.NotFound", settings), event);
					}
				} else {
					MessageManager.sendMessageAsync(MessageManager.getMessage("Discal.Channel.NotFound", settings), event);
				}
			}
		} else {
			MessageManager.sendMessageAsync(MessageManager.getMessage("DisCal.Channel.Specify", settings), event);
		}
	}

	private void moduleSimpleAnnouncement(MessageReceivedEvent event, GuildSettings settings) {
		settings.setSimpleAnnouncements(!settings.usingSimpleAnnouncements());
		DatabaseManager.getManager().updateSettings(settings);

		MessageManager.sendMessageAsync(MessageManager.getMessage("DisCal.SimpleAnnouncement", "%value%", settings.usingSimpleAnnouncements() + "", settings), event);
	}

	private void moduleSettings(MessageReceivedEvent event, GuildSettings settings) {
		EmbedBuilder em = new EmbedBuilder();
		em.withAuthorIcon(GlobalConst.discalSite);
		em.withAuthorName("DisCal");
		em.withAuthorUrl(GlobalConst.discalSite);
		em.withTitle(MessageManager.getMessage("Embed.DisCal.Settings.Title", settings));
		em.appendField(MessageManager.getMessage("Embed.DisCal.Settings.ExternalCal", settings), String.valueOf(settings.useExternalCalendar()), true);
		if (RoleUtils.roleExists(settings.getControlRole(), event)) {
			em.appendField(MessageManager.getMessage("Embed.Discal.Settings.Role", settings), RoleUtils.getRoleNameFromID(settings.getControlRole(), event), true);
		} else {
			em.appendField(MessageManager.getMessage("Embed.Discal.Settings.Role", settings), "everyone", true);
		}
		if (ChannelUtils.channelExists(settings.getDiscalChannel(), event)) {
			em.appendField(MessageManager.getMessage("Embed.DisCal.Settings.Channel", settings), ChannelUtils.getChannelNameFromNameOrId(settings.getDiscalChannel(), event.getGuild()), false);
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
		em.withColor(GlobalConst.discalColor);
		MessageManager.sendMessageAsync(em.build(), event);
	}

	private void moduleDmAnnouncements(MessageReceivedEvent event, GuildSettings settings) {
		if (settings.isDevGuild()) {
			IUser user = event.getAuthor();

			if (settings.getDmAnnouncements().contains(user.getStringID())) {
				settings.getDmAnnouncements().remove(user.getStringID());
				DatabaseManager.getManager().updateSettings(settings);
				MessageManager.sendMessageAsync(MessageManager.getMessage("DisCal.DmAnnouncements.Off", settings), event);
			} else {
				settings.getDmAnnouncements().add(user.getStringID());
				DatabaseManager.getManager().updateSettings(settings);
				MessageManager.sendMessageAsync(MessageManager.getMessage("DisCal.DmAnnouncements.On", settings), event);
			}
		} else {
			MessageManager.sendMessageAsync(MessageManager.getMessage("Notification.Disabled", settings), event);
		}
	}

	private void modulePrefix(String[] args, MessageReceivedEvent event, GuildSettings settings) {
		if (PermissionChecker.hasManageServerRole(event)) {
			if (args.length == 2) {
				String prefix = args[1];

				settings.setPrefix(prefix);
				DatabaseManager.getManager().updateSettings(settings);

				MessageManager.sendMessageAsync(MessageManager.getMessage("DisCal.Prefix.Set", "%prefix%", prefix, settings), event);
			} else {
				MessageManager.sendMessageAsync(MessageManager.getMessage("DisCal.Prefix.Specify", settings), event);
			}
		} else {
			MessageManager.sendMessageAsync(MessageManager.getMessage("Notification.Perm.MANAGE_SERVER", settings), event);
		}
	}

	private void moduleLanguage(String[] args, MessageReceivedEvent event, GuildSettings settings) {
		if (PermissionChecker.hasManageServerRole(event)) {
			if (args.length == 2) {
				String value = args[1];
				if (MessageManager.isSupported(value)) {
					String valid = MessageManager.getValidLang(value);

					settings.setLang(valid);
					DatabaseManager.getManager().updateSettings(settings);

					MessageManager.sendMessageAsync(MessageManager.getMessage("DisCal.Lang.Success", settings), event);
				} else {
					String langs = MessageManager.getLangs().toString().replace("[", "").replace("]", "");
					MessageManager.sendMessageAsync(MessageManager.getMessage("DisCal.Lang.Unsupported", "%values%", langs, settings), event);
				}
			} else {
				String langs = MessageManager.getLangs().toString().replace("[", "").replace("]", "");
				MessageManager.sendMessageAsync(MessageManager.getMessage("DisCal.Lang.Specify", "%values%", langs, settings), event);
			}
		} else {
			MessageManager.sendMessageAsync(MessageManager.getMessage("Notification.Perm.MANAGE_SERVER", settings), event);
		}
	}

	private void moduleInvite(MessageReceivedEvent event, GuildSettings settings) {
		MessageManager.sendMessageAsync(MessageManager.getMessage("DisCal.InviteLink", "%link%", GlobalConst.supportInviteLink, settings), event);
	}

	private void moduleBrand(MessageReceivedEvent event, GuildSettings settings) {
		if (PermissionChecker.hasSufficientRole(event)) {
			if (settings.isPatronGuild()) {
				settings.setBranded(!settings.isBranded());
				DatabaseManager.getManager().updateSettings(settings);

				MessageManager.sendMessageAsync(MessageManager.getMessage("DisCal.Brand", "%value%", settings.isBranded() + "", settings), event);
			} else {
				MessageManager.sendMessageAsync(MessageManager.getMessage("Notification.Patron", settings), event);
			}
		} else {
			MessageManager.sendMessageAsync(MessageManager.getMessage("Notification.Perm.CONTROL_ROLE", settings), event);
		}
	}

	private void moduleDashboard(MessageReceivedEvent event, GuildSettings settings) {
		MessageManager.sendMessageAsync(MessageManager.getMessage("DisCal.DashboardLink", "%link%", GlobalConst.discalDashboardLink, settings), event);
	}
}