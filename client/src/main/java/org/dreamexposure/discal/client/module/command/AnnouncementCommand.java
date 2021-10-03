package org.dreamexposure.discal.client.module.command;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.Role;
import discord4j.core.spec.EmbedCreateSpec;
import org.dreamexposure.discal.client.announcement.AnnouncementCreator;
import org.dreamexposure.discal.client.message.AnnouncementMessageFormatter;
import org.dreamexposure.discal.client.message.Messages;
import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.enums.announcement.AnnouncementType;
import org.dreamexposure.discal.core.enums.event.EventColor;
import org.dreamexposure.discal.core.object.BotSettings;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.announcement.Announcement;
import org.dreamexposure.discal.core.object.command.CommandInfo;
import org.dreamexposure.discal.core.utils.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.function.TupleUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Nova Fox on 3/4/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class AnnouncementCommand implements Command {

    /**
     * Gets the command this Object is responsible for.
     *
     * @return The command this Object is responsible for.
     */
    @Override
    public String getCommand() {
        return "Announcement";
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
        ArrayList<String> aliases = new ArrayList<>();
        aliases.add("announcements");
        aliases.add("announce");
        aliases.add("alert");
        aliases.add("alerts");
        aliases.add("a");
        aliases.add("ann");
        return aliases;
    }

    /**
     * Gets the info on the command (not sub command) to be used in help menus.
     *
     * @return The command info.
     */
    @Override
    public CommandInfo getCommandInfo() {
        CommandInfo info = new CommandInfo(
            "announcement",
            "Used for all announcement functions.",
            "!announcement <function> (value(s))"
        );

        info.getSubCommands().put("create", "Starts the announcement creator.");
        info.getSubCommands().put("copy", "Copies an existing announcement.");
        info.getSubCommands().put("edit", "Edits an existing announcement.");
        info.getSubCommands().put("confirm", "Confirms and creates/edits the announcement.");
        info.getSubCommands().put("cancel", "Cancels the current announcement creator/editor");
        info.getSubCommands().put("delete", "Deletes an existing announcement.");
        info.getSubCommands().put("view", "Views the specified announcement.");
        info.getSubCommands().put("review", "Reviews the announcement in the creator/editor.");
        info.getSubCommands().put("subscribe", "Subscribes users/roles to the announcement.");
        info.getSubCommands().put("sub", "Subscribes users/roles to the announcement.");
        info.getSubCommands().put("unsubscribe", "Unsubscribes users/roles to the announcement.");
        info.getSubCommands().put("unsub", "Unsubscribes users/roles to the announcement.");
        info.getSubCommands().put("channel", "Sets the channel the announcement will be sent in.");
        info.getSubCommands().put("type", "Sets the announcement's type.");
        info.getSubCommands().put("hours", "Sets the amount of hours before the event to fire (added to minutes)");
        info.getSubCommands().put("minutes", "Sets the amount of minutes before the event to fire (added to hours)");
        info.getSubCommands().put("list", "Lists an amount of events.");
        info.getSubCommands().put("event", "Sets the event the announcement is for (if applicable)");
        info.getSubCommands().put("color", "Sets the color the announcement is for (if applicable)");
        info.getSubCommands().put("info", "Sets an additional info.");
        info.getSubCommands().put("enable", "Enables or Disables the announcement (alias for `disable`)");
        info.getSubCommands().put("disable", "Enables or Disables the announcement (alias for `enable`)");
        info.getSubCommands().put("publish", "Allows for the event to be published if posted in a news channel");

        return info;
    }

    /**
     * Issues the command this Object is responsible for.
     *
     * @param args  The command arguments.
     * @param event The event received.
     */
    @Override
    public Mono<Void> issueCommand(String[] args, MessageCreateEvent event, GuildSettings settings) {
        return Mono.defer(() -> {
            if (args.length < 1) {
                return Messages.sendMessage(Messages.getMessage("Notification.Args.Few", settings), event);
            } else {
                switch (args[0].toLowerCase()) {
                    case "create":
                        return PermissionChecker.hasDisCalRole(event, settings).flatMap(has -> {
                            if (has)
                                return this.moduleCreate(event, settings);
                            else
                                return Messages.sendMessage(
                                    Messages.getMessage("Notification.Perm.CONTROL_ROLE", settings), event);
                        });
                    case "confirm":
                        return PermissionChecker.hasDisCalRole(event, settings).flatMap(has -> {
                            if (has)
                                return this.moduleConfirm(event, settings);
                            else
                                return Messages.sendMessage(
                                    Messages.getMessage("Notification.Perm.CONTROL_ROLE", settings), event);
                        });
                    case "cancel":
                        return PermissionChecker.hasDisCalRole(event, settings).flatMap(has -> {
                            if (has)
                                return this.moduleCancel(event, settings);
                            else
                                return Messages.sendMessage(
                                    Messages.getMessage("Notification.Perm.CONTROL_ROLE", settings), event);
                        });
                    case "delete":
                        return PermissionChecker.hasDisCalRole(event, settings).flatMap(has -> {
                            if (has)
                                return this.moduleDelete(args, event, settings);
                            else
                                return Messages.sendMessage(
                                    Messages.getMessage("Notification.Perm.CONTROL_ROLE", settings), event);
                        });
                    case "view":
                    case "review":
                        return this.moduleView(args, event, settings);
                    case "subscribe":
                    case "sub":
                        return this.moduleSubscribeRewrite(args, event, settings);
                    case "unsubscribe":
                    case "unsub":
                        return this.moduleUnsubscribeRewrite(args, event, settings);
                    case "type":
                        return this.moduleType(args, event, settings);
                    case "hours":
                        return this.moduleHours(args, event, settings);
                    case "minutes":
                        return this.moduleMinutes(args, event, settings);
                    case "list":
                        return this.moduleList(args, event, settings);
                    case "event":
                        return this.moduleEvent(args, event, settings);
                    case "info":
                        return this.moduleInfo(args, event, settings);
                    case "enable":
                    case "enabled":
                    case "disable":
                    case "disabled":
                        return this.moduleEnable(args, event, settings);
                    case "channel":
                        return this.moduleChannel(args, event, settings);
                    case "color":
                    case "colour":
                        return this.moduleColor(args, event, settings);
                    case "publish":
                        if (settings.getPatronGuild() || settings.getPatronGuild())
                            return this.modulePublish(event, settings);
                        else
                            return Messages.sendMessage(Messages.getMessage("Notification.Patron", settings), event);
                    case "copy":
                        return PermissionChecker.hasDisCalRole(event, settings).flatMap(has -> {
                            if (has)
                                return this.moduleCopy(args, event, settings);
                            else
                                return Messages.sendMessage(
                                    Messages.getMessage("Notification.Perm.CONTROL_ROLE", settings), event);
                        });
                    case "edit":
                        return PermissionChecker.hasDisCalRole(event, settings).flatMap(has -> {
                            if (has)
                                return this.moduleEdit(args, event, settings);
                            else
                                return Messages.sendMessage(
                                    Messages.getMessage("Notification.Perm.CONTROL_ROLE", settings), event);
                        });
                    default:
                        return Messages.sendMessage(Messages.getMessage("Notification.Args.Invalid", settings), event);
                }
            }
        }).then();
    }


    private Mono<Void> moduleCreate(MessageCreateEvent event, GuildSettings settings) {
        return Mono.defer(() -> {
            if (AnnouncementCreator.getCreator().hasAnnouncement(settings.getGuildID())) {
                Announcement a = AnnouncementCreator.getCreator().getAnnouncement(settings.getGuildID());

                Mono<Void> deleteUserMessage = Messages.deleteMessage(event);
                Mono<Void> deleteCreatorMessage = Messages.deleteMessage(a.getCreatorMessage());

                return Mono.when(deleteUserMessage, deleteCreatorMessage)
                    .then(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a, settings))
                    .flatMap(em -> Messages.sendMessage(
                        Messages.getMessage("Creator.Announcement.AlreadyInit", settings), em, event))
                    .doOnNext(a::setCreatorMessage);
            } else {
                return AnnouncementCreator.getCreator().init(event, settings)
                    .then(Messages.deleteMessage(event));
            }
        }).then();
    }

    private Mono<Void> moduleEdit(String[] args, MessageCreateEvent event, GuildSettings settings) {
        return Mono.defer(() -> {
            if (AnnouncementCreator.getCreator().hasAnnouncement(settings.getGuildID())) {
                Announcement a = AnnouncementCreator.getCreator().getAnnouncement(settings.getGuildID());

                Mono<Void> deleteUserMessage = Messages.deleteMessage(event);
                Mono<Void> deleteCreatorMessage = Messages.deleteMessage(a.getCreatorMessage());

                return Mono.when(deleteUserMessage, deleteCreatorMessage)
                    .then(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a, settings))
                    .flatMap(em -> Messages.sendMessage(
                        Messages.getMessage("Creator.Announcement.AlreadyInit", settings), em, event))
                    .doOnNext(a::setCreatorMessage);
            } else {
                if (args.length == 2) {
                    return AnnouncementUtils.announcementExists(args[1], settings.getGuildID())
                        .flatMap(exists -> {
                            if (exists) {
                                return AnnouncementCreator.getCreator().edit(event, args[1], settings)
                                    .switchIfEmpty(Messages.sendMessage(
                                        Messages.getMessage("Notification.Error.Unknown", settings), event)
                                        .then(Mono.empty()));
                            } else {
                                return Messages.sendMessage(
                                    Messages.getMessage("Creator.Announcement.CannotFind.Announcement", settings), event);
                            }
                        });
                } else {
                    return Messages.sendMessage(Messages.getMessage("Creator.Announcement.Edit.Specify", settings), event);
                }
            }
        }).then();
    }

    private Mono<Void> moduleConfirm(MessageCreateEvent event, GuildSettings settings) {
        return Mono.defer(() -> {
            if (AnnouncementCreator.getCreator().hasAnnouncement(settings.getGuildID())) {
                Announcement a = AnnouncementCreator.getCreator().getAnnouncement(settings.getGuildID());

                Mono<Void> deleteUserMessage = Messages.deleteMessage(event);
                Mono<Void> deleteCreatorMessage = Messages.deleteMessage(a.getCreatorMessage());

                return Mono.when(deleteUserMessage, deleteCreatorMessage)
                    .then(AnnouncementCreator.getCreator().confirmAnnouncement(settings.getGuildID()))
                    .flatMap(acr -> {
                        if (acr.getSuccessful()) {
                            String msg;
                            if (a.getEditing())
                                msg = Messages.getMessage("Creator.Announcement.Confirm.Edit.Success", settings);
                            else
                                msg = Messages.getMessage("Creator.Announcement.Confirm.Create.Success", settings);

                            return AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a, settings)
                                .flatMap(em -> Messages.sendMessage(msg, em, event));
                        } else {
                            return AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a, settings)
                                .flatMap(em -> Messages.sendMessage(
                                    Messages.getMessage("Creator.Announcement.Confirm.Failure", settings), em, event))
                                .doOnNext(a::setCreatorMessage);
                        }
                    });
            } else {
                return Messages.sendMessage(Messages.getMessage("Creator.Announcement.NotInit", settings), event);
            }
        }).then();
    }

    private Mono<Void> moduleCancel(MessageCreateEvent event, GuildSettings settings) {
        return Mono.defer(() -> {
            if (AnnouncementCreator.getCreator().hasAnnouncement(settings.getGuildID())) {
                Announcement a = AnnouncementCreator.getCreator().getAnnouncement(settings.getGuildID());

                Mono<Void> deleteUserMessage = Messages.deleteMessage(event);
                Mono<Void> deleteCreatorMessage = Messages.deleteMessage(a.getCreatorMessage());
                Mono<Message> sendMsg = Messages.sendMessage(Messages.getMessage("Creator.Announcement.Cancel.Success", settings), event);

                AnnouncementCreator.getCreator().terminate(settings.getGuildID());

                return Mono.when(deleteUserMessage, deleteCreatorMessage, sendMsg);
            } else {
                return Messages.sendMessage(Messages.getMessage("Creator.Announcement.NotInit", settings), event);
            }
        }).then();
    }

    private Mono<Void> moduleDelete(String[] args, MessageCreateEvent event, GuildSettings settings) {
        return Mono.defer(() -> {
            if (AnnouncementCreator.getCreator().hasAnnouncement(settings.getGuildID())) {
                Announcement a = AnnouncementCreator.getCreator().getAnnouncement(settings.getGuildID());

                Mono<Void> deleteUserMessage = Messages.deleteMessage(event);
                Mono<Void> deleteCreatorMessage = Messages.deleteMessage(a.getCreatorMessage());

                return Mono.when(deleteUserMessage, deleteCreatorMessage)
                    .then(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a, settings))
                    .flatMap(em -> Messages.sendMessage(
                        Messages.getMessage("Creator.Announcement.Delete.InCreator", settings), em, event))
                    .doOnNext(a::setCreatorMessage);
            } else if (args.length == 2) {
                return AnnouncementUtils.announcementExists(args[1], settings.getGuildID())
                    .flatMap(exists -> {
                        if (exists) {
                            return DatabaseManager.INSTANCE.deleteAnnouncement(args[1]).flatMap(success -> {
                                if (success) {
                                    return Messages.sendMessage(
                                        Messages.getMessage("Creator.Announcement.Delete.Success", settings), event);
                                } else {
                                    return Messages.sendMessage(
                                        Messages.getMessage("Creator.Announcement.Delete.Failure", settings), event);
                                }
                            });
                        } else {
                            return Messages.sendMessage(
                                Messages.getMessage("Creator.Announcement.CannotFind.Announcement", settings), event);
                        }
                    });
            } else {
                return Messages.sendMessage(Messages.getMessage("Creator.Announcement.Delete.Specify", settings), event);
            }
        }).then();
    }

    private Mono<Void> moduleView(String[] args, MessageCreateEvent event, GuildSettings settings) {
        return Mono.defer(() -> {
            if (AnnouncementCreator.getCreator().hasAnnouncement(settings.getGuildID())) {
                Announcement a = AnnouncementCreator.getCreator().getAnnouncement(settings.getGuildID());

                Mono<Void> deleteUserMessage = Messages.deleteMessage(event);
                Mono<Void> deleteCreatorMessage = Messages.deleteMessage(a.getCreatorMessage());

                return Mono.when(deleteUserMessage, deleteCreatorMessage)
                    .then(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a, settings))
                    .flatMap(em -> Messages.sendMessage(em, event))
                    .doOnNext(a::setCreatorMessage);
            } else {
                if (args.length == 2) {
                    UUID id;
                    try {
                        id = UUID.fromString(args[1]);
                    } catch (IllegalArgumentException e) {
                        return Messages.sendMessage(Messages.getMessage("Creator.Announcement.CannotFind.Announcement", settings), event);
                    }

                    return DatabaseManager.INSTANCE.getAnnouncement(id, settings.getGuildID()).flatMap(a -> {
                        Mono<String> subNamesMono = event.getGuild()
                            .flatMap(g -> AnnouncementMessageFormatter.getSubscriberNames(a, g));

                        Mono<EmbedCreateSpec> emMono = AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a, settings);

                        return Mono.zip(subNamesMono, emMono)
                            .flatMap(TupleUtils.function((subs, embed) -> Messages.sendMessage(subs, embed, event)));
                    }).switchIfEmpty(Messages.sendMessage(
                        Messages.getMessage("Creator.Announcement.CannotFind.Announcement", settings), event));

                } else {
                    return Messages.sendMessage(Messages.getMessage("Creator.Announcement.View.Specify", settings), event);
                }
            }
        }).then();
    }

    private Mono<Void> moduleSubscribeRewriteArgsOne(MessageCreateEvent event, GuildSettings settings) {
        return Mono.defer(() -> {
            if (AnnouncementCreator.getCreator().hasAnnouncement(settings.getGuildID())) {
                Announcement a = AnnouncementCreator.getCreator().getAnnouncement(settings.getGuildID());

                Mono<Void> deleteUserMessage = Messages.deleteMessage(event);
                Mono<Void> deleteCreatorMessage = Messages.deleteMessage(a.getCreatorMessage());

                return event.getMessage().getAuthorAsMember()
                    .flatMap(user -> {
                        if (a.getSubscriberUserIds().contains(user.getId().asString())) {
                            return Mono.when(deleteUserMessage, deleteCreatorMessage)
                                .then(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a, settings))
                                .flatMap(em -> Messages.sendMessage(
                                    Messages.getMessage("Creator.Announcement.Subscribe.Self.Already", settings),
                                    em, event))
                                .doOnNext(a::setCreatorMessage);
                        } else {
                            a.getSubscriberUserIds().add(user.getId().asString());

                            return Mono.when(deleteUserMessage, deleteCreatorMessage)
                                .then(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a, settings))
                                .flatMap(em -> Messages.sendMessage(
                                    Messages.getMessage("Creator.Announcement.Subscribe.Self.Success", settings),
                                    em, event))
                                .doOnNext(a::setCreatorMessage);
                        }
                    });
            } else {
                return Messages.sendMessage(Messages.getMessage("Creator.Announcement.Subscribe.Self.Specify", settings), event);
            }
        }).then();
    }

    private Mono<Void> moduleSubscribeRewriteArgsTwo(String[] args, MessageCreateEvent event, GuildSettings settings) {
        return Mono.defer(() -> {
            String value = args[1];
            if (value.length() <= 32) {
                if (AnnouncementCreator.getCreator().hasAnnouncement(settings.getGuildID())) {
                    Announcement a = AnnouncementCreator.getCreator().getAnnouncement(settings.getGuildID());

                    Mono<Void> deleteUserMessage = Messages.deleteMessage(event);
                    Mono<Void> deleteCreatorMessage = Messages.deleteMessage(a.getCreatorMessage());

                    if ("everyone".equalsIgnoreCase(value) || "here".equalsIgnoreCase(value)) {
                        if (!a.getSubscriberRoleIds().contains(value.toLowerCase().trim())) {
                            a.getSubscriberRoleIds().add(value.toLowerCase().trim());

                            return Mono.when(deleteUserMessage, deleteCreatorMessage)
                                .then(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a, settings))
                                .flatMap(em -> Messages.sendMessage(
                                    Messages.getMessage("Creator.Announcement.Subscribe.Other.Success", "%value%",
                                        value.toLowerCase(), settings), em, event))
                                .doOnNext(a::setCreatorMessage);
                        } else {
                            return Mono.when(deleteUserMessage, deleteCreatorMessage)
                                .then(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a, settings))
                                .flatMap(em -> Messages.sendMessage(
                                    Messages.getMessage("Creator.Announcement.Subscribe.Other.Success", "%value%",
                                        value.toLowerCase(), settings), event)
                                )
                                .doOnNext(a::setCreatorMessage);
                        }
                    } else {
                        //Well, now we check if a user exists, then if not, we check for a role that exists...
                        return event.getGuild().flatMap(guild ->
                            UserUtils.getUser(value, guild).flatMap(mem -> {
                                if (!a.getSubscriberUserIds().contains(mem.getId().asString())) {
                                    a.getSubscriberUserIds().add(mem.getId().asString());

                                    return Mono.when(deleteUserMessage, deleteCreatorMessage)
                                        .then(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a, settings))
                                        .flatMap(em -> Messages.sendMessage(
                                            Messages.getMessage("Creator.Announcement.Subscribe.Other.Success",
                                                "%value%", mem.getUsername(), settings), em, event))
                                        .doOnNext(msg -> a.setCreatorMessage(msg));
                                } else {
                                    return Mono.when(deleteUserMessage, deleteCreatorMessage)
                                        .then(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a, settings))
                                        .flatMap(em -> Messages.sendMessage(
                                            Messages.getMessage("Creator.Announcement.Subscribe.Other.Already",
                                                "%value%", mem.getUsername(), settings), em, event))
                                        .doOnNext(msg -> a.setCreatorMessage(msg));
                                }
                            }).switchIfEmpty(RoleUtils.getRole(value, guild)
                                .flatMap(role -> {
                                    if (!a.getSubscriberRoleIds().contains(role.getId().asString())) {
                                        a.getSubscriberRoleIds().add(role.getId().asString());

                                        return Mono.when(deleteUserMessage, deleteCreatorMessage)
                                            .then(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a, settings))
                                            .flatMap(em -> Messages.sendMessage(
                                                Messages.getMessage("Creator.Announcement.Subscribe.Other.Success",
                                                    "%value%", role.getName(), settings), em, event))
                                            .doOnNext(msg -> a.setCreatorMessage(msg));
                                    } else {
                                        return Mono.when(deleteUserMessage, deleteCreatorMessage)
                                            .then(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a, settings))
                                            .flatMap(em -> Messages.sendMessage(
                                                Messages.getMessage("Creator.Announcement.Subscribe.Other.Already",
                                                    "%value%", role.getName(), settings), em, event))
                                            .doOnNext(msg -> a.setCreatorMessage(msg));
                                    }
                                })
                            ));
                    }
                } else {
                    return Messages.sendMessage(
                        Messages.getMessage("Creator.Announcement.Subscribe.Self.Specify", settings), event);
                }
            } else {
                return AnnouncementUtils.announcementExists(value, settings.getGuildID())
                    .flatMap(exists -> {
                        if (exists) {
                            return DatabaseManager.INSTANCE.getAnnouncement(UUID.fromString(value), settings.getGuildID())
                                .flatMap(a -> event.getMessage().getAuthorAsMember()
                                    .filter(u -> !a.getSubscriberUserIds().contains(u.getId().asString()))
                                    .doOnNext(u -> a.getSubscriberUserIds().add(u.getId().asString()))
                                    .flatMap(u -> DatabaseManager.INSTANCE.updateAnnouncement(a))
                                )
                                .then(Messages.sendMessage(
                                    Messages.getMessage("Creator.Announcement.Subscribe.Self.Success", settings),
                                    event));

                        } else {
                            return Messages.sendMessage(
                                Messages.getMessage("Creator.Announcement.CannotFind.Announcement", settings), event);
                        }
                    });
            }
        }).then();
    }

    private Mono<Void> moduleSubscribeRewriteArgsThree(String[] args, MessageCreateEvent event, GuildSettings settings) {
        return event.getGuild().flatMap(guild -> {
            //First we check if the first arg is an announcement ID, because we don't want to stop people from subbing,
            //while a staff member is creating an announcement.
            if (args[1].length() > 32) {
                return DatabaseManager.INSTANCE.getAnnouncement(UUID.fromString(args[1]), settings.getGuildID()).flatMap(a -> {
                    //We have the announcement, now lets handle subscribing all of those users/roles...
                    List<String> toLookFor = Arrays.asList(args).subList(2, args.length);

                    Mono<List<Member>> membersMono = Flux.fromIterable(toLookFor)
                        .flatMap(str -> UserUtils.getUser(str, guild))
                        .collectList()
                        .defaultIfEmpty(new ArrayList<>()); //So the zip doesn't fail..

                    Mono<List<Role>> rolesMono = Flux.fromIterable(toLookFor)
                        .flatMap(str -> RoleUtils.getRole(str, guild))
                        .collectList()
                        .defaultIfEmpty(new ArrayList<>()); //So the zip doesn't fail..

                    return Mono.zip(membersMono, rolesMono).flatMap(TupleUtils.function((members, roles) -> {
                        List<String> subbedMembers = new ArrayList<>();
                        List<String> subbedRoles = new ArrayList<>();

                        for (Member m : members) {
                            if (!a.getSubscriberUserIds().contains(m.getId().asString())) {
                                a.getSubscriberUserIds().add(m.getId().asString());
                                subbedMembers.add(m.getDisplayName());
                            }
                        }

                        for (Role r : roles) {
                            if (!a.getSubscriberRoleIds().contains(r.getId().asString())) {
                                a.getSubscriberRoleIds().add(r.getId().asString());
                                subbedRoles.add(r.getName());
                            }
                        }

                        String subMemString = subbedMembers.toString().replace("[", "").replace("]", "");
                        String subRoleString = subbedRoles.toString().replace("[", "").replace("]", "");

                        var embed = EmbedCreateSpec.builder()
                            .author("DisCal", BotSettings.BASE_URL.get(), GlobalVal.getIconUrl())
                            .color(GlobalVal.getDiscalColor())
                            .title("Subscribed the Following")
                            .description(Messages.getMessage("Embed.Announcement.Subscribe.Users", "%users%",
                                subMemString, settings)
                                + GlobalVal.getLineBreak()
                                + Messages.getMessage("Embed.Announcement.Subscribe.Roles", "%roles%",
                                subRoleString, settings))
                            .footer(Messages.getMessage("Embed.Announcement.Subscribe.Footer", "%id%",
                                a.getId().toString(), settings), null)
                            .build();

                        return DatabaseManager.INSTANCE.updateAnnouncement(a).thenReturn(embed);
                    }))
                        .flatMap(em -> Messages.sendMessage(em, event));
                })
                    .switchIfEmpty(Messages.sendMessage(
                        Messages.getMessage("Creator.Announcement.CannotFind.Announcement", settings), event))
                    .onErrorResume(IllegalArgumentException.class, e ->
                        Messages.sendMessage(Messages.getMessage("Creator.Announcement.CannotFind.Announcement",
                            settings), event));
            } else if (AnnouncementCreator.getCreator().hasAnnouncement(settings.getGuildID())) {
                Announcement a = AnnouncementCreator.getCreator().getAnnouncement(settings.getGuildID());

                Mono<Void> deleteUserMessage = Messages.deleteMessage(event);
                Mono<Void> deleteCreatorMessage = Messages.deleteMessage(a.getCreatorMessage());

                //Okay, now lets just go about subscribing all of the users/roles...
                List<String> toLookFor = Arrays.asList(args).subList(1, args.length);

                Mono<List<Member>> membersMono = Flux.fromIterable(toLookFor)
                    .flatMap(str -> UserUtils.getUser(str, guild))
                    .collectList()
                    .defaultIfEmpty(new ArrayList<>()); //So the zip doesn't fail..

                Mono<List<Role>> rolesMono = Flux.fromIterable(toLookFor)
                    .flatMap(str -> RoleUtils.getRole(str, guild))
                    .collectList()
                    .defaultIfEmpty(new ArrayList<>()); //So the zip doesn't fail..

                return Mono.zip(membersMono, rolesMono).flatMap(TupleUtils.function((members, roles) -> {
                    List<String> subbedMembers = new ArrayList<>();
                    List<String> subbedRoles = new ArrayList<>();

                    for (Member m : members) {
                        if (!a.getSubscriberUserIds().contains(m.getId().asString())) {
                            a.getSubscriberUserIds().add(m.getId().asString());
                            subbedMembers.add(m.getDisplayName());
                        }
                    }

                    for (Role r : roles) {
                        if (!a.getSubscriberRoleIds().contains(r.getId().asString())) {
                            a.getSubscriberRoleIds().add(r.getId().asString());
                            subbedRoles.add(r.getName());
                        }
                    }

                    String subMemString = subbedMembers.toString().replace("[", "").replace("]", "");
                    String subRoleString = subbedRoles.toString().replace("[", "").replace("]", "");
                    var embed = EmbedCreateSpec.builder()
                        .author("DisCal", BotSettings.BASE_URL.get(), GlobalVal.getIconUrl())
                        .color(GlobalVal.getDiscalColor())
                        .title("Subscribed the Following")
                        .description(Messages.getMessage("Embed.Announcement.Subscribe.Users", "%users%",
                            subMemString, settings)
                            + GlobalVal.getLineBreak()
                            + Messages.getMessage("Embed.Announcement.Subscribe.Roles", "%roles%",
                            subRoleString, settings))
                        .footer(Messages.getMessage("Embed.Announcement.Subscribe.Footer", "%id%",
                            a.getId().toString(), settings), null)
                        .build();

                    return Mono.when(deleteUserMessage, deleteCreatorMessage).thenReturn(embed);
                }))
                    .flatMap(em -> Messages.sendMessage(em, event))
                    .doOnNext(a::setCreatorMessage);
            } else {
                //No announcement being created/edited, and no ID is specified, so we can't sub anyone.
                return Messages.sendMessage(Messages.getMessage("Creator.Announcement.Subscribe.Other.Specify", settings), event);
            }
        }).then();
    }

    private Mono<Void> moduleSubscribeRewrite(String[] args, MessageCreateEvent event, GuildSettings settings) {
        return Mono.defer(() -> {
            if (args.length == 1)
                return this.moduleSubscribeRewriteArgsOne(event, settings);
            else if (args.length == 2)
                return this.moduleSubscribeRewriteArgsTwo(args, event, settings);
            else
                return this.moduleSubscribeRewriteArgsThree(args, event, settings);
        });
    }

    private Mono<Void> moduleUnsubscribeRewriteArgsOne(MessageCreateEvent event, GuildSettings settings) {
        return Mono.defer(() -> {
            if (AnnouncementCreator.getCreator().hasAnnouncement(settings.getGuildID())) {
                Announcement a = AnnouncementCreator.getCreator().getAnnouncement(settings.getGuildID());

                Mono<Void> deleteUserMessage = Messages.deleteMessage(event);
                Mono<Void> deleteCreatorMessage = Messages.deleteMessage(a.getCreatorMessage());

                return event.getMessage().getAuthorAsMember()
                    .flatMap(user -> {
                        if (!a.getSubscriberUserIds().contains(user.getId().asString())) {
                            return Mono.when(deleteUserMessage, deleteCreatorMessage)
                                .then(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a, settings))
                                .flatMap(em -> Messages.sendMessage(
                                    Messages.getMessage("Creator.Announcement.Unsubscribe.Self.Not", settings),
                                    em, event))
                                .doOnNext(a::setCreatorMessage);
                        } else {
                            a.getSubscriberUserIds().remove(user.getId().asString());

                            return Mono.when(deleteUserMessage, deleteCreatorMessage)
                                .then(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a, settings))
                                .flatMap(em -> Messages.sendMessage(
                                    Messages.getMessage("Creator.Announcement.Unsubscribe.Self.Success", settings),
                                    em, event))
                                .doOnNext(a::setCreatorMessage);
                        }
                    });
            } else {
                return Messages.sendMessage(Messages.getMessage("Creator.Announcement.Unsubscribe.Self.Specify", settings), event);
            }
        }).then();
    }

    private Mono<Void> moduleUnsubscribeRewriteArgsTwo(String[] args, MessageCreateEvent event, GuildSettings settings) {
        return Mono.defer(() -> {
            String value = args[1];
            if (value.length() <= 32) {
                if (AnnouncementCreator.getCreator().hasAnnouncement(settings.getGuildID())) {
                    Announcement a = AnnouncementCreator.getCreator().getAnnouncement(settings.getGuildID());

                    Mono<Void> deleteUserMessage = Messages.deleteMessage(event);
                    Mono<Void> deleteCreatorMessage = Messages.deleteMessage(a.getCreatorMessage());

                    if ("everyone".equalsIgnoreCase(value) || "here".equalsIgnoreCase(value)) {
                        if (a.getSubscriberRoleIds().contains(value.toLowerCase().trim())) {
                            a.getSubscriberRoleIds().remove(value.toLowerCase().trim());

                            return Mono.when(deleteUserMessage, deleteCreatorMessage)
                                .then(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a, settings))
                                .flatMap(em -> Messages.sendMessage(
                                    Messages.getMessage("Creator.Announcement.Unsubscribe.Other.Success", "%value%",
                                        value.toLowerCase(), settings), em, event))
                                .doOnNext(a::setCreatorMessage);
                        } else {
                            return Mono.when(deleteUserMessage, deleteCreatorMessage)
                                .then(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a, settings))
                                .flatMap(em -> Messages.sendMessage(
                                    Messages.getMessage("Creator.Announcement.Unsubscribe.Other.Not", "%value%",
                                        value.toLowerCase(), settings), event)
                                )
                                .doOnNext(a::setCreatorMessage);
                        }
                    } else {
                        //Well, now we check if a user exists, then if not, we check for a role that exists...
                        return event.getGuild().flatMap(guild ->
                            UserUtils.getUser(value, guild).flatMap(mem -> {
                                if (a.getSubscriberUserIds().contains(mem.getId().asString())) {
                                    a.getSubscriberUserIds().remove(mem.getId().asString());

                                    return Mono.when(deleteUserMessage, deleteCreatorMessage)
                                        .then(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a, settings))
                                        .flatMap(em -> Messages.sendMessage(
                                            Messages.getMessage("Creator.Announcement.Unsubscribe.Other.Success",
                                                "%value%", mem.getUsername(), settings), em, event))
                                        .doOnNext(msg -> a.setCreatorMessage(msg));
                                } else {
                                    return Mono.when(deleteUserMessage, deleteCreatorMessage)
                                        .then(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a, settings))
                                        .flatMap(em -> Messages.sendMessage(
                                            Messages.getMessage("Creator.Announcement.Unsubscribe.Other.Not",
                                                "%value%", mem.getUsername(), settings), em, event))
                                        .doOnNext(msg -> a.setCreatorMessage(msg));
                                }
                            }).switchIfEmpty(RoleUtils.getRole(value, guild)
                                .flatMap(role -> {
                                    if (!a.getSubscriberRoleIds().contains(role.getId().asString())) {
                                        a.getSubscriberRoleIds().remove(role.getId().asString());

                                        return Mono.when(deleteUserMessage, deleteCreatorMessage)
                                            .then(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a, settings))
                                            .flatMap(em -> Messages.sendMessage(
                                                Messages.getMessage("Creator.Announcement.Unsubscribe.Other.Success",
                                                    "%value%", role.getName(), settings), em, event))
                                            .doOnNext(msg -> a.setCreatorMessage(msg));
                                    } else {
                                        return Mono.when(deleteUserMessage, deleteCreatorMessage)
                                            .then(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a, settings))
                                            .flatMap(em -> Messages.sendMessage(
                                                Messages.getMessage("Creator.Announcement.Unsubscribe.Other.Not",
                                                    "%value%", role.getName(), settings), em, event))
                                            .doOnNext(msg -> a.setCreatorMessage(msg));
                                    }
                                })
                            ));
                    }
                } else {
                    return Messages.sendMessage(
                        Messages.getMessage("Creator.Announcement.Unsubscribe.Self.Specify", settings), event);
                }
            } else {
                return AnnouncementUtils.announcementExists(value, settings.getGuildID())
                    .flatMap(exists -> {
                        if (exists) {
                            return DatabaseManager.INSTANCE.getAnnouncement(UUID.fromString(value), settings.getGuildID())
                                .flatMap(a -> event.getMessage().getAuthorAsMember()
                                    .doOnNext(u -> a.getSubscriberUserIds().remove(u.getId().asString()))
                                    .flatMap(u -> DatabaseManager.INSTANCE.updateAnnouncement(a)))
                                .then(Messages.sendMessage(
                                    Messages.getMessage("Creator.Announcement.Unsubscribe.Self.Success", settings),
                                    event));

                        } else {
                            return Messages.sendMessage(
                                Messages.getMessage("Creator.Announcement.CannotFind.Announcement", settings), event);
                        }
                    });
            }
        }).then();
    }

    private Mono<Void> moduleUnsubscribeRewriteArgsThree(String[] args, MessageCreateEvent event, GuildSettings settings) {
        return event.getGuild().flatMap(guild -> {
            //First we check if the first arg is an announcement ID, because we don't want to stop people from unsubbing,
            //while a staff member is creating an announcement.
            if (args[1].length() > 32) {
                return DatabaseManager.INSTANCE.getAnnouncement(UUID.fromString(args[1]), settings.getGuildID()).flatMap(a -> {
                    //We have the announcement, now lets handle subscribing all of those users/roles...
                    List<String> toLookFor = Arrays.asList(args).subList(2, args.length);

                    Mono<List<Member>> membersMono = Flux.fromIterable(toLookFor)
                        .flatMap(str -> UserUtils.getUser(str, guild))
                        .collectList()
                        .defaultIfEmpty(new ArrayList<>()); //So the zip doesn't fail..

                    Mono<List<Role>> rolesMono = Flux.fromIterable(toLookFor)
                        .flatMap(str -> RoleUtils.getRole(str, guild))
                        .collectList()
                        .defaultIfEmpty(new ArrayList<>()); //So the zip doesn't fail..

                    return Mono.zip(membersMono, rolesMono).flatMap(TupleUtils.function((members, roles) -> {
                        List<String> subbedMembers = new ArrayList<>();
                        List<String> subbedRoles = new ArrayList<>();

                        for (Member m : members) {
                            if (a.getSubscriberUserIds().contains(m.getId().asString())) {
                                a.getSubscriberUserIds().remove(m.getId().asString());
                                subbedMembers.add(m.getDisplayName());
                            }
                        }

                        for (Role r : roles) {
                            if (a.getSubscriberRoleIds().contains(r.getId().asString())) {
                                a.getSubscriberRoleIds().remove(r.getId().asString());
                                subbedRoles.add(r.getName());
                            }
                        }

                        String subMemString = subbedMembers.toString().replace("[", "").replace("]", "");
                        String subRoleString = subbedRoles.toString().replace("[", "").replace("]", "");
                        var embed = EmbedCreateSpec.builder()
                            .author("DisCal", BotSettings.BASE_URL.get(), GlobalVal.getIconUrl())
                            .color(GlobalVal.getDiscalColor())
                            .title("Unsubscribed the Following")
                            .description(Messages.getMessage("Embed.Announcement.Unsubscribe.Users", "%users%",
                                subMemString, settings)
                                + GlobalVal.getLineBreak()
                                + Messages.getMessage("Embed.Announcement.Unsubscribe.Roles", "%roles%",
                                subRoleString, settings))
                            .footer(Messages.getMessage("Embed.Announcement.Unsubscribe.Footer", "%id%",
                                a.getId().toString(), settings), null)
                            .build();


                        return DatabaseManager.INSTANCE.updateAnnouncement(a).thenReturn(embed);
                    }))
                        .flatMap(em -> Messages.sendMessage(em, event));
                })
                    .switchIfEmpty(Messages.sendMessage(
                        Messages.getMessage("Creator.Announcement.CannotFind.Announcement", settings), event))
                    .onErrorResume(IllegalArgumentException.class, e ->
                        Messages.sendMessage(Messages.getMessage("Creator.Announcement.CannotFind.Announcement",
                            settings), event));
            } else if (AnnouncementCreator.getCreator().hasAnnouncement(settings.getGuildID())) {
                Announcement a = AnnouncementCreator.getCreator().getAnnouncement(settings.getGuildID());

                Mono<Void> deleteUserMessage = Messages.deleteMessage(event);
                Mono<Void> deleteCreatorMessage = Messages.deleteMessage(a.getCreatorMessage());

                //Okay, now lets just go about subscribing all of the users/roles...
                List<String> toLookFor = Arrays.asList(args).subList(1, args.length);

                Mono<List<Member>> membersMono = Flux.fromIterable(toLookFor)
                    .flatMap(str -> UserUtils.getUser(str, guild))
                    .collectList()
                    .defaultIfEmpty(new ArrayList<>()); //So the zip doesn't fail..

                Mono<List<Role>> rolesMono = Flux.fromIterable(toLookFor)
                    .flatMap(str -> RoleUtils.getRole(str, guild))
                    .collectList()
                    .defaultIfEmpty(new ArrayList<>()); //So the zip doesn't fail..

                return Mono.zip(membersMono, rolesMono).flatMap(TupleUtils.function((members, roles) -> {
                    List<String> subbedMembers = new ArrayList<>();
                    List<String> subbedRoles = new ArrayList<>();

                    for (Member m : members) {
                        if (a.getSubscriberUserIds().contains(m.getId().asString())) {
                            a.getSubscriberUserIds().remove(m.getId().asString());
                            subbedMembers.add(m.getDisplayName());
                        }
                    }

                    for (Role r : roles) {
                        if (a.getSubscriberRoleIds().contains(r.getId().asString())) {
                            a.getSubscriberRoleIds().remove(r.getId().asString());
                            subbedRoles.add(r.getName());
                        }
                    }

                    String subMemString = subbedMembers.toString().replace("[", "").replace("]", "");
                    String subRoleString = subbedRoles.toString().replace("[", "").replace("]", "");
                    var embed = EmbedCreateSpec.builder()
                        .author("DisCal", BotSettings.BASE_URL.get(), GlobalVal.getIconUrl())
                        .color(GlobalVal.getDiscalColor())
                        .title("Unsubscribed the Following")
                        .description(Messages.getMessage("Embed.Announcement.Unsubscribe.Users", "%users%",
                            subMemString, settings)
                            + GlobalVal.getLineBreak()
                            + Messages.getMessage("Embed.Announcement.Unsubscribe.Roles", "%roles%",
                            subRoleString, settings))
                        .footer(Messages.getMessage("Embed.Announcement.Unsubscribe.Footer", "%id%",
                            a.getId().toString(), settings), null)
                        .build();


                    return Mono.when(deleteUserMessage, deleteCreatorMessage).thenReturn(embed);
                }))
                    .flatMap(em -> Messages.sendMessage(em, event))
                    .doOnNext(a::setCreatorMessage);
            } else {
                //No announcement being created/edited, and no ID is specified, so we can't sub anyone.
                return Messages.sendMessage(Messages.getMessage("Creator.Announcement.Unsubscribe.Other.Specify", settings), event);
            }
        }).then();
    }

    private Mono<Void> moduleUnsubscribeRewrite(String[] args, MessageCreateEvent event, GuildSettings settings) {
        return Mono.defer(() -> {
            if (args.length == 1)
                return this.moduleUnsubscribeRewriteArgsOne(event, settings);
            else if (args.length == 2)
                return this.moduleUnsubscribeRewriteArgsTwo(args, event, settings);
            else
                return this.moduleUnsubscribeRewriteArgsThree(args, event, settings);
        });
    }

    private Mono<Void> moduleType(String[] args, MessageCreateEvent event, GuildSettings settings) {
        return Mono.defer(() -> {
            if (AnnouncementCreator.getCreator().hasAnnouncement(settings.getGuildID())) {
                Announcement a = AnnouncementCreator.getCreator().getAnnouncement(settings.getGuildID());

                Mono<Void> deleteUserMessage = Messages.deleteMessage(event);
                Mono<Void> deleteCreatorMessage = Messages.deleteMessage(a.getCreatorMessage());

                if (args.length == 2) {
                    if (AnnouncementType.Companion.isValid(args[1])) {
                        AnnouncementType type = AnnouncementType.Companion.fromValue(args[1]);
                        a.setType(type);

                        //Get the correct message to send depending on the type...
                        String msg = switch (type) {
                            case SPECIFIC -> Messages.getMessage("Creator.Announcement.Type.Success.Specific", settings);
                            case UNIVERSAL -> Messages.getMessage("Creator.Announcement.Type.Success.Universal", settings);
                            case COLOR -> Messages.getMessage("Creator.Announcement.Type.Success.Color", settings);
                            case RECUR -> Messages.getMessage("Creator.Announcement.Type.Success.Recur", settings);
                            default -> "Type message somehow not handled. Ugh. Contact the devs please";
                        };

                        //Okay, now we can return with the message...
                        return Mono.when(deleteUserMessage, deleteCreatorMessage)
                            .then(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a, settings))
                            .flatMap(em -> Messages.sendMessage(msg, em, event))
                            .doOnNext(a::setCreatorMessage);
                    } else {
                        //Not valid type...
                        return Mono.when(deleteUserMessage, deleteCreatorMessage)
                            .then(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a, settings))
                            .flatMap(em -> Messages.sendMessage(
                                Messages.getMessage("Creator.Announcement.Type.Specify", settings), em, event))
                            .doOnNext(a::setCreatorMessage);
                    }
                } else {
                    return Mono.when(deleteUserMessage, deleteCreatorMessage)
                        .then(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a, settings))
                        .flatMap(em -> Messages.sendMessage(
                            Messages.getMessage("Creator.Announcement.Type.Specify", settings), em, event))
                        .doOnNext(a::setCreatorMessage);
                }
            } else {
                //Not in creator.
                return Messages.sendMessage(Messages.getMessage("Creator.Announcement.NotInit", settings), event);
            }
        }).then();
    }

    private Mono<Void> moduleHours(String[] args, MessageCreateEvent event, GuildSettings settings) {
        return Mono.defer(() -> {
            if (AnnouncementCreator.getCreator().hasAnnouncement(settings.getGuildID())) {
                Announcement a = AnnouncementCreator.getCreator().getAnnouncement(settings.getGuildID());

                Mono<Void> deleteUserMessage = Messages.deleteMessage(event);
                Mono<Void> deleteCreatorMessage = Messages.deleteMessage(a.getCreatorMessage());

                if (args.length == 2) {
                    int hours;
                    try {
                        hours = Math.abs(Integer.parseInt(args[1]));
                    } catch (NumberFormatException ignore) {
                        return Mono.when(deleteUserMessage, deleteCreatorMessage)
                            .then(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a, settings))
                            .flatMap(em -> Messages.sendMessage(
                                Messages.getMessage("Creator.Announcement.Hours.NotInt", settings), em, event))
                            .doOnNext(a::setCreatorMessage);
                    }
                    a.setHoursBefore(hours);

                    return Mono.when(deleteUserMessage, deleteCreatorMessage)
                        .then(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a, settings))
                        .flatMap(em -> Messages.sendMessage(
                            Messages.getMessage("Creator.Announcement.Hours.Success.New", settings), em, event))
                        .doOnNext(a::setCreatorMessage);
                } else {
                    return Mono.when(deleteUserMessage, deleteCreatorMessage)
                        .then(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a, settings))
                        .flatMap(em -> Messages.sendMessage(
                            Messages.getMessage("Creator.Announcement.Hours.Specify", settings), em, event))
                        .doOnNext(a::setCreatorMessage);
                }
            } else {
                return Messages.sendMessage(Messages.getMessage("Creator.Announcement.NotInit", settings), event);
            }
        }).then();
    }

    private Mono<Void> moduleMinutes(String[] args, MessageCreateEvent event, GuildSettings settings) {
        return Mono.defer(() -> {
            if (AnnouncementCreator.getCreator().hasAnnouncement(settings.getGuildID())) {
                Announcement a = AnnouncementCreator.getCreator().getAnnouncement(settings.getGuildID());

                Mono<Void> deleteUserMessage = Messages.deleteMessage(event);
                Mono<Void> deleteCreatorMessage = Messages.deleteMessage(a.getCreatorMessage());

                if (args.length == 2) {
                    int minutes;
                    try {
                        minutes = Math.abs(Integer.parseInt(args[1]));
                    } catch (NumberFormatException ignore) {
                        return Mono.when(deleteUserMessage, deleteCreatorMessage)
                            .then(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a, settings))
                            .flatMap(em -> Messages.sendMessage(
                                Messages.getMessage("Creator.Announcement.Minutes.NotInt", settings), em, event))
                            .doOnNext(a::setCreatorMessage);
                    }
                    a.setMinutesBefore(minutes);

                    return Mono.when(deleteUserMessage, deleteCreatorMessage)
                        .then(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a, settings))
                        .flatMap(em -> Messages.sendMessage(
                            Messages.getMessage("Creator.Announcement.Minutes.Success.New", settings), em, event))
                        .doOnNext(a::setCreatorMessage);
                } else {
                    return Mono.when(deleteUserMessage, deleteCreatorMessage)
                        .then(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a, settings))
                        .flatMap(em -> Messages.sendMessage(
                            Messages.getMessage("Creator.Announcement.Minutes.Specify", settings), em, event))
                        .doOnNext(a::setCreatorMessage);
                }
            } else {
                return Messages.sendMessage(Messages.getMessage("Creator.Announcement.NotInit", settings), event);
            }
        }).then();
    }

    private Mono<Void> moduleList(String[] args, MessageCreateEvent event, GuildSettings settings) {
        return Mono.defer(() -> {
            if (AnnouncementCreator.getCreator().hasAnnouncement(settings.getGuildID())) {
                Announcement a = AnnouncementCreator.getCreator().getAnnouncement(settings.getGuildID());

                Mono<Void> deleteUserMessage = Messages.deleteMessage(event);
                Mono<Void> deleteCreatorMessage = Messages.deleteMessage(a.getCreatorMessage());

                return Mono.when(deleteUserMessage, deleteCreatorMessage)
                    .then(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a, settings))
                    .flatMap(em -> Messages.sendMessage(
                        Messages.getMessage("Creator.Announcement.List.InCreator", settings), em, event))
                    .doOnNext(a::setCreatorMessage);
            } else if (args.length == 2) {
                if ("all".equalsIgnoreCase(args[1])) {
                    return DatabaseManager.INSTANCE.getAnnouncements(settings.getGuildID()).flatMap(announcements ->
                        Messages.sendMessage(Messages.getMessage("Creator.Announcement.List.All", "%amount%",
                            announcements.size() + "", settings), event)
                            .then(Flux.fromIterable(announcements)
                                .flatMap(a -> AnnouncementMessageFormatter.getCondensedAnnouncementEmbed(a, settings))
                                .flatMap(em -> Messages.sendMessage(em, event))
                                .then()));
                } else {
                    //List specific amount of announcements...
                    int amount;
                    try {
                        amount = Integer.parseInt(args[1]);
                    } catch (NumberFormatException ignore) {
                        return Messages.sendMessage(Messages.getMessage("Creator.Announcement.List.NotInt", settings),
                            event);
                    }

                    return DatabaseManager.INSTANCE.getAnnouncements(settings.getGuildID()).flatMap(allAnnouncements -> {
                        List<Announcement> toPost; //We only post the amount listed...
                        if (allAnnouncements.size() > amount)
                            toPost = allAnnouncements.subList(0, amount);
                        else
                            toPost = allAnnouncements;

                        return Messages.sendMessage(Messages.getMessage("Creator.Announcement.List.Some", "%amount%",
                            toPost.size() + "", settings), event)
                            .then(Flux.fromIterable(toPost)
                                .flatMap(a -> AnnouncementMessageFormatter.getCondensedAnnouncementEmbed(a, settings))
                                .flatMap(em -> Messages.sendMessage(em, event))
                                .then());
                    });
                }
            } else {
                return Messages.sendMessage(Messages.getMessage("Creator.Announcement.List.Specify", settings), event);
            }
        }).then();
    }

    private Mono<Void> moduleEvent(String[] args, MessageCreateEvent event, GuildSettings settings) {
        return Mono.defer(() -> {
            if (AnnouncementCreator.getCreator().hasAnnouncement(settings.getGuildID())) {
                Announcement a = AnnouncementCreator.getCreator().getAnnouncement(settings.getGuildID());

                Mono<Void> deleteUserMessage = Messages.deleteMessage(event);
                Mono<Void> deleteCreatorMessage = Messages.deleteMessage(a.getCreatorMessage());

                if (args.length == 2) {
                    if (a.getType().equals(AnnouncementType.SPECIFIC)) {
                        return EventUtils.eventExists(settings, args[1]).flatMap(exists -> {
                            if (exists) {
                                a.setEventId(args[1]);

                                return Mono.when(deleteUserMessage, deleteCreatorMessage)
                                    .then(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a, settings))
                                    .flatMap(em -> Messages.sendMessage(
                                        Messages.getMessage("Creator.Announcement.Event.Success.New", settings),
                                        em, event))
                                    .doOnNext(a::setCreatorMessage);
                            } else {
                                return Mono.when(deleteUserMessage, deleteCreatorMessage)
                                    .then(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a, settings))
                                    .flatMap(em -> Messages.sendMessage(
                                        Messages.getMessage("Creator.Announcement.CannotFind.Event", settings),
                                        em, event))
                                    .doOnNext(a::setCreatorMessage);
                            }
                        });
                    } else if (a.getType().equals(AnnouncementType.RECUR)) {
                        return EventUtils.eventExists(settings, args[1]).flatMap(exists -> {
                            if (exists) {
                                String value = args[1];
                                if (args[1].contains("_"))
                                    value = args[1].split("_")[0];

                                a.setEventId(value);

                                return Mono.when(deleteUserMessage, deleteCreatorMessage)
                                    .then(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a, settings))
                                    .flatMap(em -> Messages.sendMessage(
                                        Messages.getMessage("Creator.Announcement.Event.Success.New", settings),
                                        em, event))
                                    .doOnNext(a::setCreatorMessage);
                            } else {
                                return Mono.when(deleteUserMessage, deleteCreatorMessage)
                                    .then(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a, settings))
                                    .flatMap(em -> Messages.sendMessage(
                                        Messages.getMessage("Creator.Announcement.CannotFind.Event", settings),
                                        em, event))
                                    .doOnNext(a::setCreatorMessage);
                            }
                        });
                    } else {
                        return Mono.when(deleteUserMessage, deleteCreatorMessage)
                            .then(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a, settings))
                            .flatMap(em -> Messages.sendMessage(
                                Messages.getMessage("Creator.Announcement.Event.Failure.Type", settings), em, event))
                            .doOnNext(a::setCreatorMessage);
                    }
                } else {
                    return Mono.when(deleteUserMessage, deleteCreatorMessage)
                        .then(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a, settings))
                        .flatMap(em -> Messages.sendMessage(
                            Messages.getMessage("Creator.Announcement.Event.Specify", settings), em, event))
                        .doOnNext(a::setCreatorMessage);
                }
            } else {
                return Messages.sendMessage(Messages.getMessage("Creator.Announcement.NotInit", settings), event);
            }
        }).then();
    }

    private Mono<Void> moduleInfo(String[] args, MessageCreateEvent event, GuildSettings settings) {
        return Mono.defer(() -> {
            if (AnnouncementCreator.getCreator().hasAnnouncement(settings.getGuildID())) {
                Announcement a = AnnouncementCreator.getCreator().getAnnouncement(settings.getGuildID());

                Mono<Void> deleteUserMessage = Messages.deleteMessage(event);
                Mono<Void> deleteCreatorMessage = Messages.deleteMessage(a.getCreatorMessage());

                if (args.length >= 2) {
                    a.setInfo(GeneralUtils.getContent(args, 1));

                    return Mono.when(deleteUserMessage, deleteCreatorMessage)
                        .then(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a, settings))
                        .flatMap(em -> Messages.sendMessage(
                            Messages.getMessage("Creator.Announcement.Info.Success.New", settings), em, event))
                        .doOnNext(a::setCreatorMessage);
                } else {
                    return Mono.when(deleteUserMessage, deleteCreatorMessage)
                        .then(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a, settings))
                        .flatMap(em -> Messages.sendMessage(
                            Messages.getMessage("Creator.Announcement.Info.Specify", settings), em, event))
                        .doOnNext(a::setCreatorMessage);
                }
            } else {
                return Messages.sendMessage(Messages.getMessage("Creator.Announcement.NotInit", settings), event);
            }
        }).then();
    }

    private Mono<Void> moduleEnable(String[] args, MessageCreateEvent event, GuildSettings settings) {
        return Mono.defer(() -> {
            if (AnnouncementCreator.getCreator().hasAnnouncement(settings.getGuildID())) {
                Announcement a = AnnouncementCreator.getCreator().getAnnouncement(settings.getGuildID());

                Mono<Void> deleteUserMessage = Messages.deleteMessage(event);
                Mono<Void> deleteCreatorMessage = Messages.deleteMessage(a.getCreatorMessage());

                return Mono.when(deleteUserMessage, deleteCreatorMessage)
                    .then(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a, settings))
                    .flatMap(em -> Messages.sendMessage(
                        Messages.getMessage("Announcement.Enable.Creator", settings), em, event))
                    .doOnNext(a::setCreatorMessage);
            } else if (args.length == 2) {
                return AnnouncementUtils.announcementExists(args[1], settings.getGuildID()).flatMap(exists -> {
                    if (exists) {
                        UUID id = UUID.fromString(args[1]);
                        AtomicBoolean en = new AtomicBoolean(false); //This has got to be tested...
                        return DatabaseManager.INSTANCE.getAnnouncement(id, settings.getGuildID())
                            .doOnNext(a -> a.setEnabled(!a.getEnabled()))
                            .doOnNext(a -> en.set(a.getEnabled()))
                            .flatMap(DatabaseManager.INSTANCE::updateAnnouncement)
                            .map(i -> Messages.getMessage("Announcement.Enable.Success", "%value%", en.get() + "", settings))
                            .flatMap(msg -> Messages.sendMessage(msg, event));
                    } else {
                        return Messages.sendMessage(
                            Messages.getMessage("Creator.Announcement.CannotFind.Announcement", settings), event);
                    }
                });
            } else {
                return Messages.sendMessage(Messages.getMessage("Announcement.Enable.Specify", settings), event);
            }
        }).then();
    }

    private Mono<Void> modulePublish(MessageCreateEvent event, GuildSettings settings) {
        return Mono.defer(() -> {
            if (AnnouncementCreator.getCreator().hasAnnouncement(settings.getGuildID())) {
                Announcement a = AnnouncementCreator.getCreator().getAnnouncement(settings.getGuildID());

                Mono<Void> deleteUserMessage = Messages.deleteMessage(event);
                Mono<Void> deleteCreatorMessage = Messages.deleteMessage(a.getCreatorMessage());

                a.setPublish(!a.getPublish());
                return Mono.when(deleteUserMessage, deleteCreatorMessage)
                    .then(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a, settings))
                    .flatMap(em -> Messages.sendMessage(em, event))
                    .doOnNext(a::setCreatorMessage);
            } else {
                return Messages.sendMessage(Messages.getMessage("Creator.Announcement.NotInit", settings), event);
            }
        }).then();
    }

    private Mono<Void> moduleChannel(String[] args, MessageCreateEvent event, GuildSettings settings) {
        return Mono.defer(() -> {
            if (AnnouncementCreator.getCreator().hasAnnouncement(settings.getGuildID())) {
                Announcement a = AnnouncementCreator.getCreator().getAnnouncement(settings.getGuildID());

                Mono<Void> deleteUserMessage = Messages.deleteMessage(event);
                Mono<Void> deleteCreatorMessage = Messages.deleteMessage(a.getCreatorMessage());

                if (args.length == 2) {
                    return ChannelUtils.channelExists(args[1], event).flatMap(exists -> {
                        if (exists) {
                            return ChannelUtils.getChannelFromNameOrId(args[1], event)
                                .doOnNext(c -> a.setAnnouncementChannelId(c.getId().asString()))
                                .then(Mono.when(deleteUserMessage, deleteCreatorMessage))
                                .then(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a, settings))
                                .flatMap(e -> Messages.sendMessage(
                                    Messages.getMessage("Creator.Announcement.Channel.Success.New", settings), e, event))
                                .doOnNext(a::setCreatorMessage);
                        } else {
                            return Mono.when(deleteUserMessage, deleteCreatorMessage)
                                .then(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a, settings))
                                .flatMap(em -> Messages.sendMessage(
                                    Messages.getMessage("Creator.Announcement.CannotFind.Channel", settings), em, event))
                                .doOnNext(a::setCreatorMessage);
                        }
                    });
                } else {
                    return Mono.when(deleteUserMessage, deleteCreatorMessage)
                        .then(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a, settings))
                        .flatMap(em -> Messages.sendMessage(
                            Messages.getMessage("Creator.Announcement.Channel.Specify", settings), em, event))
                        .doOnNext(a::setCreatorMessage);
                }
            } else {
                return Messages.sendMessage(Messages.getMessage("Creator.Announcement.NotInit", settings), event);
            }
        }).then();
    }

    private Mono<Void> moduleColor(String[] args, MessageCreateEvent event, GuildSettings settings) {
        return Mono.defer(() -> {
            if (AnnouncementCreator.getCreator().hasAnnouncement(settings.getGuildID())) {
                Announcement a = AnnouncementCreator.getCreator().getAnnouncement(settings.getGuildID());

                Mono<Void> deleteUserMessage = Messages.deleteMessage(event);
                Mono<Void> deleteCreatorMessage = Messages.deleteMessage(a.getCreatorMessage());

                if (a.getType().equals(AnnouncementType.COLOR)) {
                    if (args.length == 2) {
                        if (EventColor.Companion.exists(args[1])) {
                            a.setEventColor(EventColor.Companion.fromNameOrHexOrId(args[1]));

                            return Mono.when(deleteUserMessage, deleteCreatorMessage)
                                .then(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a, settings))
                                .flatMap(em -> Messages.sendMessage(
                                    Messages.getMessage("Creator.Announcement.Color.Success.New", settings), em, event))
                                .doOnNext(a::setCreatorMessage);
                        } else {
                            return Mono.when(deleteUserMessage, deleteCreatorMessage)
                                .then(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a, settings))
                                .flatMap(em -> Messages.sendMessage(
                                    Messages.getMessage("Creator.Announcement.Color.Specify", settings), em, event))
                                .doOnNext(a::setCreatorMessage);
                        }
                    } else {
                        return Mono.when(deleteUserMessage, deleteCreatorMessage)
                            .then(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a, settings))
                            .flatMap(em -> Messages.sendMessage(
                                Messages.getMessage("Creator.Announcement.Color.Specify", settings), em, event))
                            .doOnNext(a::setCreatorMessage);
                    }
                } else {
                    //type is not color, can't set color
                    return Mono.when(deleteUserMessage, deleteCreatorMessage)
                        .then(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a, settings))
                        .flatMap(em -> Messages.sendMessage(
                            Messages.getMessage("Creator.Announcement.Color.Failure.Type", settings), em, event))
                        .doOnNext(a::setCreatorMessage);
                }
            } else {
                return Messages.sendMessage(Messages.getMessage("Creator.Announcement.NotInit", settings), event);
            }
        }).then();
    }

    private Mono<Void> moduleCopy(String[] args, MessageCreateEvent event, GuildSettings settings) {
        return Mono.defer(() -> {
            if (!AnnouncementCreator.getCreator().hasAnnouncement(settings.getGuildID())) {
                if (args.length == 2) {
                    return AnnouncementUtils.announcementExists(args[1], settings.getGuildID()).flatMap(exists -> {
                        if (exists) {
                            return AnnouncementCreator.getCreator().init(event, args[1], settings)
                                .switchIfEmpty(Messages.sendMessage(
                                    Messages.getMessage("Notification.Error.Unknown", settings), event)
                                    .then(Mono.empty()));
                        } else {
                            return Messages.sendMessage(
                                Messages.getMessage("Creator.Announcement.CannotFind.Announcement", settings), event);
                        }
                    });
                } else {
                    return Messages.sendMessage(Messages.getMessage("Creator.Announcement.Copy.Specify", settings), event);
                }
            } else {
                Announcement a = AnnouncementCreator.getCreator().getAnnouncement(settings.getGuildID());

                Mono<Void> deleteUserMessage = Messages.deleteMessage(event);
                Mono<Void> deleteCreatorMessage = Messages.deleteMessage(a.getCreatorMessage());

                return Mono.when(deleteUserMessage, deleteCreatorMessage)
                    .then(AnnouncementMessageFormatter.getFormatAnnouncementEmbed(a, settings))
                    .flatMap(em -> Messages.sendMessage(
                        Messages.getMessage("Creator.Announcement.AlreadyInit", settings), em, event))
                    .doOnNext(a::setCreatorMessage);
            }
        }).then();
    }
}
