package org.dreamexposure.discal.client.module.command;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Image;
import org.dreamexposure.discal.Application;
import org.dreamexposure.discal.client.message.Messages;
import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.object.BotSettings;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.command.CommandInfo;
import org.dreamexposure.discal.core.utils.ChannelUtils;
import org.dreamexposure.discal.core.utils.GlobalVal;
import org.dreamexposure.discal.core.utils.PermissionChecker;
import reactor.core.publisher.Mono;
import reactor.function.TupleUtils;

import java.util.ArrayList;

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
        info.getSubCommands().put("channel", "Sets the channel bot commands can be used in.");
        info.getSubCommands().put("prefix", "Sets the bot's prefix.");
        info.getSubCommands().put("invite", "Displays an invite to the support guild.");
        info.getSubCommands().put("dashboard", "Displays the link to the web control dashboard.");

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
                return switch (args[0].toLowerCase()) {
                    case "discal" -> this.moduleDisCalInfo(event, settings);
                    case "channel" -> this.moduleDisCalChannel(args, event, settings);
                    case "prefix" -> this.modulePrefix(args, event, settings);
                    case "invite" -> this.moduleInvite(event, settings);
                    case "dashboard" -> this.moduleDashboard(event, settings);
                    default -> Messages.sendMessage(Messages.getMessage("Notification.Args.Invalid", settings), event);
                };
            }
        }).then();
    }

    private Mono<Void> moduleDisCalInfo(final MessageCreateEvent event, final GuildSettings settings) {
        final Mono<Guild> guildMono = event.getGuild();
        final Mono<String> guildCountMono = event.getClient().getGuilds().count().map(i -> i + "");
        final Mono<String> calCountMono = DatabaseManager.INSTANCE.getCalendarCount().map(i -> i + "");
        final Mono<String> annCountMono = DatabaseManager.INSTANCE.getAnnouncementCount().map(i -> i + "");

        final Mono<EmbedCreateSpec> embedMono = Mono.zip(guildMono, guildCountMono, calCountMono, annCountMono)
            .map(TupleUtils.function((guild, guilds, calendars, announcements) -> {
                var builder = EmbedCreateSpec.builder()
                    .title(Messages.getMessage("Embed.DisCal.Info.Title", settings))
                    .addField(Messages.getMessage("Embed.DisCal.Info.Developer", settings), "DreamExposure", true)
                    .addField(Messages.getMessage("Embed.Discal.Info.Version", settings), GlobalVal.getVersion(), true)
                    .addField(Messages.getMessage("Embed.DisCal.Info.Library", settings), GlobalVal.getD4jVersion(), false)
                    .addField("Shard Index", Application.getShardIndex() + "/" + Application.getShardCount(), true)
                    .addField(Messages.getMessage("Embed.DisCal.Info.TotalGuilds", settings), guilds, true)
                    .addField(Messages.getMessage("Embed.DisCal.Info.TotalCalendars", settings), calendars, true)
                    .addField(Messages.getMessage("Embed.DisCal.Info.TotalAnnouncements", settings), announcements, true)
                    .footer(Messages.getMessage("Embed.DisCal.Info.Patron", settings) + ": https://www.patreon.com/Novafox", null)
                    .url(BotSettings.BASE_URL.get())
                    .color(GlobalVal.getDiscalColor());

                if (settings.getBranded())
                    builder.author(guild.getName(), BotSettings.BASE_URL.get(),
                        guild.getIconUrl(Image.Format.PNG).orElse(GlobalVal.getIconUrl()));
                else
                    builder.author("DisCal", BotSettings.BASE_URL.get(), GlobalVal.getIconUrl());

                return builder.build();
            }));

        return embedMono.flatMap(embed -> Messages.sendMessage(embed, event)).then();
    }

    private Mono<Void> moduleDisCalChannel(final String[] args, final MessageCreateEvent event, final GuildSettings settings) {
        return PermissionChecker.hasManageServerRole(event)
            .filter(identity -> identity)
            .flatMap(ignore -> {
                if (args.length == 2) {
                    final String channelName = args[1];
                    if ("all".equalsIgnoreCase(channelName)) {
                        settings.setDiscalChannel("all");
                        return DatabaseManager.INSTANCE.updateSettings(settings)
                            .then(Messages.sendMessage(Messages.getMessage("DisCal.Channel.All", settings), event));
                    } else {
                        return event.getGuild().flatMap(g -> ChannelUtils.getChannelFromNameOrId(channelName, g)
                            .doOnNext(c -> settings.setDiscalChannel(c.getId().asString()))
                            .flatMap(channel ->
                                DatabaseManager.INSTANCE.updateSettings(settings)
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

    private Mono<Void> modulePrefix(final String[] args, final MessageCreateEvent event, final GuildSettings settings) {
        return PermissionChecker.hasManageServerRole(event)
            .filter(identity -> identity)
            .flatMap(ignore -> {
                if (args.length == 2) {
                    final String prefix = args[1];
                    settings.setPrefix(prefix);

                    return DatabaseManager.INSTANCE.updateSettings(settings).then(Messages
                        .sendMessage(Messages.getMessage("DisCal.Prefix.Set", "%prefix%", prefix, settings), event));
                } else {
                    return Messages.sendMessage(Messages.getMessage("DisCal.Prefix.Specify", settings), event);
                }
            })
            .switchIfEmpty(Messages.sendMessage(Messages.getMessage("Notification.Perm.MANAGE_SERVER", settings), event))
            .then();
    }

    private Mono<Void> moduleInvite(final MessageCreateEvent event, final GuildSettings settings) {
        return Messages.sendMessage(Messages.getMessage("DisCal.InviteLink", "%link%",
            BotSettings.SUPPORT_INVITE.get(), settings), event).then();
    }

    private Mono<Void> moduleDashboard(final MessageCreateEvent event, final GuildSettings settings) {
        return Messages.sendMessage(Messages.getMessage("DisCal.DashboardLink", "%link%",
            GlobalVal.getDiscalDashboardLink(), settings), event).then();
    }
}
