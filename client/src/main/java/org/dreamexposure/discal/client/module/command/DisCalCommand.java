package org.dreamexposure.discal.client.module.command;

import org.dreamexposure.discal.client.message.Messages;
import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.object.BotSettings;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.command.CommandInfo;
import org.dreamexposure.discal.core.utils.ChannelUtils;
import org.dreamexposure.discal.core.utils.GlobalConst;
import org.dreamexposure.discal.core.utils.PermissionChecker;
import org.dreamexposure.discal.core.utils.RoleUtils;

import java.util.ArrayList;
import java.util.function.Consumer;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Image;
import reactor.core.publisher.Mono;
import reactor.function.TupleUtils;

/**
 * Created by Nova Fox on 1/5/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class DisCalCommand implements Command {

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
     * <br>
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
        final CommandInfo info = new CommandInfo(
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
     * @return {@code true} if successful, else {@code false}.
     */
    @Override
    public Mono<Void> issueCommand(final String[] args, final MessageCreateEvent event, final GuildSettings settings) {
        return Mono.defer(() -> {
            if (args.length < 1) {
                return this.moduleDisCalInfo(event, settings);
            } else {
                switch (args[0].toLowerCase()) {
                    case "discal":
                        return this.moduleDisCalInfo(event, settings);
                    case "settings":
                        return this.moduleSettings(event, settings);
                    case "role":
                        return this.moduleControlRole(args, event, settings);
                    case "channel":
                        return this.moduleDisCalChannel(args, event, settings);
                    case "simpleannouncement":
                        return this.moduleSimpleAnnouncement(event, settings);
                    case "dmannouncement":
                    case "dmannouncements":
                        return this.moduleDmAnnouncements(event, settings);
                    case "language":
                    case "lang":
                        return this.moduleLanguage(args, event, settings);
                    case "prefix":
                        return this.modulePrefix(args, event, settings);
                    case "invite":
                        return this.moduleInvite(event, settings);
                    case "dashboard":
                        return this.moduleDashboard(event, settings);
                    case "brand":
                        return this.moduleBrand(event, settings);
                    default:
                        return Messages.sendMessage(Messages.getMessage("Notification.Args.Invalid", settings), event);
                }
            }
        }).then();
    }

    private Mono<Void> moduleDisCalInfo(final MessageCreateEvent event, final GuildSettings settings) {
        final Mono<Guild> guildMono = event.getGuild();
        final Mono<String> guildCountMono = event.getClient().getGuilds().count().map(i -> i + "");
        final Mono<String> calCountMono = DatabaseManager.getCalendarCount().map(i -> i + "");
        final Mono<String> annCountMono = DatabaseManager.getAnnouncementCount().map(i -> i + "");

        final Mono<Consumer<EmbedCreateSpec>> embedMono = Mono.zip(guildMono, guildCountMono, calCountMono, annCountMono)
            .map(TupleUtils.function((guild, guilds, calendars, announcements) -> (EmbedCreateSpec spec) -> {
                if (settings.getBranded())
                    spec.setAuthor(guild.getName(), GlobalConst.discalSite, guild.getIconUrl(Image.Format.PNG).orElse(GlobalConst.iconUrl));
                else
                    spec.setAuthor("DisCal", GlobalConst.discalSite, GlobalConst.iconUrl);

                spec.setTitle(Messages.getMessage("Embed.DisCal.Info.Title", settings));
                spec.addField(Messages.getMessage("Embed.DisCal.Info.Developer", settings), "DreamExposure", true);
                spec.addField(Messages.getMessage("Embed.Discal.Info.Version", settings), GlobalConst.version, true);
                spec.addField(Messages.getMessage("Embed.DisCal.Info.Library", settings), GlobalConst.d4jVersion, false);
                spec.addField("Shard Index", BotSettings.SHARD_INDEX.get() + "/" + BotSettings.SHARD_COUNT.get(), true);
                spec.addField(Messages.getMessage("Embed.DisCal.Info.TotalGuilds", settings), guilds, true);
                spec.addField(Messages.getMessage("Embed.DisCal.Info.TotalCalendars", settings), calendars, true);
                spec.addField(Messages.getMessage("Embed.DisCal.Info.TotalAnnouncements", settings), announcements, true);
                spec.setFooter(Messages.getMessage("Embed.DisCal.Info.Patron", settings) + ": https://www.patreon.com/Novafox", null);
                spec.setUrl("https://www.discalbot.com");
                spec.setColor(GlobalConst.discalColor);
            }));

        return embedMono.flatMap(embed -> Messages.sendMessage(embed, event)).then();
    }

    private Mono<Void> moduleControlRole(final String[] args, final MessageCreateEvent event, final GuildSettings settings) {
        return PermissionChecker.hasManageServerRole(event)
            .filter(identity -> identity)
            .flatMap(ignore -> {
                if (args.length == 2) {
                    final String roleName = args[1];
                    if ("everyone".equalsIgnoreCase(roleName)) {
                        settings.setControlRole("everyone");
                        return DatabaseManager.updateSettings(settings)
                            .then(Messages.sendMessage(Messages.getMessage("DisCal.ControlRole.Reset", settings), event));
                    } else {
                        return event.getGuild().flatMap(g -> RoleUtils.getRole(roleName, g)
                            .doOnNext(r -> settings.setControlRole(r.getId().asString()))
                            .flatMap(r ->
                                DatabaseManager.updateSettings(settings)
                                    .then(Messages.sendMessage(Messages.getMessage("DisCal.ControlRole.Set", "%role%",
                                        r.getName(), settings), event))
                            ).switchIfEmpty(Messages.sendMessage(Messages.getMessage("DisCal.ControlRole.Invalid", settings), event))
                        );
                    }
                } else {
                    return Messages.sendMessage(Messages.getMessage("DisCal.ControlRole.Specify", settings), event);
                }
            }).switchIfEmpty(Messages.sendMessage(Messages.getMessage("Notification.Perm.MANAGE_SERVER", settings), event))
            .then();
    }

    private Mono<Void> moduleDisCalChannel(final String[] args, final MessageCreateEvent event, final GuildSettings settings) {
        return PermissionChecker.hasManageServerRole(event)
            .filter(identity -> identity)
            .flatMap(ignore -> {
                if (args.length == 2) {
                    final String channelName = args[1];
                    if ("all".equalsIgnoreCase(channelName)) {
                        settings.setDiscalChannel("all");
                        return DatabaseManager.updateSettings(settings)
                            .then(Messages.sendMessage(Messages.getMessage("DisCal.Channel.All", settings), event));
                    } else {
                        return event.getGuild().flatMap(g -> ChannelUtils.getChannelFromNameOrId(channelName, g)
                            .doOnNext(c -> settings.setDiscalChannel(c.getId().asString()))
                            .flatMap(channel ->
                                DatabaseManager.updateSettings(settings)
                                    .then(Messages.sendMessage(Messages.getMessage("DisCal.Channel.Set", "%channel%",
                                        channel.getName(), settings), event))
                            ).switchIfEmpty(Messages.sendMessage(Messages.getMessage("Discal.Channel.NotFound", settings), event))
                        );
                    }
                } else {
                    return Messages.sendMessage(Messages.getMessage("DisCal.Channel.Specify", settings), event);
                }
            }).switchIfEmpty(Messages.sendMessage(Messages.getMessage("Notification.Perm.MANAGE_SERVER", settings), event))
            .then();
    }

    private Mono<Void> moduleSimpleAnnouncement(final MessageCreateEvent event, final GuildSettings settings) {
        return Mono.just(settings)
            .doOnNext(s -> s.setSimpleAnnouncements(!s.getSimpleAnnouncements()))
            .flatMap(DatabaseManager::updateSettings)
            .then(Messages.sendMessage(
                Messages.getMessage("DisCal.SimpleAnnouncement", "%value%", settings.getSimpleAnnouncements() + "", settings)
                , event))
            .then();
    }

    private Mono<Void> moduleSettings(final MessageCreateEvent event, final GuildSettings settings) {
        final Mono<Guild> guildMono = event.getGuild().cache();
        final Mono<String> dRoleMono = RoleUtils.getRoleNameFromID(settings.getControlRole(), event).defaultIfEmpty("everyone");
        final Mono<String> dChanMono = guildMono.flatMap(g ->
            ChannelUtils.getChannelNameFromNameOrId(settings.getDiscalChannel(), g)).defaultIfEmpty("All Channels");

        final Mono<Consumer<EmbedCreateSpec>> embedMono = Mono.zip(guildMono, dRoleMono, dChanMono)
            .map(TupleUtils.function((guild, dRole, dChannel) -> spec -> {
                if (settings.getBranded())
                    spec.setAuthor(guild.getName(), GlobalConst.discalSite, guild.getIconUrl(Image.Format.PNG).orElse(GlobalConst.iconUrl));
                else
                    spec.setAuthor("DisCal", GlobalConst.discalSite, GlobalConst.iconUrl);

                spec.setTitle(Messages.getMessage("Embed.DisCal.Settings.Title", settings));
                spec.addField(Messages.getMessage("Embed.Discal.Settings.Role", settings), dRole, true);
                spec.addField(Messages.getMessage("Embed.DisCal.Settings.Channel", settings), dChannel, false);
                spec.addField(Messages.getMessage("Embed.DisCal.Settings.SimpleAnn", settings), settings.getSimpleAnnouncements() + "",
                    true);
                spec.addField(Messages.getMessage("Embed.DisCal.Settings.Patron", settings), settings.getPatronGuild() + "", true);
                spec.addField(Messages.getMessage("Embed.DisCal.Settings.Dev", settings), settings.getDevGuild() + "", true);
                spec.addField(Messages.getMessage("Embed.DisCal.Settings.MaxCal", settings), settings.getMaxCalendars() + "", true);
                spec.addField(Messages.getMessage("Embed.DisCal.Settings.Language", settings), settings.getLang(), true);
                spec.addField(Messages.getMessage("Embed.DisCal.Settings.Prefix", settings), settings.getPrefix(), true);
                //TODO: Add translations...
                spec.addField("Using Branding", settings.getBranded() + "", true);
                spec.setFooter(Messages.getMessage("Embed.DisCal.Info.Patron", settings) + ": https://www.patreon.com/Novafox", null);
                spec.setUrl("https://www.discalbot.com/");
                spec.setColor(GlobalConst.discalColor);
            }));

        return embedMono.flatMap(embed -> Messages.sendMessage(embed, event)).then();
    }

    private Mono<Void> moduleDmAnnouncements(final MessageCreateEvent event, final GuildSettings settings) {
        return Mono.just(settings.getDevGuild())
            .filter(identity -> identity)
            .flatMap(b -> event.getMessage().getAuthorAsMember())
            .flatMap(member -> {
                if (settings.getDmAnnouncements().contains(member.getId().asString())) {
                    settings.getDmAnnouncements().remove(member.getId().asString());
                    return DatabaseManager.updateSettings(settings)
                        .then(Messages.sendMessage(Messages.getMessage("DisCal.DmAnnouncements.Off", settings), event));
                } else {
                    settings.getDmAnnouncements().add(member.getId().asString());
                    return DatabaseManager.updateSettings(settings)
                        .then(Messages.sendMessage(Messages.getMessage("DisCal.DmAnnouncements.On", settings), event));
                }
            })
            .switchIfEmpty(Messages.sendMessage(Messages.getMessage("Notification.Disabled", settings), event))
            .then();
    }

    private Mono<Void> modulePrefix(final String[] args, final MessageCreateEvent event, final GuildSettings settings) {
        return PermissionChecker.hasManageServerRole(event)
            .filter(identity -> identity)
            .flatMap(ignore -> {
                if (args.length == 2) {
                    final String prefix = args[1];
                    settings.setPrefix(prefix);

                    return DatabaseManager.updateSettings(settings).then(Messages
                        .sendMessage(Messages.getMessage("DisCal.Prefix.Set", "%prefix%", prefix, settings), event));
                } else {
                    return Messages.sendMessage(Messages.getMessage("DisCal.Prefix.Specify", settings), event);
                }
            })
            .switchIfEmpty(Messages.sendMessage(Messages.getMessage("Notification.Perm.MANAGE_SERVER", settings), event))
            .then();
    }

    private Mono<Void> moduleLanguage(final String[] args, final MessageCreateEvent event, final GuildSettings settings) {
        return PermissionChecker.hasManageServerRole(event)
            .filter(identity -> identity)
            .flatMap(ignore -> {
                if (args.length == 2) {
                    final String value = args[1];
                    if (Messages.isSupported(value)) {
                        final String valid = Messages.getValidLang(value);
                        settings.setLang(valid);

                        return DatabaseManager.updateSettings(settings)
                            .then(Messages.sendMessage(Messages.getMessage("DisCal.Lang.Success", settings), event));
                    } else {
                        final String langs = Messages.getLangs().toString().replace("[", "").replace("]", "");
                        return Messages.sendMessage(
                            Messages.getMessage("DisCal.Lang.Unsupported", "%values%", langs, settings), event);
                    }
                } else {
                    final String langs = Messages.getLangs().toString().replace("[", "").replace("]", "");
                    return Messages.sendMessage(
                        Messages.getMessage("DisCal.Lang.Unsupported", "%values%", langs, settings), event);
                }
            })
            .switchIfEmpty(Messages.sendMessage(Messages.getMessage("Notification.Perm.MANAGE_SERVER", settings), event))
            .then();
    }

    private Mono<Void> moduleInvite(final MessageCreateEvent event, final GuildSettings settings) {
        return Messages.sendMessage(Messages.getMessage("DisCal.InviteLink", "%link%",
            GlobalConst.supportInviteLink, settings), event).then();
    }

    private Mono<Void> moduleBrand(final MessageCreateEvent event, final GuildSettings settings) {
        return PermissionChecker.hasDisCalRole(event, settings)
            .filter(identity -> identity)
            .map(b -> settings.getPatronGuild())
            .flatMap(isPatron -> {
                if (isPatron) {
                    settings.setBranded(!settings.getBranded());
                    return DatabaseManager.updateSettings(settings)
                        .then(Messages.sendMessage(
                            Messages.getMessage("DisCal.Brand", "%value%", settings.getBranded() + "", settings),
                            event));
                } else {
                    return Messages.sendMessage(Messages.getMessage("Notification.Patron", settings), event);
                }
            })
            .switchIfEmpty(Messages.sendMessage(Messages.getMessage("Notification.Perm.CONTROL_ROLE", settings), event))
            .then();
    }

    private Mono<Void> moduleDashboard(final MessageCreateEvent event, final GuildSettings settings) {
        return Messages.sendMessage(Messages.getMessage("DisCal.DashboardLink", "%link%",
            GlobalConst.discalDashboardLink, settings), event).then();
    }
}
