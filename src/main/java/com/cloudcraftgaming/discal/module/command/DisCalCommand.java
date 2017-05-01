package com.cloudcraftgaming.discal.module.command;

import com.cloudcraftgaming.discal.Main;
import com.cloudcraftgaming.discal.database.DatabaseManager;
import com.cloudcraftgaming.discal.internal.data.GuildSettings;
import com.cloudcraftgaming.discal.module.command.info.CommandInfo;
import com.cloudcraftgaming.discal.utils.*;
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

        info.getSubCommands().add("settings");
        info.getSubCommands().add("role");
        info.getSubCommands().add("channel");
        info.getSubCommands().add("simpleAnnouncement");
        info.getSubCommands().add("dmAnnouncement");
        info.getSubCommands().add("language");
        info.getSubCommands().add("lang");

        return info;
    }

    /**
     * Issues the command this Object is responsible for.
     * @param args The command arguments.
     * @param event The event received.
     * @return <code>true</code> if successful, else <code>false</code>.
     */
    @Override
    public Boolean issueCommand(String[] args, MessageReceivedEvent event) {
        if (args.length < 1) {
            moduleDisCalInfo(event);
        } else {
            switch (args[0].toLowerCase()) {
                case "discal":
                    moduleDisCalInfo(event);
                    break;
                case "settings":
                    moduleSettings(event);
                    break;
                case "role":
                    moduleControlRole(args, event);
                    break;
                case "channel":
                    moduleDisCalChannel(args, event);
                    break;
                case "simpleannouncement":
                    moduleSimpleAnnouncement(event);
                    break;
                case "dmannouncement":
                    moduleDmAnnouncements(event);
                    break;
				case "language":
					moduleLanguage(args, event);
					break;
				case "lang":
					moduleLanguage(args, event);
					break;
                default:
                    Message.sendMessage(MessageManager.getMessage("Notification.Args.Invalid", event), event);
                    break;
            }
        }
        return false;
    }

    private void moduleDisCalInfo(MessageReceivedEvent event) {
        IGuild guild = event.getGuild();

        EmbedBuilder em = new EmbedBuilder();
        em.withAuthorIcon(Main.client.getGuildByID(266063520112574464L).getIconURL());
        em.withAuthorName("DisCal!");
        em.withTitle(MessageManager.getMessage("Embed.DisCal.Info.Title", event));
        em.appendField(MessageManager.getMessage("Embed.DisCal.Info.Developer", event), "NovaFox161", true);
        em.appendField(MessageManager.getMessage("Embed.Discal.Info.Version", event), Main.version, true);
        em.appendField(MessageManager.getMessage("Embed.DisCal.Info.Library", event), "Discord4J, version 2.8.1", false);
        em.appendField(MessageManager.getMessage("Embed.DisCal.Info.TotalGuilds", event), Main.client.getGuilds().size() + "", true);
        em.appendField(MessageManager.getMessage("Embed.DisCal.Info.TotalCalendars", event), DatabaseManager.getManager().getCalendarCount() + "", true);
        em.appendField(MessageManager.getMessage("Embed.DisCal.Info.TotalAnnouncements", event), DatabaseManager.getManager().getAnnouncementCount() + "", true);
		em.appendField(MessageManager.getMessage("Embed.DisCal.Info.Ping", "%shard%", guild.getShard().getInfo()[0] + "", event), guild.getShard().getResponseTime() + "ms", false);
        em.withFooterText("[" + MessageManager.getMessage("Embed.DisCal.Info.Patron", event) + "](https://www.patreon.com/Novafox)");
        em.withUrl("https://www.cloudcraftgaming.com/discal/");
        em.withColor(56, 138, 237);
        Message.sendMessage(em.build(), event);
    }

    /**
     * Sets the control role for the guild.
     * @param args The args of the command.
     * @param event The event received.
     */
    private void moduleControlRole(String[] args, MessageReceivedEvent event) {
        if (PermissionChecker.hasSufficientRole(event)) {
            if (args.length == 2) {
                String roleName = args[1];
                IGuild guild = event.getGuild();
                IRole controlRole;

                if (!"everyone".equalsIgnoreCase(roleName)) {
                    controlRole = RoleUtils.getRoleFromID(roleName, event);

                    if (controlRole != null) {
                        GuildSettings settings = DatabaseManager.getManager().getSettings(guild.getLongID());
                        settings.setControlRole(controlRole.getStringID());
                        DatabaseManager.getManager().updateSettings(settings);
                        //Send message.
                        Message.sendMessage(MessageManager.getMessage("DisCal.ControlRole.Set", "%role%", controlRole.getName(), event), event);

                    } else {
                        //Invalid role.
                        Message.sendMessage(MessageManager.getMessage("DisCal.ControlRole.Invalid", event), event);
                    }
                } else {
                    //Role is @everyone, set this so that anyone can control the bot.
                    GuildSettings settings = DatabaseManager.getManager().getSettings(guild.getLongID());
                    settings.setControlRole("everyone");
                    DatabaseManager.getManager().updateSettings(settings);
                    //Send message
                    Message.sendMessage(MessageManager.getMessage("DisCal.ControlRole.Reset", event), event);
                }
            } else {
                Message.sendMessage(MessageManager.getMessage("DisCal.ControlRole.Specify", event), event);
            }
        } else {
            Message.sendMessage(MessageManager.getMessage("Notification.Perm.CONTROL_ROLE", event), event);
        }
    }

    /**
     * Sets the channel for the guild that DisCal can respond in.
     * @param args The command args
     * @param event The event received.
     */
    private void moduleDisCalChannel(String[] args, MessageReceivedEvent event) {
        long guildId = event.getGuild().getLongID();
        if (args.length == 2) {
            String channelName = args[1];
            if (channelName.equalsIgnoreCase("all")) {
                //Reset channel info.
                GuildSettings settings = DatabaseManager.getManager().getSettings(guildId);
                settings.setDiscalChannel("all");
                DatabaseManager.getManager().updateSettings(settings);
                Message.sendMessage(MessageManager.getMessage("DisCal.Channel.All", event), event);
            } else {
                if (ChannelUtils.channelExists(channelName, event)) {
                    IChannel channel = ChannelUtils.getChannelFromNameOrId(channelName, event);
                    if (channel != null) {
                        GuildSettings settings = DatabaseManager.getManager().getSettings(guildId);
                        settings.setDiscalChannel(channel.getStringID());
                        DatabaseManager.getManager().updateSettings(settings);
                        Message.sendMessage(MessageManager.getMessage("DisCal.Channel.Set", "%channel%", channel.getName(), settings), event);
                    } else {
                        Message.sendMessage(MessageManager.getMessage("Discal.Channel.NotFound", event), event);
                    }
                } else {
                    Message.sendMessage(MessageManager.getMessage("Discal.Channel.NotFound", event), event);
                }
            }
        } else {
            Message.sendMessage(MessageManager.getMessage("DisCal.Channel.Specify", event), event);
        }
    }

    private void moduleSimpleAnnouncement(MessageReceivedEvent event) {
        long guildId = event.getGuild().getLongID();
        GuildSettings settings =  DatabaseManager.getManager().getSettings(guildId);
        settings.setSimpleAnnouncements(!settings.usingSimpleAnnouncements());
        DatabaseManager.getManager().updateSettings(settings);

        Message.sendMessage(MessageManager.getMessage("DisCal.SimpleAnnouncement", "%value%", settings.usingSimpleAnnouncements() + "", event), event);
    }

    private void moduleSettings(MessageReceivedEvent event) {
        long guildId = event.getGuild().getLongID();

        GuildSettings settings = DatabaseManager.getManager().getSettings(guildId);

        EmbedBuilder em = new EmbedBuilder();
        em.withAuthorIcon(Main.client.getGuildByID(266063520112574464L).getIconURL());
        em.withAuthorName("DisCal");
        em.withTitle(MessageManager.getMessage("Embed.DisCal.Settings.Title", event));
        em.appendField(MessageManager.getMessage("Embed.DisCal.Settings.ExternalCal", event), String.valueOf(settings.useExternalCalendar()), true);
        if (RoleUtils.roleExists(settings.getControlRole(), event)) {
            em.appendField(MessageManager.getMessage("Embed.Discal.Settings.Role", event), RoleUtils.getRoleNameFromID(settings.getControlRole(), event), true);
        } else {
            em.appendField(MessageManager.getMessage("Embed.Discal.Settings.Role", event), "everyone", true);
        }
        if (ChannelUtils.channelExists(settings.getDiscalChannel(), event)) {
            em.appendField(MessageManager.getMessage("Embed.DisCal.Settings.Channel", event), ChannelUtils.getChannelNameFromNameOrId(settings.getDiscalChannel(), guildId), false);
        } else {
            em.appendField(MessageManager.getMessage("Embed.DisCal.Settings.Channel", event), "All Channels", true);
        }
        em.appendField(MessageManager.getMessage("Embed.DisCal.Settings.SimpleAnn", event), String.valueOf(settings.usingSimpleAnnouncements()), true);
        em.appendField(MessageManager.getMessage("Embed.DisCal.Settings.Patron", event), String.valueOf(settings.isPatronGuild()), true);
        em.appendField(MessageManager.getMessage("Embed.DisCal.Settings.Dev", event), String.valueOf(settings.isDevGuild()), true);
        em.appendField(MessageManager.getMessage("Embed.DisCal.Settings.MaxCal", event), String.valueOf(settings.getMaxCalendars()), true);
        em.appendField(MessageManager.getMessage("Embed.DisCal.Settings.Language", event), settings.getLang(), true);
        em.withFooterText("[" + MessageManager.getMessage("Embed.DisCal.Info.Patron", event) + "](https://www.patreon.com/Novafox)");
        em.withUrl("https://www.cloudcraftgaming.com/discal/");
        em.withColor(56, 138, 237);
        Message.sendMessage(em.build(), event);
    }

    private void moduleDmAnnouncements(MessageReceivedEvent event) {
        long guildId = event.getGuild().getLongID();
        GuildSettings settings = DatabaseManager.getManager().getSettings(guildId);
        if (settings.isDevGuild()) {
            IUser user = event.getAuthor();

            if (settings.getDmAnnouncements().contains(user.getStringID())) {
                settings.getDmAnnouncements().remove(user.getStringID());
                DatabaseManager.getManager().updateSettings(settings);
                Message.sendMessage(MessageManager.getMessage("DisCal.DmAnnouncements.Off", event), event);
            } else {
                settings.getDmAnnouncements().add(user.getStringID());
                DatabaseManager.getManager().updateSettings(settings);
                Message.sendMessage(MessageManager.getMessage("DisCal.DmAnnouncements.On", event), event);
            }
        } else {
            Message.sendMessage(MessageManager.getMessage("Notification.Disabled", event), event);
        }
    }

    private void moduleLanguage(String[] args, MessageReceivedEvent event) {
		if (PermissionChecker.hasManageServerRole(event)) {
			if (args.length == 2) {
				String value = args[1];
				if (MessageManager.isSupported(value)) {
					long guildId = event.getGuild().getLongID();
					String valid = MessageManager.getValidLang(value);
					GuildSettings settings = DatabaseManager.getManager().getSettings(guildId);

					settings.setLang(valid);
					DatabaseManager.getManager().updateSettings(settings);

					Message.sendMessage(MessageManager.getMessage("DisCal.Lang.Success", settings), event);
				} else {
					String langs = MessageManager.getLangs().toString().replace("[", "").replace("]", "");
					Message.sendMessage(MessageManager.getMessage("DisCal.Lang.Unsupported", "%values%", langs, event), event);
				}
			} else {
				String langs = MessageManager.getLangs().toString().replace("[", "").replace("]", "");
				Message.sendMessage(MessageManager.getMessage("DisCal.Lang.Specify", "%values%", langs, event), event);
			}
		} else {
			Message.sendMessage(MessageManager.getMessage("Notification.Perm.MANAGE_SERVER", event), event);
		}
	}
}