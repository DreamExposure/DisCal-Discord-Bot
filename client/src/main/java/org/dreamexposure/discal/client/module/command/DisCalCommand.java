package org.dreamexposure.discal.client.module.command;

import org.dreamexposure.discal.client.message.MessageManager;
import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.object.BotSettings;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.command.CommandInfo;
import org.dreamexposure.discal.core.utils.ChannelUtils;
import org.dreamexposure.discal.core.utils.GeneralUtils;
import org.dreamexposure.discal.core.utils.GlobalConst;
import org.dreamexposure.discal.core.utils.PermissionChecker;
import org.dreamexposure.discal.core.utils.RoleUtils;

import java.util.ArrayList;
import java.util.function.Consumer;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Image;

/**
 * Created by Nova Fox on 1/5/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
@SuppressWarnings("OptionalGetWithoutIsPresent")
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
        CommandInfo info = new CommandInfo(
                "event",
                "Used to configure DisCal",
                "!DisCal (function) (value)"
        );

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
    public boolean issueCommand(String[] args, MessageCreateEvent event, GuildSettings settings) {
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
                case "dmannouncements":
                    moduleDmAnnouncements(event, settings);
                    break;
                case "language":
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

    private void moduleDisCalInfo(MessageCreateEvent event, GuildSettings settings) {
        Consumer<EmbedCreateSpec> embed = spec -> {
            Guild guild = event.getGuild().block();

            if (settings.isBranded() && guild != null)
                spec.setAuthor(guild.getName(), GlobalConst.discalSite, guild.getIconUrl(Image.Format.PNG).orElse(GlobalConst.iconUrl));
            else
                spec.setAuthor("DisCal", GlobalConst.discalSite, GlobalConst.iconUrl);

            spec.setTitle(MessageManager.getMessage("Embed.DisCal.Info.Title", settings));
            spec.addField(MessageManager.getMessage("Embed.DisCal.Info.Developer", settings), "DreamExposure", true);
            spec.addField(MessageManager.getMessage("Embed.Discal.Info.Version", settings), GlobalConst.version, true);
            spec.addField(MessageManager.getMessage("Embed.DisCal.Info.Library", settings), "Discord4J, version 3.0.6", false);
            spec.addField("Shard Index", BotSettings.SHARD_INDEX.get() + "/" + BotSettings.SHARD_COUNT.get(), true);
            spec.addField(MessageManager.getMessage("Embed.DisCal.Info.TotalGuilds", settings), event.getClient().getGuilds().count().block() + "", true);
            spec.addField(MessageManager.getMessage("Embed.DisCal.Info.TotalCalendars", settings), DatabaseManager.getCalendarCount().block() + "", true);
            spec.addField(MessageManager.getMessage("Embed.DisCal.Info.TotalAnnouncements", settings), DatabaseManager.getAnnouncementCount().block() + "", true);
            spec.setFooter(MessageManager.getMessage("Embed.DisCal.Info.Patron", settings) + ": https://www.patreon.com/Novafox", null);
            spec.setUrl("https://www.discalbot.com");
            spec.setColor(GlobalConst.discalColor);
        };
        MessageManager.sendMessageAsync(embed, event);
    }

    /**
     * Sets the control role for the guild.
     *
     * @param args  The args of the command.
     * @param event The event received.
     */
    private void moduleControlRole(String[] args, MessageCreateEvent event, GuildSettings settings) {
        if (PermissionChecker.hasDisCalRole(event, settings).block()) {
            if (args.length > 1) {
                String roleName = GeneralUtils.getContent(args, 1);
                Role controlRole;

                if (!"everyone".equalsIgnoreCase(roleName)) {
                    controlRole = RoleUtils.getRoleFromID(roleName, event);

                    if (controlRole != null) {
                        settings.setControlRole(controlRole.getId().asString());
                        DatabaseManager.updateSettings(settings).subscribe();
                        //Send message.
                        MessageManager.sendMessageAsync(MessageManager.getMessage("DisCal.ControlRole.Set", "%role%", controlRole.getName(), settings), event);

                    } else {
                        //Invalid role.
                        MessageManager.sendMessageAsync(MessageManager.getMessage("DisCal.ControlRole.Invalid", settings), event);
                    }
                } else {
                    //Role is @everyone, set this so that anyone can control the bot.
                    settings.setControlRole("everyone");
                    DatabaseManager.updateSettings(settings).subscribe();
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
    private void moduleDisCalChannel(String[] args, MessageCreateEvent event, GuildSettings settings) {
        if (args.length == 2) {
            String channelName = args[1];
            if (channelName.equalsIgnoreCase("all")) {
                //Reset channel info.
                settings.setDiscalChannel("all");
                DatabaseManager.updateSettings(settings).subscribe();
                MessageManager.sendMessageAsync(MessageManager.getMessage("DisCal.Channel.All", settings), event);
            } else {
                if (ChannelUtils.channelExists(channelName, event)) {
                    GuildChannel channel = ChannelUtils.getChannelFromNameOrId(channelName, event);
                    if (channel != null) {
                        settings.setDiscalChannel(channel.getId().asString());
                        DatabaseManager.updateSettings(settings).subscribe();
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

    private void moduleSimpleAnnouncement(MessageCreateEvent event, GuildSettings settings) {
        settings.setSimpleAnnouncements(!settings.usingSimpleAnnouncements());
        DatabaseManager.updateSettings(settings).subscribe();

        MessageManager.sendMessageAsync(MessageManager.getMessage("DisCal.SimpleAnnouncement", "%value%", settings.usingSimpleAnnouncements() + "", settings), event);
    }

    private void moduleSettings(MessageCreateEvent event, GuildSettings settings) {
        Consumer<EmbedCreateSpec> embed = spec -> {
            Guild guild = event.getGuild().block();

            if (settings.isBranded() && guild != null)
                spec.setAuthor(guild.getName(), GlobalConst.discalSite, guild.getIconUrl(Image.Format.PNG).orElse(GlobalConst.iconUrl));
            else
                spec.setAuthor("DisCal", GlobalConst.discalSite, GlobalConst.iconUrl);

            spec.setTitle(MessageManager.getMessage("Embed.DisCal.Settings.Title", settings));
            spec.addField(MessageManager.getMessage("Embed.DisCal.Settings.ExternalCal", settings), String.valueOf(settings.useExternalCalendar()), true);
            if (RoleUtils.roleExists(settings.getControlRole(), event)) {
                spec.addField(MessageManager.getMessage("Embed.Discal.Settings.Role", settings), RoleUtils.getRoleNameFromID(settings.getControlRole(), event), true);
            } else {
                spec.addField(MessageManager.getMessage("Embed.Discal.Settings.Role", settings), "everyone", true);
            }
            if (ChannelUtils.channelExists(settings.getDiscalChannel(), event)) {
                spec.addField(MessageManager.getMessage("Embed.DisCal.Settings.Channel", settings), ChannelUtils.getChannelNameFromNameOrId(settings.getDiscalChannel(), guild), false);
            } else {
                spec.addField(MessageManager.getMessage("Embed.DisCal.Settings.Channel", settings), "All Channels", true);
            }
            spec.addField(MessageManager.getMessage("Embed.DisCal.Settings.SimpleAnn", settings), String.valueOf(settings.usingSimpleAnnouncements()), true);
            spec.addField(MessageManager.getMessage("Embed.DisCal.Settings.Patron", settings), String.valueOf(settings.isPatronGuild()), true);
            spec.addField(MessageManager.getMessage("Embed.DisCal.Settings.Dev", settings), String.valueOf(settings.isDevGuild()), true);
            spec.addField(MessageManager.getMessage("Embed.DisCal.Settings.MaxCal", settings), String.valueOf(settings.getMaxCalendars()), true);
            spec.addField(MessageManager.getMessage("Embed.DisCal.Settings.Language", settings), settings.getLang(), true);
            spec.addField(MessageManager.getMessage("Embed.DisCal.Settings.Prefix", settings), settings.getPrefix(), true);
            //TODO: Add translations...
            spec.addField("Using Branding", settings.isBranded() + "", true);
            spec.setFooter(MessageManager.getMessage("Embed.DisCal.Info.Patron", settings) + ": https://www.patreon.com/Novafox", null);
            spec.setUrl("https://www.discalbot.com/");
            spec.setColor(GlobalConst.discalColor);
        };
        MessageManager.sendMessageAsync(embed, event);
    }

    private void moduleDmAnnouncements(MessageCreateEvent event, GuildSettings settings) {
        if (settings.isDevGuild()) {
            Member user = event.getMember().get();

            if (settings.getDmAnnouncements().contains(user.getId().asString())) {
                settings.getDmAnnouncements().remove(user.getId().asString());
                DatabaseManager.updateSettings(settings).subscribe();
                MessageManager.sendMessageAsync(MessageManager.getMessage("DisCal.DmAnnouncements.Off", settings), event);
            } else {
                settings.getDmAnnouncements().add(user.getId().asString());
                DatabaseManager.updateSettings(settings).subscribe();
                MessageManager.sendMessageAsync(MessageManager.getMessage("DisCal.DmAnnouncements.On", settings), event);
            }
        } else {
            MessageManager.sendMessageAsync(MessageManager.getMessage("Notification.Disabled", settings), event);
        }
    }

    private void modulePrefix(String[] args, MessageCreateEvent event, GuildSettings settings) {
        if (PermissionChecker.hasManageServerRole(event).blockOptional().orElse(false)) {
            if (args.length == 2) {
                String prefix = args[1];

                settings.setPrefix(prefix);
                DatabaseManager.updateSettings(settings).subscribe();

                MessageManager.sendMessageAsync(MessageManager.getMessage("DisCal.Prefix.Set", "%prefix%", prefix, settings), event);
            } else {
                MessageManager.sendMessageAsync(MessageManager.getMessage("DisCal.Prefix.Specify", settings), event);
            }
        } else {
            MessageManager.sendMessageAsync(MessageManager.getMessage("Notification.Perm.MANAGE_SERVER", settings), event);
        }
    }

    private void moduleLanguage(String[] args, MessageCreateEvent event, GuildSettings settings) {
        if (PermissionChecker.hasManageServerRole(event).blockOptional().orElse(false)) {
            if (args.length == 2) {
                String value = args[1];
                if (MessageManager.isSupported(value)) {
                    String valid = MessageManager.getValidLang(value);

                    settings.setLang(valid);
                    DatabaseManager.updateSettings(settings).subscribe();

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

    private void moduleInvite(MessageCreateEvent event, GuildSettings settings) {
        MessageManager.sendMessageAsync(MessageManager.getMessage("DisCal.InviteLink", "%link%", GlobalConst.supportInviteLink, settings), event);
    }

    private void moduleBrand(MessageCreateEvent event, GuildSettings settings) {
        if (PermissionChecker.hasDisCalRole(event, settings).block()) {
            if (settings.isPatronGuild()) {
                settings.setBranded(!settings.isBranded());
                DatabaseManager.updateSettings(settings).subscribe();

                MessageManager.sendMessageAsync(MessageManager.getMessage("DisCal.Brand", "%value%", settings.isBranded() + "", settings), event);
            } else {
                MessageManager.sendMessageAsync(MessageManager.getMessage("Notification.Patron", settings), event);
            }
        } else {
            MessageManager.sendMessageAsync(MessageManager.getMessage("Notification.Perm.CONTROL_ROLE", settings), event);
        }
    }

    private void moduleDashboard(MessageCreateEvent event, GuildSettings settings) {
        MessageManager.sendMessageAsync(MessageManager.getMessage("DisCal.DashboardLink", "%link%", GlobalConst.discalDashboardLink, settings), event);
    }
}