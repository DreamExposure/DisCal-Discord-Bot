package org.dreamexposure.discal.client.module.command;

import org.dreamexposure.discal.client.calendar.CalendarCreator;
import org.dreamexposure.discal.client.message.CalendarMessageFormatter;
import org.dreamexposure.discal.client.message.Messages;
import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.calendar.CalendarData;
import org.dreamexposure.discal.core.object.calendar.PreCalendar;
import org.dreamexposure.discal.core.object.command.CommandInfo;
import org.dreamexposure.discal.core.utils.CalendarUtils;
import org.dreamexposure.discal.core.utils.GeneralUtils;
import org.dreamexposure.discal.core.utils.PermissionChecker;
import org.dreamexposure.discal.core.utils.TimeZoneUtils;

import java.util.ArrayList;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

/**
 * Created by Nova Fox on 1/4/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
//TODO: Basically rewrite this whole class in order to support multi-cal!!!!!
public class CalendarCommand implements Command {
    private final String TIME_ZONE_DB = "http://www.joda.org/joda-time/timezones.html";

    /**
     * Gets the command this Object is responsible for.
     *
     * @return The command this Object is responsible for.
     */
    @Override
    public String getCommand() {
        return "calendar";
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
        aliases.add("cal");
        aliases.add("callador");
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
            "calendar",
            "Used for direct interaction with your DisCal Calendar.",
            "!calendar <subCommand> (value)"
        );

        info.getSubCommands().put("create", "Starts the creation of a new calendar.");
        info.getSubCommands().put("cancel", "Cancels the creator/editor");
        info.getSubCommands().put("view", "Views the calendar in the creator/editor");
        info.getSubCommands().put("review", "Views the calendar in the creator/editor");
        info.getSubCommands().put("confirm", "Confirms and creates/edits the calendar.");
        info.getSubCommands().put("delete", "Deletes the calendar");
        info.getSubCommands().put("remove", "Deletes the calendar");
        info.getSubCommands().put("name", "Sets the calendar's name/summary");
        info.getSubCommands().put("summary", "Sets the calendar's name/summary");
        info.getSubCommands().put("description", "Sets the calendar's description");
        info.getSubCommands().put("timezone", "Sets teh calendar's timezone.");
        info.getSubCommands().put("edit", "Edits the calendar.");

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
    public Mono<Void> issueCommand(String[] args, MessageCreateEvent event, GuildSettings settings) {
        return Mono.defer(() -> {
            if (args.length < 1) {
                return Messages.sendMessage(Messages.getMessage("Notification.Args.Few", settings), event);
            } else {
                return DatabaseManager.getMainCalendar(settings.getGuildID())
                    .defaultIfEmpty(CalendarData.empty())
                    .flatMap(calData -> {
                        switch (args[0].toLowerCase()) {
                            case "create":
                                return PermissionChecker.hasDisCalRole(event, settings)
                                    .flatMap(has -> {
                                        if (has)
                                            return moduleCreate(args, event, calData, settings);
                                        else
                                            return Messages.sendMessage(
                                                Messages.getMessage("Notification.Perm.CONTROL_ROLE", settings), event);
                                    });
                            case "cancel":
                                return PermissionChecker.hasDisCalRole(event, settings)
                                    .flatMap(has -> {
                                        if (has)
                                            return moduleCancel(event, calData, settings);
                                        else
                                            return Messages.sendMessage(
                                                Messages.getMessage("Notification.Perm.CONTROL_ROLE", settings), event);
                                    });
                            case "view":
                            case "review":
                                return moduleView(event, calData, settings);
                            case "confirm":
                                return PermissionChecker.hasDisCalRole(event, settings)
                                    .flatMap(has -> {
                                        if (has)
                                            return moduleConfirm(event, calData, settings);
                                        else
                                            return Messages.sendMessage(
                                                Messages.getMessage("Notification.Perm.CONTROL_ROLE", settings), event);
                                    });
                            case "delete":
                            case "remove":
                                return PermissionChecker.hasDisCalRole(event, settings)
                                    .flatMap(has -> {
                                        if (has)
                                            return moduleDelete(event, calData, settings);
                                        else
                                            return Messages.sendMessage(
                                                Messages.getMessage("Notification.Perm.CONTROL_ROLE", settings), event);
                                    });
                            case "name":
                            case "summary":
                                return moduleSummary(args, event, calData, settings);
                            case "description":
                                return moduleDescription(args, event, calData, settings);
                            case "timezone":
                                return moduleTimezone(args, event, calData, settings);
                            case "edit":
                                return PermissionChecker.hasDisCalRole(event, settings)
                                    .flatMap(has -> {
                                        if (has)
                                            return moduleEdit(event, calData, settings);
                                        else
                                            return Messages.sendMessage(
                                                Messages.getMessage("Notification.Perm.CONTROL_ROLE", settings), event);
                                    });
                            default:
                                return Messages.sendMessage(Messages.getMessage("Notification.Args.Invalid", settings), event);
                        }
                    });
            }
        }).then();
    }

    private Mono<Void> moduleCreate(String[] args, MessageCreateEvent event, CalendarData calendarData, GuildSettings settings) {
        return Mono.defer(() -> {
            if (CalendarCreator.getCreator().hasPreCalendar(settings.getGuildID())) {
                PreCalendar preCal = CalendarCreator.getCreator().getPreCalendar(settings.getGuildID());

                Mono<Void> deleteUserMessage = Messages.deleteMessage(event);
                Mono<Void> deleteCreatorMessage = Messages.deleteMessage(preCal.getCreatorMessage());

                return Mono.when(deleteUserMessage, deleteCreatorMessage)
                    .then(CalendarMessageFormatter.getPreCalendarEmbed(preCal, settings))
                    .flatMap(embed -> Messages.sendMessage(Messages.getMessage("Creator.Calendar.AlreadyInit", settings), embed, event))
                    .doOnNext(preCal::setCreatorMessage);
            } else if (calendarData.getCalendarAddress().equalsIgnoreCase("primary")) {
                if (args.length > 1) {
                    String name = GeneralUtils.getContent(args, 1);

                    return CalendarCreator.getCreator().init(event, name, settings)
                        .then(Messages.deleteMessage(event));

                } else {
                    return Messages.sendMessage(Messages.getMessage("Creator.Calendar.Create.Name", settings), event);
                }
            } else {
                return Messages.sendMessage(Messages.getMessage("Creator.Calendar.HasCalendar", settings), event);
            }
        }).then();
    }

    private Mono<Void> moduleCancel(MessageCreateEvent event, CalendarData calendarData, GuildSettings settings) {
        return Mono.defer(() -> {
            if (CalendarCreator.getCreator().hasPreCalendar(settings.getGuildID())) {
                PreCalendar preCal = CalendarCreator.getCreator().getPreCalendar(settings.getGuildID());

                CalendarCreator.getCreator().terminate(settings.getGuildID());

                Mono<Void> deleteUserMessage = Messages.deleteMessage(event);
                Mono<Void> deleteCreatorMessage = Messages.deleteMessage(preCal.getCreatorMessage());

                return Mono.when(deleteUserMessage, deleteCreatorMessage)
                    .then(Mono.just(preCal.isEditing()))
                    .filter(identity -> identity)
                    .flatMap(b ->
                        Messages.sendMessage(Messages.getMessage("Creator.Calendar.Cancel.Success", settings), event)
                    ).switchIfEmpty(Messages.sendMessage(
                        Messages.getMessage("Creator.Calendar.Cancel.Edit.Success", settings), event));
            } else if (calendarData.getCalendarAddress().equalsIgnoreCase("primary")) {
                return Messages.sendMessage(Messages.getMessage("Creator.Calendar.NotInit", settings), event);
            } else {
                return Messages.sendMessage(Messages.getMessage("Creator.Calendar.HasCalendar", settings), event);
            }
        }).then();
    }

    private Mono<Void> moduleView(MessageCreateEvent event, CalendarData calendarData, GuildSettings settings) {
        return Mono.defer(() -> {
            if (CalendarCreator.getCreator().hasPreCalendar(settings.getGuildID())) {
                PreCalendar preCal = CalendarCreator.getCreator().getPreCalendar(settings.getGuildID());

                Mono<Void> deleteUserMessage = Messages.deleteMessage(event);
                Mono<Void> deleteCreatorMessage = Messages.deleteMessage(preCal.getCreatorMessage());

                return Mono.when(deleteUserMessage, deleteCreatorMessage)
                    .then(CalendarMessageFormatter.getPreCalendarEmbed(preCal, settings))
                    .flatMap(em -> Messages.sendMessage(Messages.getMessage("Creator.Calendar.Review", settings), em, event))
                    .doOnNext(preCal::setCreatorMessage);

            } else if (calendarData.getCalendarAddress().equalsIgnoreCase("primary")) {
                return Messages.sendMessage(Messages.getMessage("Creator.Calendar.NoCalendar", settings), event);
            } else {
                return Messages.sendMessage(Messages.getMessage("Creator.Calendar.HasCalendar", settings), event);
            }
        }).then();
    }

    private Mono<Void> moduleConfirm(MessageCreateEvent event, CalendarData calendarData, GuildSettings settings) {
        return Mono.defer(() -> {
            if (CalendarCreator.getCreator().hasPreCalendar(settings.getGuildID())) {
                return CalendarCreator.getCreator().confirmCalendar(settings).flatMap(response -> {
                    if (response.isSuccessful()) {
                        String msg;
                        if (response.isEdited())
                            msg = Messages.getMessage("Creator.Calendar.Confirm.Edit.Success", settings);
                        else
                            msg = Messages.getMessage("Creator.Calendar.Confirm.Create.Success", settings);

                        Mono<Void> deleteUserMessage = Messages.deleteMessage(event);
                        Mono<Void> deleteCreatorMessage = Messages.deleteMessage(response.getCreatorMessage());

                        return Mono.when(deleteUserMessage, deleteCreatorMessage)
                            .then(CalendarMessageFormatter.getCalendarLinkEmbed(response.getCalendar(), settings))
                            .flatMap(embed -> Messages.sendMessage(msg, embed, event));
                    } else {
                        //Failed, post failure message
                        PreCalendar preCal = CalendarCreator.getCreator().getPreCalendar(settings.getGuildID());
                        String msg;
                        if (response.isEdited())
                            msg = Messages.getMessage("Creator.Calendar.Confirm.Edit.Failure", settings);
                        else
                            msg = Messages.getMessage("Creator.Calendar.Confirm.Create.Failure", settings);

                        Mono<Void> deleteUserMessage = Messages.deleteMessage(event);
                        Mono<Void> deleteCreatorMessage = Messages.deleteMessage(response.getCreatorMessage());


                        return Mono.when(deleteUserMessage, deleteCreatorMessage)
                            .then(CalendarMessageFormatter.getPreCalendarEmbed(preCal, settings))
                            .flatMap(embed -> Messages.sendMessage(msg, embed, event))
                            .doOnNext(preCal::setCreatorMessage);
                    }
                });
            } else if (calendarData.getCalendarAddress().equalsIgnoreCase("primary")) {
                return Messages.sendMessage(Messages.getMessage("Creator.Calendar.NoCalendar", settings), event);
            } else {
                return Messages.sendMessage(Messages.getMessage("Creator.Calendar.HasCalendar", settings), event);
            }
        }).then();
    }

    private Mono<Void> moduleDelete(MessageCreateEvent event, CalendarData calendarData, GuildSettings settings) {
        return Mono.defer(() -> {
            if (CalendarCreator.getCreator().hasPreCalendar(settings.getGuildID())) {
                //In creator, can't delete
                PreCalendar preCal = CalendarCreator.getCreator().getPreCalendar(settings.getGuildID());

                Mono<Void> deleteUserMessage = Messages.deleteMessage(event);
                Mono<Void> deleteCreatorMessage = Messages.deleteMessage(preCal.getCreatorMessage());

                return Mono.when(deleteUserMessage, deleteCreatorMessage)
                    .then(CalendarMessageFormatter.getPreCalendarEmbed(preCal, settings))
                    .flatMap(em -> Messages.sendMessage(
                        Messages.getMessage("Creator.Calendar.Delete.Failure.InCreator", settings), em, event))
                    .doOnNext(preCal::setCreatorMessage);
            } else if (calendarData.getCalendarAddress().equalsIgnoreCase("primary")) {
                //No calendar to delete
                return Messages.sendMessage(Messages.getMessage("Creator.Calendar.Delete.Failure.NoCalendar", settings), event);
            } else {
                //Test perms and delete calendar...
                return PermissionChecker.hasManageServerRole(event)
                    .filter(identity -> identity)
                    .flatMap(b -> CalendarUtils.deleteCalendar(calendarData, settings)
                        .flatMap(success -> {
                            if (success) {
                                return Messages.sendMessage(
                                    Messages.getMessage("Creator.Calendar.Delete.Success", settings), event);
                            } else {
                                return Messages.sendMessage(
                                    Messages.getMessage("Creator.Calendar.Delete.Failure.Unknown", settings), event);
                            }
                        })
                    ).switchIfEmpty(Messages.sendMessage(Messages.getMessage("Notification.Perm.MANAGE_SERVER", settings), event));
            }
        }).then();
    }

    private Mono<Void> moduleSummary(String[] args, MessageCreateEvent event, CalendarData calendarData, GuildSettings settings) {
        return Mono.defer(() -> {
            if (CalendarCreator.getCreator().hasPreCalendar(settings.getGuildID())) {
                PreCalendar preCal = CalendarCreator.getCreator().getPreCalendar(settings.getGuildID());

                Mono<Void> deleteUserMessage = Messages.deleteMessage(event);
                Mono<Void> deleteCreatorMessage = Messages.deleteMessage(preCal.getCreatorMessage());
                if (args.length > 1) {
                    preCal.setSummary(GeneralUtils.getContent(args, 1));

                    return Mono.when(deleteUserMessage, deleteCreatorMessage)
                        .then(CalendarMessageFormatter.getPreCalendarEmbed(preCal, settings))
                        .flatMap(em -> Messages.sendMessage(
                            Messages.getMessage("Creator.Calendar.Summary.N.Success", settings), em, event))
                        .doOnNext(preCal::setCreatorMessage);
                } else {
                    return Mono.when(deleteUserMessage, deleteCreatorMessage)
                        .then(CalendarMessageFormatter.getPreCalendarEmbed(preCal, settings))
                        .flatMap(em -> Messages.sendMessage(
                            Messages.getMessage("Creator.Calendar.Summary.Specify", settings), em, event))
                        .doOnNext(preCal::setCreatorMessage);
                }
            } else if (calendarData.getCalendarAddress().equalsIgnoreCase("primary")) {
                return Messages.sendMessage(Messages.getMessage("Creator.Calendar.NoCalendar", settings), event);
            } else {
                return Messages.sendMessage(Messages.getMessage("Creator.Calendar.HasCalendar", settings), event);
            }
        }).then();
    }

    private Mono<Void> moduleDescription(String[] args, MessageCreateEvent event, CalendarData calendarData, GuildSettings settings) {
        return Mono.defer(() -> {
            if (CalendarCreator.getCreator().hasPreCalendar(settings.getGuildID())) {
                PreCalendar preCal = CalendarCreator.getCreator().getPreCalendar(settings.getGuildID());

                Mono<Void> deleteUserMessage = Messages.deleteMessage(event);
                Mono<Void> deleteCreatorMessage = Messages.deleteMessage(preCal.getCreatorMessage());
                if (args.length > 1) {
                    preCal.setDescription(GeneralUtils.getContent(args, 1));

                    return Mono.when(deleteUserMessage, deleteCreatorMessage)
                        .then(CalendarMessageFormatter.getPreCalendarEmbed(preCal, settings))
                        .flatMap(em -> Messages.sendMessage(
                            Messages.getMessage("Creator.Calendar.Description.N.Success", settings) + TIME_ZONE_DB, em,
                            event))
                        .doOnNext(preCal::setCreatorMessage);
                } else {
                    return Mono.when(deleteUserMessage, deleteCreatorMessage)
                        .then(CalendarMessageFormatter.getPreCalendarEmbed(preCal, settings))
                        .flatMap(em -> Messages.sendMessage(
                            Messages.getMessage("Creator.Calendar.Description.Specify", settings), em, event))
                        .doOnNext(preCal::setCreatorMessage);
                }
            } else if (calendarData.getCalendarAddress().equalsIgnoreCase("primary")) {
                return Messages.sendMessage(Messages.getMessage("Creator.Calendar.NoCalendar", settings), event);
            } else {
                return Messages.sendMessage(Messages.getMessage("Creator.Calendar.HasCalendar", settings), event);
            }
        }).then();
    }

    private Mono<Void> moduleTimezone(String[] args, MessageCreateEvent event, CalendarData calendarData, GuildSettings settings) {
        return Mono.defer(() -> {
            if (CalendarCreator.getCreator().hasPreCalendar(settings.getGuildID())) {
                PreCalendar preCal = CalendarCreator.getCreator().getPreCalendar(settings.getGuildID());

                Mono<Void> deleteUserMessage = Messages.deleteMessage(event);
                Mono<Void> deleteCalendarMessage = Messages.deleteMessage(preCal.getCreatorMessage());
                if (args.length == 2) {
                    String value = args[1];

                    if (TimeZoneUtils.isValid(value)) {
                        preCal.setTimezone(value);

                        return Mono.when(deleteUserMessage, deleteCalendarMessage)
                            .then(CalendarMessageFormatter.getPreCalendarEmbed(preCal, settings))
                            .flatMap(em -> Messages.sendMessage(
                                Messages.getMessage("Creator.Calendar.TimeZone.N.Success", settings), em, event))
                            .doOnNext(preCal::setCreatorMessage);
                    } else {
                        return Mono.when(deleteUserMessage, deleteCalendarMessage)
                            .then(CalendarMessageFormatter.getPreCalendarEmbed(preCal, settings))
                            .flatMap(em ->
                                Messages.sendMessage(Messages.getMessage(
                                    "Creator.Calendar.TimeZone.Invalid", "%tz_db%", TIME_ZONE_DB, settings), em, event))
                            .doOnNext(preCal::setCreatorMessage);
                    }
                } else {
                    return Mono.when(deleteUserMessage, deleteCalendarMessage)
                        .then(CalendarMessageFormatter.getPreCalendarEmbed(preCal, settings))
                        .flatMap(em -> Messages.sendMessage(
                            Messages.getMessage("Creator.Calendar.TimeZone.Specify", settings) + TIME_ZONE_DB, em, event))
                        .doOnNext(preCal::setCreatorMessage);
                }
            } else if (calendarData.getCalendarAddress().equalsIgnoreCase("primary")) {
                return Messages.sendMessage(Messages.getMessage("Creator.Calendar.NoCalendar", settings), event);
            } else {
                return Messages.sendMessage(Messages.getMessage("Creator.Calendar.HasCalendar", settings), event);
            }
        }).then();
    }

    private Mono<Void> moduleEdit(MessageCreateEvent event, CalendarData calendarData, GuildSettings settings) {
        return Mono.defer(() -> {
            if (!CalendarCreator.getCreator().hasPreCalendar(settings.getGuildID())) {
                if (!calendarData.getCalendarAddress().equalsIgnoreCase("primary")) {
                    return CalendarCreator.getCreator().edit(event, settings)
                        .then(Messages.deleteMessage(event));
                } else {
                    return Messages.sendMessage(Messages.getMessage("Creator.Calendar.NoCalendar", settings), event);
                }
            } else {
                PreCalendar preCal = CalendarCreator.getCreator().getPreCalendar(settings.getGuildID());

                Mono<Void> deleteUserMessage = Messages.deleteMessage(event);
                Mono<Void> deleteCreatorMessage = Messages.deleteMessage(preCal.getCreatorMessage());

                return Mono.when(deleteUserMessage, deleteCreatorMessage)
                    .then(CalendarMessageFormatter.getPreCalendarEmbed(preCal, settings))
                    .flatMap(embed ->
                        Messages.sendMessage(Messages.getMessage("Creator.Calendar.AlreadyInit", settings), embed, event)
                    ).doOnNext(preCal::setCreatorMessage);
            }
        }).then();
    }
}