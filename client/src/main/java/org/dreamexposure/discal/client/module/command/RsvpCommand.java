package org.dreamexposure.discal.client.module.command;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Role;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Image;
import org.dreamexposure.discal.client.DisCalClient;
import org.dreamexposure.discal.client.message.Messages;
import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.object.BotSettings;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.command.CommandInfo;
import org.dreamexposure.discal.core.object.event.RsvpData;
import org.dreamexposure.discal.core.utils.*;
import reactor.core.publisher.Mono;
import reactor.function.TupleUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by Nova Fox on 8/31/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class RsvpCommand implements Command {
    /**
     * Gets the command this Object is responsible for.
     *
     * @return The command this Object is responsible for.
     */
    @Override
    public String getCommand() {
        return "rsvp";
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
            "rsvp",
            "Confirms attendance to an event",
            "!rsvp <subCommand> <eventId>"
        );

        info.getSubCommands().put("onTime", "Marks you are going to event");
        info.getSubCommands().put("late", "Marks that you will be late to event");
        info.getSubCommands().put("not", "Marks that you are NOT going to event");
        info.getSubCommands().put("unsure", "Marks that you may or may not go to event");
        info.getSubCommands().put("remove", "Removes your RSVP from the event");
        info.getSubCommands().put("list", "Lists who has RSVPed to event");
        info.getSubCommands().put("limit", "Sets the amount of people that can RSVP to the event, -1 to disable");
        info.getSubCommands().put("role", "Sets the  role people will get when they RSVP to the event, 'none' to " +
            "remove");

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
            if (args.length > 0) {
                switch (args[0].toLowerCase()) {
                    case "ontime":
                        return this.moduleGoing(args, event, settings);
                    case "late":
                        return this.moduleGoingLate(args, event, settings);
                    case "not":
                        return this.moduleNotGoing(args, event, settings);
                    case "unsure":
                        return this.moduleUnsure(args, event, settings);
                    case "remove":
                        return this.moduleRemove(args, event, settings);
                    case "list":
                        return this.moduleList(args, event, settings);
                    case "limit":
                        return PermissionChecker.hasDisCalRole(event, settings)
                            .flatMap(has -> {
                                if (has)
                                    return this.moduleLimit(args, event, settings);
                                else
                                    return Messages.sendMessage(Messages.getMessage("Notification.Perm.CONTROL_ROLE", settings), event);
                            });
                    case "role":
                        if (settings.getPatronGuild() || settings.getDevGuild()) {
                            return PermissionChecker.hasDisCalRole(event, settings)
                                .flatMap(has -> {
                                    if (has)
                                        return this.moduleRole(args, event, settings);
                                    else
                                        return Messages.sendMessage(Messages.getMessage("Notification.Perm" +
                                            ".CONTROL_ROLE", settings), event);
                                });
                        } else {
                            return Messages.sendMessage(Messages.getMessage("Notification.Patron", settings), event);
                        }
                    default:
                        return Messages.sendMessage(Messages.getMessage("Notification.Args.InvalidSubCmd", settings), event);
                }
            } else {
                return Messages.sendMessage(Messages.getMessage("Notification.Args.Few", settings), event);
            }
        }).then();
    }

    private Mono<Void> moduleGoing(final String[] args, final MessageCreateEvent event, final GuildSettings settings) {
        return Mono.justOrEmpty(event.getMember())
            .flatMap(mem -> {
                if (args.length == 2) {
                    return Mono.just(args[1]).flatMap(eventId -> EventUtils.eventExists(settings, eventId)
                        .flatMap(exists -> {
                            if (exists) {
                                return TimeUtils.isInPast(eventId, settings).flatMap(inPast -> {
                                    if (!inPast) {
                                        return DatabaseManager.INSTANCE.getRsvpData(settings.getGuildID(), eventId)
                                            .filter(data -> data.hasRoom(mem.getId().asString()))
                                            .flatMap(data -> data.removeCompletely(mem).thenReturn(data))
                                            .flatMap(data -> data.addGoingOnTime(mem).thenReturn(data))
                                            .flatMap(data -> DatabaseManager.INSTANCE.updateRsvpData(data)
                                                .then(this.getRsvpEmbed(data, settings))
                                                .flatMap(embed -> Messages.sendMessage(
                                                    Messages.getMessage("RSVP.going.success", settings), embed, event))
                                            ).switchIfEmpty(Messages.sendMessage("You cannot RSVP to the event as the" +
                                                " max number of people are already attending", event)
                                            );
                                    } else {
                                        return Messages
                                            .sendMessage(Messages.getMessage("Notifications.Event.InPast", settings), event);
                                    }
                                });
                            } else {
                                return Messages
                                    .sendMessage(Messages.getMessage("Notifications.Event.NotExist", settings), event);
                            }
                        })
                    );
                } else {
                    return Messages.sendMessage(Messages.getMessage("RSVP.going.specify", settings), event);
                }
            }).then();
    }

    private Mono<Void> moduleGoingLate(final String[] args, final MessageCreateEvent event, final GuildSettings settings) {
        return Mono.justOrEmpty(event.getMember())
            .flatMap(mem -> {
                if (args.length == 2) {
                    return Mono.just(args[1]).flatMap(eventId -> EventUtils.eventExists(settings, eventId)
                        .flatMap(exists -> {
                            if (exists) {
                                return TimeUtils.isInPast(eventId, settings).flatMap(inPast -> {
                                    if (!inPast) {
                                        return DatabaseManager.INSTANCE.getRsvpData(settings.getGuildID(), eventId)
                                            .filter(data -> data.hasRoom(mem.getId().asString()))
                                            .flatMap(data -> data.removeCompletely(mem).thenReturn(data))
                                            .flatMap(data -> data.addGoingLate(mem).thenReturn(data))
                                            .flatMap(data -> DatabaseManager.INSTANCE.updateRsvpData(data)
                                                .then(this.getRsvpEmbed(data, settings))
                                                .flatMap(embed -> Messages.sendMessage(
                                                    Messages.getMessage("RSVP.late.success", settings), embed, event))
                                            ).switchIfEmpty(Messages.sendMessage("You cannot RSVP to the event as the" +
                                                " max number of people are already attending", event)
                                            );
                                    } else {
                                        return Messages
                                            .sendMessage(Messages.getMessage("Notifications.Event.InPast", settings), event);
                                    }
                                });
                            } else {
                                return Messages
                                    .sendMessage(Messages.getMessage("Notifications.Event.NotExist", settings), event);
                            }
                        })
                    );
                } else {
                    return Messages.sendMessage(Messages.getMessage("RSVP.late.specify", settings), event);
                }
            }).then();
    }

    private Mono<Void> moduleNotGoing(final String[] args, final MessageCreateEvent event, final GuildSettings settings) {
        return Mono.justOrEmpty(event.getMember())
            .flatMap(mem -> {
                if (args.length == 2) {
                    return Mono.just(args[1]).flatMap(eventId -> EventUtils.eventExists(settings, eventId)
                        .flatMap(exists -> {
                            if (exists) {
                                return TimeUtils.isInPast(eventId, settings).flatMap(inPast -> {
                                    if (!inPast) {
                                        return DatabaseManager.INSTANCE.getRsvpData(settings.getGuildID(), eventId)
                                            .flatMap(data -> data.removeCompletely(mem).thenReturn(data))
                                            .doOnNext(data -> data.getNotGoing().add(mem.getId().asString()))
                                            .flatMap(data -> DatabaseManager.INSTANCE.updateRsvpData(data)
                                                .then(this.getRsvpEmbed(data, settings))
                                                .flatMap(embed -> Messages.sendMessage(
                                                    Messages.getMessage("RSVP.not.success", settings), embed, event))
                                            );
                                    } else {
                                        return Messages
                                            .sendMessage(Messages.getMessage("Notifications.Event.InPast", settings), event);
                                    }
                                });
                            } else {
                                return Messages
                                    .sendMessage(Messages.getMessage("Notifications.Event.NotExist", settings), event);
                            }
                        })
                    );
                } else {
                    return Messages.sendMessage(Messages.getMessage("RSVP.not.specify", settings), event);
                }
            }).then();
    }

    private Mono<Void> moduleRemove(final String[] args, final MessageCreateEvent event, final GuildSettings settings) {
        return Mono.justOrEmpty(event.getMember())
            .flatMap(mem -> {
                if (args.length == 2) {
                    return Mono.just(args[1]).flatMap(eventId -> EventUtils.eventExists(settings, eventId)
                        .flatMap(exists -> {
                            if (exists) {
                                return TimeUtils.isInPast(eventId, settings).flatMap(inPast -> {
                                    if (!inPast) {
                                        return DatabaseManager.INSTANCE.getRsvpData(settings.getGuildID(), eventId)
                                            .flatMap(data -> data.removeCompletely(mem).thenReturn(data))
                                            .flatMap(data -> DatabaseManager.INSTANCE.updateRsvpData(data)
                                                .then(this.getRsvpEmbed(data, settings))
                                                .flatMap(embed -> Messages.sendMessage(
                                                    Messages.getMessage("RSVP.remove.success", settings), embed, event))
                                            );
                                    } else {
                                        return Messages
                                            .sendMessage(Messages.getMessage("Notifications.Event.InPast", settings), event);
                                    }
                                });
                            } else {
                                return Messages
                                    .sendMessage(Messages.getMessage("Notifications.Event.NotExist", settings), event);
                            }
                        })
                    );
                } else {
                    return Messages.sendMessage(Messages.getMessage("RSVP.remove.specify", settings), event);
                }
            }).then();
    }

    private Mono<Void> moduleUnsure(final String[] args, final MessageCreateEvent event, final GuildSettings settings) {
        return Mono.justOrEmpty(event.getMember())
            .flatMap(mem -> {
                if (args.length == 2) {
                    return Mono.just(args[1]).flatMap(eventId -> EventUtils.eventExists(settings, eventId)
                        .flatMap(exists -> {
                            if (exists) {
                                return TimeUtils.isInPast(eventId, settings).flatMap(inPast -> {
                                    if (!inPast) {
                                        return DatabaseManager.INSTANCE.getRsvpData(settings.getGuildID(), eventId)
                                            .flatMap(data -> data.removeCompletely(mem).thenReturn(data))
                                            .doOnNext(data -> data.getUndecided().add(mem.getId().asString()))
                                            .flatMap(data -> DatabaseManager.INSTANCE.updateRsvpData(data)
                                                .then(this.getRsvpEmbed(data, settings))
                                                .flatMap(embed -> Messages.sendMessage(
                                                    Messages.getMessage("RSVP.unsure.success", settings), embed, event))
                                            );
                                    } else {
                                        return Messages
                                            .sendMessage(Messages.getMessage("Notifications.Event.InPast", settings), event);
                                    }
                                });
                            } else {
                                return Messages
                                    .sendMessage(Messages.getMessage("Notifications.Event.NotExist", settings), event);
                            }
                        })
                    );
                } else {
                    return Messages.sendMessage(Messages.getMessage("RSVP.unsure.specify", settings), event);
                }
            }).then();
    }

    //!rsvp limit <event-id> <limit>
    private Mono<Void> moduleLimit(String[] args, MessageCreateEvent event, GuildSettings settings) {
        return Mono.defer(() -> {
            if (args.length == 3) {
                return Mono.just(args[1]).flatMap(eventId -> EventUtils.eventExists(settings, eventId)
                    .flatMap(exists -> {
                        if (exists) {
                            return TimeUtils.isInPast(eventId, settings).flatMap(inPast -> {
                                if (!inPast) {
                                    //Okay, finally, we can change the limit
                                    try {
                                        int newLimit = Integer.parseInt(args[2]);
                                        return DatabaseManager.INSTANCE.getRsvpData(settings.getGuildID(), eventId)
                                            .doOnNext(data -> data.setLimit(newLimit))
                                            .flatMap(data -> DatabaseManager.INSTANCE.updateRsvpData(data)
                                                .then(this.getRsvpEmbed(data, settings))
                                                .flatMap(embed -> Messages.sendMessage("RSVP Limit changed", embed, event))
                                            );
                                    } catch (NumberFormatException e) {
                                        return Messages.sendMessage(Messages.getMessage("Notification.Args.Value" +
                                            ".Integer", settings), event);
                                    }
                                } else {
                                    return Messages.sendMessage(Messages.getMessage("Notifications.Event.InPast",
                                        settings), event);
                                }
                            });
                        } else {
                            return Messages.sendMessage(Messages.getMessage("Notifications.Event.NotExist", settings),
                                event);
                        }
                    }));
            } else {
                return Messages.sendMessage("Limit command uses the following format: `!rsvp limit <event-id> " +
                    "<#limit>", event);
            }
        }).then();
    }

    //!rsvp role <event-id> <role-name-id-or-mention>
    private Mono<Void> moduleRole(String[] args, MessageCreateEvent event, GuildSettings settings) {
        return Mono.defer(() -> {
            if (args.length == 3) {
                return Mono.just(args[1]).flatMap(eventId -> EventUtils.eventExists(settings, eventId)
                    .flatMap(exists -> {
                        if (exists) {
                            return TimeUtils.isInPast(eventId, settings).flatMap(inPast -> {
                                if (!inPast) {
                                    //Okay, finally, we can change the role
                                    if ("none".equalsIgnoreCase(args[2])) { //Remove RSVP role...
                                        return DatabaseManager.INSTANCE.getRsvpData(settings.getGuildID(), eventId)
                                            .flatMap(data -> data.clearRole(event).thenReturn(data))
                                            .flatMap(data -> DatabaseManager.INSTANCE.updateRsvpData(data).thenReturn(data))
                                            .flatMap(embed -> Messages.sendMessage("Role removed from RSVP", event));
                                    }

                                    return RoleUtils.getRole(args[2], event.getMessage())
                                        .flatMap(role -> DatabaseManager.INSTANCE.getRsvpData(settings.getGuildID(), eventId)
                                            .flatMap(data -> data.setRole(role).thenReturn(data))
                                            .flatMap(data -> DatabaseManager.INSTANCE.updateRsvpData(data).thenReturn(data))
                                            .flatMap(data -> this.getRsvpEmbed(data, settings))
                                            .flatMap(embed -> Messages.sendMessage("Role added to RSVP", embed, event)))
                                        .switchIfEmpty(Messages.sendMessage(Messages.getMessage("DisCal.ControlRole" +
                                            ".Invalid", settings), event));
                                } else {
                                    return Messages.sendMessage(Messages.getMessage("Notifications.Event.InPast",
                                        settings), event);
                                }
                            });
                        } else {
                            return Messages.sendMessage(Messages.getMessage("Notifications.Event.NotExist", settings),
                                event);
                        }
                    }));
            } else {
                return Messages.sendMessage("Role command uses the following format: `!rsvp role <event-id> <role>",
                    event);
            }
        }).then();
    }


    private Mono<Void> moduleList(final String[] args, final MessageCreateEvent event, final GuildSettings settings) {
        return Mono.defer(() -> {
            if (args.length == 2) {
                return Mono.just(args[1]).flatMap(eventId -> EventUtils.eventExists(settings, eventId)
                    .flatMap(exists -> {
                        if (exists) {
                            return DatabaseManager.INSTANCE.getRsvpData(settings.getGuildID(), eventId)
                                .flatMap(data -> this.getRsvpEmbed(data, settings))
                                .flatMap(embed -> Messages.sendMessage(embed, event));
                        } else {
                            return Messages
                                .sendMessage(Messages.getMessage("Notifications.Event.NoExist", settings), event);
                        }
                    }));
            } else {
                return Messages.sendMessage(Messages.getMessage("RSVP.list.specify", settings), event);
            }
        }).then();
    }

    private Mono<Consumer<EmbedCreateSpec>> getRsvpEmbed(final RsvpData data, final GuildSettings settings) {
        final Mono<Guild> guildMono = DisCalClient.getClient().getGuildById(settings.getGuildID()).cache();

        final Mono<List<Member>> onTimeMono = guildMono.flatMap(g -> UserUtils.getUsers(data.getGoingOnTime(), g));
        final Mono<List<Member>> lateMono = guildMono.flatMap(g -> UserUtils.getUsers(data.getGoingLate(), g));
        final Mono<List<Member>> undecidedMono = guildMono.flatMap(g -> UserUtils.getUsers(data.getUndecided(), g));
        final Mono<List<Member>> notGoingMono = guildMono.flatMap(g -> UserUtils.getUsers(data.getNotGoing(), g));
        final Mono<String> rsvpRoleNameMono = guildMono.flatMap(g -> {
            if (data.getRoleId() != null) return g.getRoleById(data.getRoleId()).map(Role::getName);
            else return Mono.just("None");
        });

        return Mono.zip(guildMono, onTimeMono, lateMono, undecidedMono, notGoingMono, rsvpRoleNameMono)
            .map(TupleUtils.function((guild, onTime, late, undecided, notGoing, roleName) -> spec -> {
                if (settings.getBranded())
                    spec.setAuthor(guild.getName(), BotSettings.BASE_URL.get(),
                        guild.getIconUrl(Image.Format.PNG).orElse(GlobalVal.getIconUrl()));
                else
                    spec.setAuthor("DisCal", BotSettings.BASE_URL.get(), GlobalVal.getIconUrl());

                spec.setTitle(Messages.getMessage("Embed.RSVP.List.Title", settings));
                spec.addField("Event ID", data.getEventId(), false);
                if (data.getLimit() > -1)
                    spec.addField("Max Respondents", data.getCurrentCount() + "/" + data.getLimit(), true);
                else
                    spec.addField("Max Respondents", "Unlimited", true);

                spec.addField("Role on RSVP", roleName, true);

                final StringBuilder onTimeBuilder = new StringBuilder();
                for (final Member u : onTime) onTimeBuilder.append(u.getDisplayName()).append(", ");

                final StringBuilder lateBuilder = new StringBuilder();
                for (final Member u : late) lateBuilder.append(u.getDisplayName()).append(", ");

                final StringBuilder unsureBuilder = new StringBuilder();
                for (final Member u : undecided) unsureBuilder.append(u.getDisplayName()).append(", ");

                final StringBuilder notGoingBuilder = new StringBuilder();
                for (final Member u : notGoing) notGoingBuilder.append(u.getDisplayName()).append(", ");

                if (onTimeBuilder.toString().isEmpty())
                    spec.addField("On time", "N/a", true);
                else
                    spec.addField("On Time", onTimeBuilder.toString(), false);

                if (lateBuilder.toString().isEmpty())
                    spec.addField("Late", "N/a", true);
                else
                    spec.addField("Late", lateBuilder.toString(), false);

                if (unsureBuilder.toString().isEmpty())
                    spec.addField("Unsure", "N/a", true);
                else
                    spec.addField("Unsure", unsureBuilder.toString(), false);

                if (notGoingBuilder.toString().isEmpty())
                    spec.addField("Not Going", "N/a", true);
                else
                    spec.addField("Not Going", notGoingBuilder.toString(), false);

                spec.setFooter(Messages.getMessage("Embed.RSVP.List.Footer", settings), null);
                spec.setColor(GlobalVal.getDiscalColor());
            }));
    }
}
