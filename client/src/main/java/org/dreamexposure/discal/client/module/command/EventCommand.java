package org.dreamexposure.discal.client.module.command;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.EventDateTime;

import org.dreamexposure.discal.client.event.EventCreator;
import org.dreamexposure.discal.client.message.EventMessageFormatter;
import org.dreamexposure.discal.client.message.Messages;
import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.enums.event.EventColor;
import org.dreamexposure.discal.core.enums.event.EventFrequency;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.calendar.CalendarData;
import org.dreamexposure.discal.core.object.command.CommandInfo;
import org.dreamexposure.discal.core.object.event.EventData;
import org.dreamexposure.discal.core.object.event.PreEvent;
import org.dreamexposure.discal.core.utils.EventUtils;
import org.dreamexposure.discal.core.utils.GeneralUtils;
import org.dreamexposure.discal.core.utils.GlobalConst;
import org.dreamexposure.discal.core.utils.ImageUtils;
import org.dreamexposure.discal.core.utils.PermissionChecker;
import org.dreamexposure.discal.core.utils.TimeUtils;
import org.dreamexposure.discal.core.wrapper.google.EventWrapper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;
import java.util.function.Consumer;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.spec.EmbedCreateSpec;
import reactor.core.publisher.Mono;

/**
 * Created by Nova Fox on 1/3/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
@SuppressWarnings("Duplicates")
public class EventCommand implements Command {
    /**
     * Gets the command this Object is responsible for.
     *
     * @return The command this Object is responsible for.
     */
    @Override
    public String getCommand() {
        return "event";
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
        ArrayList<String> a = new ArrayList<>();
        a.add("e");

        return a;
    }

    /**
     * Gets the info on the command (not sub command) to be used in help menus.
     *
     * @return The command info.
     */
    @Override
    public CommandInfo getCommandInfo() {
        CommandInfo info = new CommandInfo("event",
            "User for all event related functions",
            "!event <function> (value(s))"
        );

        info.getSubCommands().put("create", "Creates a new event");
        info.getSubCommands().put("copy", "Copies an existing event");
        info.getSubCommands().put("edit", "Edits an existing event");
        info.getSubCommands().put("cancel", "Cancels the creator/editor");
        info.getSubCommands().put("restart", "Restarts the creator/editor");
        info.getSubCommands().put("delete", "Deletes an existing event");
        info.getSubCommands().put("view", "Views an existing event");
        info.getSubCommands().put("review", "Reviews the event in the creator/editor");
        info.getSubCommands().put("confirm", "Confirms and creates/edits the event");
        info.getSubCommands().put("start", "Sets the start of the event (format: yyyy/MM/dd-hh:mm:ss)");
        info.getSubCommands().put("startdate", "Sets the start of the event (format: yyyy/MM/dd-hh:mm:ss)");
        info.getSubCommands().put("end", "Sets the end of the event (format: yyyy/MM/dd-hh:mm:ss)");
        info.getSubCommands().put("enddate", "Sets the end of the event (format: yyyy/MM/dd-hh:mm:ss)");
        info.getSubCommands().put("summary", "Sets the summary/name of the event");
        info.getSubCommands().put("description", "Sets the description of the event");
        info.getSubCommands().put("color", "Sets the color of the event");
        info.getSubCommands().put("colour", "Sets the colour of the event");
        info.getSubCommands().put("location", "Sets the location of the event");
        info.getSubCommands().put("loc", "Sets the location of the event");
        info.getSubCommands().put("recur", "True/False whether or not the event should recur");
        info.getSubCommands().put("frequency", "Sets how often the event should recur");
        info.getSubCommands().put("freq", "Sets how often the event should recur");
        info.getSubCommands().put("count", "Sets how many times the event should recur (`-1` or `0` for infinite)");
        info.getSubCommands().put("interval", "Sets the interval at which the event should recur according to the frequency");
        info.getSubCommands().put("image", "Sets the event's image");
        info.getSubCommands().put("attachment", "Sets the event's image");

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
        //TODO: Add multi-cal handling
        return DatabaseManager.getMainCalendar(settings.getGuildID()).defaultIfEmpty(CalendarData.empty())
            .flatMap(calData -> {
                if (args.length < 1) {
                    return Messages.sendMessage(Messages.getMessage("Notification.Args.Few", settings), event);
                } else {
                    switch (args[0].toLowerCase()) {
                        case "create":
                            return PermissionChecker.hasDisCalRole(event, settings)
                                .flatMap(has -> {
                                    if (has)
                                        return moduleCreate(args, event, calData, settings);
                                    else
                                        return Messages.sendMessage(Messages.getMessage("Notification.Perm.CONTROL_ROLE", settings), event);
                                });
                        case "copy":
                            return PermissionChecker.hasDisCalRole(event, settings)
                                .flatMap(has -> {
                                    if (has)
                                        return moduleCopy(args, event, calData, settings);
                                    else
                                        return Messages.sendMessage(Messages.getMessage("Notification.Perm.CONTROL_ROLE", settings), event);
                                });
                        case "edit":
                            return PermissionChecker.hasDisCalRole(event, settings)
                                .flatMap(has -> {
                                    if (has)
                                        return moduleEdit(args, event, calData, settings);
                                    else
                                        return Messages.sendMessage(Messages.getMessage("Notification.Perm.CONTROL_ROLE", settings), event);
                                });
                        case "cancel":
                            return PermissionChecker.hasDisCalRole(event, settings)
                                .flatMap(has -> {
                                    if (has)
                                        return moduleCancel(event, settings);
                                    else
                                        return Messages.sendMessage(Messages.getMessage("Notification.Perm.CONTROL_ROLE", settings), event);
                                });
                        case "delete":
                            return PermissionChecker.hasDisCalRole(event, settings)
                                .flatMap(has -> {
                                    if (has)
                                        return moduleDelete(args, event, calData, settings);
                                    else
                                        return Messages.sendMessage(Messages.getMessage("Notification.Perm.CONTROL_ROLE", settings), event);
                                });
                        case "view":
                        case "review":
                            return moduleView(args, event, calData, settings);
                        case "confirm":
                            return PermissionChecker.hasDisCalRole(event, settings)
                                .flatMap(has -> {
                                    if (has)
                                        return moduleConfirm(event, calData, settings);
                                    else
                                        return Messages.sendMessage(Messages.getMessage("Notification.Perm.CONTROL_ROLE", settings), event);
                                });
                        case "startdate":
                        case "start":
                            return moduleStartDate(args, event, settings);
                        case "enddate":
                        case "end":
                            return moduleEndDate(args, event, settings);
                        case "summary":
                            return moduleSummary(args, event, settings);
                        case "description":
                            return moduleDescription(args, event, settings);
                        case "color":
                        case "colour":
                            return moduleColor(args, event, settings);
                        case "location":
                        case "loc":
                            return moduleLocation(args, event, settings);
                        case "image":
                        case "attachment":
                            return moduleAttachment(args, event, settings);
                        case "recur":
                            return moduleRecur(args, event, settings);
                        case "frequency":
                        case "freq":
                            return moduleFrequency(args, event, settings);
                        case "count":
                            return moduleCount(args, event, settings);
                        case "interval":
                            return moduleInterval(args, event, settings);
                        default:
                            if (EventCreator.getCreator().hasPreEvent(settings.getGuildID())) {
                                PreEvent pre = EventCreator.getCreator().getPreEvent(settings.getGuildID());

                                Mono<Void> deleteUserMessage = Messages.deleteMessage(event);
                                Mono<Void> deleteCreatorMessage = Messages.deleteMessage(pre.getCreatorMessage());

                                return Mono.when(deleteUserMessage, deleteCreatorMessage)
                                    .then(EventMessageFormatter.getPreEventEmbed(pre, settings))
                                    .flatMap(em -> Messages.sendMessage(
                                        Messages.getMessage("Notification.Args.Invalid", settings), em, event))
                                    .doOnNext(pre::setCreatorMessage);
                            } else {
                                return Messages.sendMessage(Messages.getMessage("Notification.Args.Invalid", settings), event);
                            }
                    }
                }
            }).then();
    }


    private Mono<Void> moduleCreate(String[] args, MessageCreateEvent event, CalendarData calendarData, GuildSettings settings) {
        return Mono.defer(() -> {
            if (EventCreator.getCreator().hasPreEvent(settings.getGuildID())) {
                PreEvent pre = EventCreator.getCreator().getPreEvent(settings.getGuildID());

                Mono<Void> deleteUserMessage = Messages.deleteMessage(event);
                Mono<Void> deleteCreatorMessage = Messages.deleteMessage(pre.getCreatorMessage());

                return Mono.when(deleteUserMessage, deleteCreatorMessage)
                    .then(EventMessageFormatter.getPreEventEmbed(pre, calendarData.getCalendarNumber(), settings))
                    .flatMap(em -> Messages.sendMessage(Messages.getMessage("Creator.Event.AlreadyInit", settings), em, event))
                    .doOnNext(pre::setCreatorMessage);
            } else if (!calendarData.getCalendarAddress().equalsIgnoreCase("primary")) {
                if (args.length == 1)
                    return EventCreator.getCreator().init(event, settings);
                else
                    return EventCreator.getCreator().init(event, settings, GeneralUtils.getContent(args, 1));
            } else {
                return Messages.sendMessage(Messages.getMessage("Creator.Event.NoCalendar", settings), event);
            }
        }).then();
    }

    private Mono<Void> moduleCopy(String[] args, MessageCreateEvent event, CalendarData calendarData, GuildSettings settings) {
        return Mono.defer(() -> {
            if (EventCreator.getCreator().hasPreEvent(settings.getGuildID())) {
                //Already in creator/editor
                PreEvent pre = EventCreator.getCreator().getPreEvent(settings.getGuildID());

                Mono<Void> deleteUserMessage = Messages.deleteMessage(event);
                Mono<Void> deleteCreatorMessage = Messages.deleteMessage(pre.getCreatorMessage());

                return Mono.when(deleteUserMessage, deleteCreatorMessage)
                    .then(EventMessageFormatter.getPreEventEmbed(pre, calendarData.getCalendarNumber(), settings))
                    .flatMap(em -> Messages.sendMessage(Messages.getMessage("Creator.Event.AlreadyInit", settings), em, event))
                    .doOnNext(pre::setCreatorMessage);
            } else if (!calendarData.getCalendarAddress().equalsIgnoreCase("primary")) {
                //Has calendar, not in creator/editor, start it.
                if (args.length == 2) {
                    String eventId = args[1];
                    return EventUtils.eventExists(settings, calendarData.getCalendarNumber(), eventId)
                        .flatMap(exists -> {
                            if (exists) {
                                return EventCreator.getCreator().init(event, eventId, settings);
                            } else {
                                return Messages.sendMessage(Messages.getMessage("Creator.Event.NotFound", settings), event);
                            }
                        });
                } else {
                    return Messages.sendMessage(Messages.getMessage("Creator.Event.Copy.Specify", settings), event);
                }
            } else {
                return Messages.sendMessage(Messages.getMessage("Creator.Event.NoCalendar", settings), event);
            }
        }).then();
    }

    private Mono<Void> moduleEdit(String[] args, MessageCreateEvent event, CalendarData calendarData, GuildSettings settings) {
        return Mono.defer(() -> {
            if (EventCreator.getCreator().hasPreEvent(settings.getGuildID())) {
                //Already in creator
                PreEvent pre = EventCreator.getCreator().getPreEvent(settings.getGuildID());

                Mono<Void> deleteUserMessage = Messages.deleteMessage(event);
                Mono<Void> deleteCreatorMessage = Messages.deleteMessage(pre.getCreatorMessage());

                return Mono.when(deleteUserMessage, deleteCreatorMessage)
                    .then(EventMessageFormatter.getPreEventEmbed(pre, calendarData.getCalendarNumber(), settings))
                    .flatMap(em -> Messages.sendMessage(Messages.getMessage("Creator.Event.AlreadyInit", settings), em, event))
                    .doOnNext(pre::setCreatorMessage);
            } else if (calendarData.getCalendarAddress().equalsIgnoreCase("primary")) {
                //Does not have calendar
                return Messages.sendMessage(Messages.getMessage("Creator.Event.NoCalendar", settings), event);
            } else {
                //Not in creator, start editor
                if (args.length == 2) {
                    String eventId = args[1];

                    return EventUtils.eventExists(settings, calendarData.getCalendarNumber(), eventId)
                        .flatMap(exists -> {
                            if (exists) {
                                return EventCreator.getCreator().edit(event, eventId, settings);
                            } else {
                                return Messages.sendMessage(Messages.getMessage("Creator.Event.NotFound", settings), event);
                            }
                        });
                } else {
                    return Messages.sendMessage(Messages.getMessage("Creator.Event.Edit.Specify", settings), event);
                }
            }
        }).then();
    }

    private Mono<Void> moduleCancel(MessageCreateEvent event, GuildSettings settings) {
        return Mono.defer(() -> {
            if (EventCreator.getCreator().hasPreEvent(settings.getGuildID())) {
                PreEvent pre = EventCreator.getCreator().getPreEvent(settings.getGuildID());

                Mono<Void> deleteUserMessage = Messages.deleteMessage(event);
                Mono<Void> deleteCreatorMessage = Messages.deleteMessage(pre.getCreatorMessage());

                EventCreator.getCreator().terminate(settings.getGuildID());

                return Mono.when(deleteUserMessage, deleteCreatorMessage)
                    .then(Messages.sendMessage(Messages.getMessage("Creator.Event.Cancel.Success", settings), event));
            } else {
                return Messages.sendMessage(Messages.getMessage("Creator.Event.NotInit", settings), event);
            }
        }).then();
    }

    private Mono<Void> moduleDelete(String[] args, MessageCreateEvent event, CalendarData calendarData, GuildSettings settings) {
        return Mono.defer(() -> {
            if (EventCreator.getCreator().hasPreEvent(settings.getGuildID())) {
                //In creator/editor, cannot delete
                PreEvent pre = EventCreator.getCreator().getPreEvent(settings.getGuildID());

                Mono<Void> deleteUserMessage = Messages.deleteMessage(event);
                Mono<Void> deleteCreatorMessage = Messages.deleteMessage(pre.getCreatorMessage());

                return Mono.when(deleteUserMessage, deleteCreatorMessage)
                    .then(EventMessageFormatter.getPreEventEmbed(pre, calendarData.getCalendarNumber(), settings))
                    .flatMap(em -> Messages.sendMessage(Messages
                        .getMessage("Creator.Event.Delete.Failure.Creator", settings), em, event))
                    .doOnNext(pre::setCreatorMessage);
            } else if (calendarData.getCalendarAddress().equalsIgnoreCase("primary")) {
                //Does not have calendar..
                return Messages.sendMessage(Messages.getMessage("Creator.Event.NoCalendar", settings), event);
            } else {
                //Delete event
                if (args.length == 2) {
                    return EventUtils.deleteEvent(settings, calendarData.getCalendarNumber(), args[1])
                        .flatMap(success -> {
                            if (success)
                                return Messages.sendMessage(Messages.getMessage("Creator.Event.Delete.Success", settings), event);
                            else
                                return Messages.sendMessage(Messages.getMessage("Creator.Event.NotFound", settings), event);
                        });
                } else {
                    return Messages.sendMessage(Messages.getMessage("Creator.Event.Delete.Specify", settings), event);
                }
            }
        }).then();
    }

    private Mono<Void> moduleView(String[] args, MessageCreateEvent event, CalendarData calendarData, GuildSettings settings) {
        return Mono.defer(() -> {
            if (EventCreator.getCreator().hasPreEvent(settings.getGuildID())) {
                // In editor, show that
                PreEvent pre = EventCreator.getCreator().getPreEvent(settings.getGuildID());

                Mono<Void> deleteUserMessage = Messages.deleteMessage(event);
                Mono<Void> deleteCreatorMessage = Messages.deleteMessage(pre.getCreatorMessage());

                return Mono.when(deleteUserMessage, deleteCreatorMessage)
                    .then(EventMessageFormatter.getPreEventEmbed(pre, calendarData.getCalendarNumber(), settings))
                    .flatMap(em -> Messages.sendMessage(Messages.getMessage("Event.View.Creator.Confirm", settings), em, event))
                    .doOnNext(pre::setCreatorMessage);
            } else if (calendarData.getCalendarAddress().equalsIgnoreCase("primary")) {
                //Does not have calendar...
                return Messages.sendMessage(Messages.getMessage("Creator.Event.NoCalendar", settings), event);
            } else {
                //Check if enough args and event exists...
                if (args.length == 2) {
                    return EventWrapper.getEvent(calendarData, settings, args[1])
                        .flatMap(e -> EventMessageFormatter.getEventEmbed(e, calendarData.getCalendarNumber(), settings))
                        .flatMap(em -> Messages.sendMessage(em, event))
                        .switchIfEmpty(Messages.sendMessage(Messages.getMessage("Creator.Event.NotFound", settings), event));
                } else {
                    return Messages.sendMessage(Messages.getMessage("Event.View.Specify", settings), event);
                }
            }
        }).then();
    }

    private Mono<Void> moduleConfirm(MessageCreateEvent event, CalendarData calendarData, GuildSettings settings) {
        return Mono.defer(() -> {
            if (EventCreator.getCreator().hasPreEvent(settings.getGuildID())) {
                PreEvent pre = EventCreator.getCreator().getPreEvent(settings.getGuildID());

                Mono<Void> deleteUserMessage = Messages.deleteMessage(event);
                Mono<Void> deleteCreatorMessage = Messages.deleteMessage(pre.getCreatorMessage());
                if (pre.hasRequiredValues()) {
                    return EventCreator.getCreator().confirmEvent(settings).flatMap(response -> {
                        if (response.isSuccessful()) {
                            String msg;
                            if (response.isEdited())
                                msg = Messages.getMessage("Creator.Event.Confirm.Edit", settings);
                            else
                                msg = Messages.getMessage("Creator.Event.Confirm.Create", settings);

                            return Mono.when(deleteUserMessage, deleteCreatorMessage)
                                .then(EventMessageFormatter.getEventEmbed(response.getEvent(),
                                    calendarData.getCalendarNumber(), settings))
                                .flatMap(em -> Messages.sendMessage(msg, em, event));
                        } else {
                            return Mono.when(deleteUserMessage, deleteCreatorMessage)
                                .then(EventMessageFormatter.getPreEventEmbed(pre, calendarData.getCalendarNumber(),
                                    settings))
                                .flatMap(em -> Messages.sendMessage(
                                    Messages.getMessage("Creator.Event.Confirm.Failure", settings), em, event))
                                .doOnNext(pre::setCreatorMessage);
                        }
                    });
                } else {
                    //Some values are unset, tell user.
                    return Mono.when(deleteUserMessage, deleteCreatorMessage)
                        .then(EventMessageFormatter.getPreEventEmbed(pre, calendarData.getCalendarNumber(), settings))
                        .flatMap(em -> Messages.sendMessage(Messages.getMessage("Creator.Event.NoRequired", settings), em, event))
                        .doOnNext(pre::setCreatorMessage);
                }
            } else if (calendarData.getCalendarAddress().equalsIgnoreCase("primary")) {
                //No calendar...
                return Messages.sendMessage(Messages.getMessage("Creator.Event.NoCalendar", settings), event);
            } else {
                return Messages.sendMessage(Messages.getMessage("Creator.Event.NotInit", settings), event);
            }
        }).then();
    }

    private Mono<Void> moduleStartDate(String[] args, MessageCreateEvent event, GuildSettings settings) {
        return Mono.defer(() -> {
            if (EventCreator.getCreator().hasPreEvent(settings.getGuildID())) {
                PreEvent pre = EventCreator.getCreator().getPreEvent(settings.getGuildID());

                Mono<Void> deleteUserMessage = Messages.deleteMessage(event);
                Mono<Void> deleteCreatorMessage = Messages.deleteMessage(pre.getCreatorMessage());

                if (args.length == 2) {
                    if (args[1].trim().length() > 10) {
                        return Mono.just(args[1].trim()).flatMap(dateRaw -> {
                            //Do a lot of date shuffling to get to proper formats and shit like that.
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss");
                            TimeZone tz = TimeZone.getTimeZone(pre.getTimeZone());
                            sdf.setTimeZone(tz);
                            Date dateObj;
                            try {
                                dateObj = sdf.parse(dateRaw);
                            } catch (ParseException e) {
                                return Mono.when(deleteUserMessage, deleteCreatorMessage)
                                    .then(EventMessageFormatter.getPreEventEmbed(pre, settings))
                                    .flatMap(em -> Messages.sendMessage(
                                        Messages.getMessage("Creator.Event.Time.InvalidFormat", settings), em, event))
                                    .doOnNext(pre::setCreatorMessage);
                            }
                            DateTime dateTime = new DateTime(dateObj);
                            EventDateTime eventDateTime = new EventDateTime();
                            eventDateTime.setDateTime(dateTime);

                            //Wait! Lets check now if its in the future and not the past!
                            if (!TimeUtils.inPast(dateRaw, tz) && !TimeUtils.startAfterEnd(dateRaw, tz, pre)) {
                                //Date shuffling done, now actually apply all that damn stuff here.
                                pre.setStartDateTime(eventDateTime);

                                //Apply viewable date/times...
                                SimpleDateFormat sdfV = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss");
                                Date dateObjV;
                                try {
                                    dateObjV = sdfV.parse(dateRaw);
                                } catch (ParseException e) {
                                    return Mono.when(deleteUserMessage, deleteCreatorMessage)
                                        .then(EventMessageFormatter.getPreEventEmbed(pre, settings))
                                        .flatMap(em -> Messages.sendMessage(
                                            Messages.getMessage("Creator.Event.Time.InvalidFormat", settings), em, event))
                                        .doOnNext(pre::setCreatorMessage);
                                }
                                DateTime dateTimeV = new DateTime(dateObjV);
                                EventDateTime eventDateTimeV = new EventDateTime();
                                eventDateTimeV.setDateTime(dateTimeV);
                                pre.setViewableStartDate(eventDateTimeV);

                                //To streamline, check if event end is null, if so, apply 1 hour duration!
                                if (pre.getEndDateTime() == null) {
                                    EventDateTime end = pre.getStartDateTime().clone();
                                    long endLong = end.getDateTime().getValue() + 3600000; //Add an hour

                                    end.setDateTime(new DateTime(endLong));

                                    pre.setEndDateTime(end);

                                    //Viewable date
                                    EventDateTime endV = pre.getViewableStartDate().clone();
                                    long endVLong = endV.getDateTime().getValue() + 3600000; //Add an hour

                                    endV.setDateTime(new DateTime(endVLong));

                                    pre.setViewableEndDate(endV);
                                }

                                //Okay, all done, now time send back the creator message..
                                return Mono.when(deleteUserMessage, deleteCreatorMessage)
                                    .then(EventMessageFormatter.getPreEventEmbed(pre, settings))
                                    .flatMap(em -> Messages.sendMessage(Messages
                                        .getMessage("Creator.Event.Start.Success.New", settings), em, event))
                                    .doOnNext(pre::setCreatorMessage);
                            } else {
                                //Oops! Time is in the past or after end...
                                return Mono.when(deleteUserMessage, deleteCreatorMessage)
                                    .then(EventMessageFormatter.getPreEventEmbed(pre, settings))
                                    .flatMap(em -> Messages.sendMessage(Messages
                                        .getMessage("Creator.Event.Start.Failure.Illegal", settings), em, event))
                                    .doOnNext(pre::setCreatorMessage);
                            }
                        }).onErrorResume(ParseException.class, e ->
                            Mono.when(deleteUserMessage, deleteCreatorMessage)
                                .then(EventMessageFormatter.getPreEventEmbed(pre, settings)
                                    .flatMap(em -> Messages.sendMessage(
                                        Messages.getMessage("Creator.Event.Time.Invalid", settings), em, event))
                                    .doOnNext(pre::setCreatorMessage)));
                    } else {
                        //Invalid format used for time...
                        return Mono.when(deleteUserMessage, deleteCreatorMessage)
                            .then(EventMessageFormatter.getPreEventEmbed(pre, settings))
                            .flatMap(em -> Messages.sendMessage(Messages.getMessage("Creator.Event.Time.InvalidFormat", settings), em, event))
                            .doOnNext(pre::setCreatorMessage);
                    }
                } else {
                    return Mono.when(deleteUserMessage, deleteCreatorMessage)
                        .then(EventMessageFormatter.getPreEventEmbed(pre, settings))
                        .flatMap(em -> Messages.sendMessage(Messages.getMessage("Creator.Event.Start.Specify", settings), em, event))
                        .doOnNext(pre::setCreatorMessage);
                }
            } else {
                return Messages.sendMessage(Messages.getMessage("Creator.Event.NotInit", settings), event);
            }
        }).then();
    }

    private Mono<Void> moduleEndDate(String[] args, MessageCreateEvent event, GuildSettings settings) {
        return Mono.defer(() -> {
            if (EventCreator.getCreator().hasPreEvent(settings.getGuildID())) {
                PreEvent pre = EventCreator.getCreator().getPreEvent(settings.getGuildID());

                Mono<Void> deleteUserMessage = Messages.deleteMessage(event);
                Mono<Void> deleteCreatorMessage = Messages.deleteMessage(pre.getCreatorMessage());

                if (args.length == 2) {
                    if (args[1].trim().length() > 10) {
                        return Mono.just(args[1].trim()).flatMap(dateRaw -> {
                            //Do a lot of date shuffling to get to proper formats and shit like that.
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss");
                            TimeZone tz = TimeZone.getTimeZone(pre.getTimeZone());
                            sdf.setTimeZone(tz);
                            Date dateObj;
                            try {
                                dateObj = sdf.parse(dateRaw);
                            } catch (ParseException e) {
                                return Mono.when(deleteUserMessage, deleteCreatorMessage)
                                    .then(EventMessageFormatter.getPreEventEmbed(pre, settings))
                                    .flatMap(em -> Messages.sendMessage(
                                        Messages.getMessage("Creator.Event.Time.InvalidFormat", settings), em, event))
                                    .doOnNext(pre::setCreatorMessage);
                            }
                            DateTime dateTime = new DateTime(dateObj);
                            EventDateTime eventDateTime = new EventDateTime();
                            eventDateTime.setDateTime(dateTime);

                            //Wait! Lets check now if its in the future and not the past!
                            if (!TimeUtils.inPast(dateRaw, tz) && !TimeUtils.endBeforeStart(dateRaw, tz, pre)) {
                                //Date shuffling done, now actually apply all that damn stuff here.
                                pre.setEndDateTime(eventDateTime);

                                //Apply viewable date/times...
                                SimpleDateFormat sdfV = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss");
                                Date dateObjV;
                                try {
                                    dateObjV = sdfV.parse(dateRaw);
                                } catch (ParseException e) {
                                    return Mono.error(e);
                                }
                                DateTime dateTimeV = new DateTime(dateObjV);
                                EventDateTime eventDateTimeV = new EventDateTime();
                                eventDateTimeV.setDateTime(dateTimeV);
                                pre.setViewableEndDate(eventDateTimeV);

                                //Okay, all done, now time send back the creator message..
                                return Mono.when(deleteUserMessage, deleteCreatorMessage)
                                    .then(EventMessageFormatter.getPreEventEmbed(pre, settings))
                                    .flatMap(em -> Messages.sendMessage(Messages
                                        .getMessage("Creator.Event.End.Success.New", settings), em, event))
                                    .doOnNext(pre::setCreatorMessage);
                            } else {
                                //Oops! Time is in the past or after end...
                                return Mono.when(deleteUserMessage, deleteCreatorMessage)
                                    .then(EventMessageFormatter.getPreEventEmbed(pre, settings))
                                    .flatMap(em -> Messages.sendMessage(Messages
                                        .getMessage("Creator.Event.End.Failure.Illegal", settings), em, event))
                                    .doOnNext(pre::setCreatorMessage);
                            }
                        }).onErrorResume(ParseException.class, e ->
                            Mono.when(deleteUserMessage, deleteCreatorMessage)
                                .then(EventMessageFormatter.getPreEventEmbed(pre, settings)
                                    .flatMap(em -> Messages.sendMessage(
                                        Messages.getMessage("Creator.Event.Time.Invalid", settings), em, event))
                                    .doOnNext(pre::setCreatorMessage)));
                    } else {
                        //Invalid format used for time...
                        return Mono.when(deleteUserMessage, deleteCreatorMessage)
                            .then(EventMessageFormatter.getPreEventEmbed(pre, settings))
                            .flatMap(em -> Messages.sendMessage(Messages.getMessage("Creator.Event.Time.InvalidFormat", settings), em, event))
                            .doOnNext(pre::setCreatorMessage);
                    }
                } else {
                    return Mono.when(deleteUserMessage, deleteCreatorMessage)
                        .then(EventMessageFormatter.getPreEventEmbed(pre, settings))
                        .flatMap(em -> Messages.sendMessage(Messages.getMessage("Creator.Event.End.Specify", settings), em, event))
                        .doOnNext(pre::setCreatorMessage);
                }
            } else {
                return Messages.sendMessage(Messages.getMessage("Creator.Event.NotInit", settings), event);
            }
        }).then();
    }

    private Mono<Void> moduleSummary(String[] args, MessageCreateEvent event, GuildSettings settings) {
        return Mono.defer(() -> {
            if (EventCreator.getCreator().hasPreEvent(settings.getGuildID())) {
                PreEvent pre = EventCreator.getCreator().getPreEvent(settings.getGuildID());

                Mono<Void> deleteUserMessage = Messages.deleteMessage(event);
                Mono<Void> deleteCreatorMessage = Messages.deleteMessage(pre.getCreatorMessage());

                if (args.length > 1) {
                    pre.setSummary(GeneralUtils.getContent(args, 1));

                    return Mono.when(deleteUserMessage, deleteCreatorMessage)
                        .then(EventMessageFormatter.getPreEventEmbed(pre, settings))
                        .flatMap(em -> Messages.sendMessage(Messages
                            .getMessage("Creator.Event.Summary.Success.New", settings), em, event))
                        .doOnNext(pre::setCreatorMessage);
                } else {
                    return Mono.when(deleteUserMessage, deleteCreatorMessage)
                        .then(EventMessageFormatter.getPreEventEmbed(pre, settings))
                        .flatMap(em -> Messages.sendMessage(Messages
                            .getMessage("Creator.Event.Summary.Specify", settings), em, event))
                        .doOnNext(pre::setCreatorMessage);
                }
            } else {
                return Messages.sendMessage(Messages.getMessage("Creator.Event.NotInit", settings), event);
            }
        }).then();
    }

    private Mono<Void> moduleDescription(String[] args, MessageCreateEvent event, GuildSettings settings) {
        return Mono.defer(() -> {
            if (EventCreator.getCreator().hasPreEvent(settings.getGuildID())) {
                PreEvent pre = EventCreator.getCreator().getPreEvent(settings.getGuildID());

                Mono<Void> deleteUserMessage = Messages.deleteMessage(event);
                Mono<Void> deleteCreatorMessage = Messages.deleteMessage(pre.getCreatorMessage());

                if (args.length > 1) {
                    pre.setDescription(GeneralUtils.getContent(args, 1));

                    return Mono.when(deleteUserMessage, deleteCreatorMessage)
                        .then(EventMessageFormatter.getPreEventEmbed(pre, settings))
                        .flatMap(em -> Messages.sendMessage(Messages
                            .getMessage("Creator.Event.Description.Success.New", settings), em, event))
                        .doOnNext(pre::setCreatorMessage);
                } else {
                    return Mono.when(deleteUserMessage, deleteCreatorMessage)
                        .then(EventMessageFormatter.getPreEventEmbed(pre, settings))
                        .flatMap(em -> Messages.sendMessage(Messages
                            .getMessage("Creator.Event.Description.Specify", settings), em, event))
                        .doOnNext(pre::setCreatorMessage);
                }
            } else {
                return Messages.sendMessage(Messages.getMessage("Creator.Event.NotInit", settings), event);
            }
        }).then();
    }

    private Mono<Void> moduleColor(String[] args, MessageCreateEvent event, GuildSettings settings) {
        return Mono.defer(() -> {
            if (EventCreator.getCreator().hasPreEvent(settings.getGuildID())) {
                PreEvent pre = EventCreator.getCreator().getPreEvent(settings.getGuildID());

                Mono<Void> deleteUserMessage = Messages.deleteMessage(event);
                Mono<Void> deleteCreatorMessage = Messages.deleteMessage(pre.getCreatorMessage());

                if (args.length == 2) {
                    String value = args[1];
                    if (value.equalsIgnoreCase("list") || value.equalsIgnoreCase("colors")
                        || value.equalsIgnoreCase("colours")) {
                        Consumer<EmbedCreateSpec> embed = spec -> {
                            spec.setAuthor("DisCal", GlobalConst.discalSite, GlobalConst.iconUrl);

                            spec.setTitle("Available Colors");
                            spec.setUrl("https://discalbot.com/docs/event/colors");
                            spec.setColor(GlobalConst.discalColor);
                            spec.setFooter("Click Title for previews of the colors!", null);

                            for (EventColor ec : EventColor.values()) {
                                spec.addField(ec.name(), ec.getId() + "", true);
                            }
                        };

                        return Mono.when(deleteUserMessage, deleteCreatorMessage)
                            .then(Messages.sendMessage("All Supported Colors. " +
                                "Use either the name or ID in the command: `!event color <name/id>`", embed, event))
                            .doOnNext(pre::setCreatorMessage);
                    } else {
                        if (EventColor.exists(value)) {
                            pre.setColor(EventColor.fromNameOrHexOrID(value));

                            return Mono.when(deleteUserMessage, deleteCreatorMessage)
                                .then(EventMessageFormatter.getPreEventEmbed(pre, settings))
                                .flatMap(em -> Messages.sendMessage(
                                    Messages.getMessage("Creator.Event.Color.Success.New", settings), em, event))
                                .doOnNext(pre::setCreatorMessage);
                        } else {
                            return Mono.when(deleteUserMessage, deleteCreatorMessage)
                                .then(EventMessageFormatter.getPreEventEmbed(pre, settings))
                                .flatMap(em -> Messages.sendMessage(
                                    Messages.getMessage("Creator.Event.Color.Invalid", settings), em, event))
                                .doOnNext(pre::setCreatorMessage);
                        }
                    }
                } else {
                    //Not enough args...
                    return Mono.when(deleteUserMessage, deleteCreatorMessage)
                        .then(EventMessageFormatter.getPreEventEmbed(pre, settings))
                        .flatMap(em -> Messages.sendMessage(
                            Messages.getMessage("Creator.Event.Color.Specify", settings), em, event))
                        .doOnNext(pre::setCreatorMessage);
                }
            } else {
                //Not in creator/editor, just default to listing the supported colors.
                Consumer<EmbedCreateSpec> embed = spec -> {
                    spec.setAuthor("DisCal", GlobalConst.discalSite, GlobalConst.iconUrl);

                    spec.setTitle("Available Colors");
                    spec.setUrl("https://discalbot.com/docs/event/colors");
                    spec.setColor(GlobalConst.discalColor);
                    spec.setFooter("Click Title for previews of the colors!", null);

                    for (EventColor ec : EventColor.values()) {
                        spec.addField(ec.name(), ec.getId() + "", true);
                    }
                };
                return Messages.sendMessage("All Supported Colors. " +
                    "Use either the name or ID in the command: `!event color <name/id>`", embed, event);
            }
        }).then();
    }

    private Mono<Void> moduleLocation(String[] args, MessageCreateEvent event, GuildSettings settings) {
        return Mono.defer(() -> {
            if (EventCreator.getCreator().hasPreEvent(settings.getGuildID())) {
                PreEvent pre = EventCreator.getCreator().getPreEvent(settings.getGuildID());

                Mono<Void> deleteUserMessage = Messages.deleteMessage(event);
                Mono<Void> deleteCreatorMessage = Messages.deleteMessage(pre.getCreatorMessage());

                if (args.length > 1) {
                    if (args[1].equalsIgnoreCase("clear")) {
                        pre.setLocation(null);

                        return Mono.when(deleteUserMessage, deleteCreatorMessage)
                            .then(EventMessageFormatter.getPreEventEmbed(pre, settings))
                            .flatMap(em -> Messages.sendMessage(Messages
                                .getMessage("Creator.Event.Location.Success.Clear", settings), em, event))
                            .doOnNext(pre::setCreatorMessage);
                    } else {
                        pre.setLocation(GeneralUtils.getContent(args, 1));

                        return Mono.when(deleteUserMessage, deleteCreatorMessage)
                            .then(EventMessageFormatter.getPreEventEmbed(pre, settings))
                            .flatMap(em -> Messages.sendMessage(Messages
                                .getMessage("Creator.Event.Location.Success.New", settings), em, event))
                            .doOnNext(pre::setCreatorMessage);
                    }
                } else {
                    return Mono.when(deleteUserMessage, deleteCreatorMessage)
                        .then(EventMessageFormatter.getPreEventEmbed(pre, settings))
                        .flatMap(em -> Messages.sendMessage(Messages
                            .getMessage("Creator.Event.Location.Specify", settings), em, event))
                        .doOnNext(pre::setCreatorMessage);
                }
            } else {
                return Messages.sendMessage(Messages.getMessage("Creator.Event.NotInit", settings), event);
            }
        }).then();
    }

    private Mono<Void> moduleAttachment(String[] args, MessageCreateEvent event, GuildSettings settings) {
        return Mono.defer(() -> {
            if (EventCreator.getCreator().hasPreEvent(settings.getGuildID())) {
                PreEvent pre = EventCreator.getCreator().getPreEvent(settings.getGuildID());

                Mono<Void> deleteUserMessage = Messages.deleteMessage(event);
                Mono<Void> deleteCreatorMessage = Messages.deleteMessage(pre.getCreatorMessage());

                if (args.length == 2) {
                    String value = args[1].trim();
                    if (value.equalsIgnoreCase("delete") || value.equalsIgnoreCase("remove")
                        || value.equalsIgnoreCase("clear")) {
                        pre.setEventData(EventData.empty());

                        return Mono.when(deleteUserMessage, deleteCreatorMessage)
                            .then(EventMessageFormatter.getPreEventEmbed(pre, settings))
                            .flatMap(em -> Messages.sendMessage(
                                Messages.getMessage("Creator.Event.Attachment.Delete", settings), em, event))
                            .doOnNext(pre::setCreatorMessage);
                    } else {
                        return ImageUtils.validate(value, settings.isPatronGuild()).flatMap(valid -> {
                            if (valid) {
                                PreEvent preEvent = EventCreator.getCreator().getPreEvent(settings.getGuildID());

                                EventData eventData = EventData.fromImage(
                                    settings.getGuildID(),
                                    preEvent.getEventId(),
                                    preEvent.getEndDateTime().getDateTime().getValue(),
                                    value
                                );
                                preEvent.setEventData(eventData);

                                return Mono.when(deleteUserMessage, deleteCreatorMessage)
                                    .then(EventMessageFormatter.getPreEventEmbed(pre, settings))
                                    .flatMap(em -> Messages.sendMessage(
                                        Messages.getMessage("Creator.Event.Attachment.Success", settings), em, event))
                                    .doOnNext(pre::setCreatorMessage);
                            } else {
                                return Mono.when(deleteUserMessage, deleteCreatorMessage)
                                    .then(EventMessageFormatter.getPreEventEmbed(pre, settings))
                                    .flatMap(em -> Messages.sendMessage(
                                        Messages.getMessage("Creator.Event.Attachment.Failure", settings), em, event))
                                    .doOnNext(pre::setCreatorMessage);
                            }
                        });
                    }
                } else {
                    return Mono.when(deleteUserMessage, deleteCreatorMessage)
                        .then(EventMessageFormatter.getPreEventEmbed(pre, settings))
                        .flatMap(em -> Messages.sendMessage(
                            Messages.getMessage("Creator.Event.Attachment.Specify", settings), em, event))
                        .doOnNext(pre::setCreatorMessage);
                }
            } else {
                return Messages.sendMessage(Messages.getMessage("Creator.Event.NotInit", settings), event);
            }
        }).then();
    }

    //Event recurrence settings
    private Mono<Void> moduleRecur(String[] args, MessageCreateEvent event, GuildSettings settings) {
        return Mono.defer(() -> {
            if (EventCreator.getCreator().hasPreEvent(settings.getGuildID())) {
                PreEvent pre = EventCreator.getCreator().getPreEvent(settings.getGuildID());

                Mono<Void> deleteUserMessage = Messages.deleteMessage(event);
                Mono<Void> deleteCreatorMessage = Messages.deleteMessage(pre.getCreatorMessage());

                if (args.length == 2) {
                    String valueString = args[1];
                    if (pre.isEditing() && pre.getEventId().contains("_")) {
                        //This event is a child of a recurring parent. we can't edit it's recurrence
                        return Mono.when(deleteUserMessage, deleteCreatorMessage)
                            .then(EventMessageFormatter.getPreEventEmbed(pre, settings))
                            .flatMap(em -> Messages.sendMessage(
                                Messages.getMessage("Creator.Event.Recur.Failure.Child",
                                    "%id%", pre.getEventId().split("_")[0], settings), em, event))
                            .doOnNext(pre::setCreatorMessage);
                    }

                    boolean shouldRecur = Boolean.parseBoolean(valueString);
                    pre.setShouldRecur(shouldRecur);

                    if (shouldRecur) {
                        return Mono.when(deleteUserMessage, deleteCreatorMessage)
                            .then(EventMessageFormatter.getPreEventEmbed(pre, settings))
                            .flatMap(em -> Messages.sendMessage(
                                Messages.getMessage("Creator.Event.Recur.True", settings), em, event))
                            .doOnNext(pre::setCreatorMessage);
                    } else {
                        return Mono.when(deleteUserMessage, deleteCreatorMessage)
                            .then(EventMessageFormatter.getPreEventEmbed(pre, settings))
                            .flatMap(em -> Messages.sendMessage(
                                Messages.getMessage("Creator.Event.Recur.False", settings), em, event))
                            .doOnNext(pre::setCreatorMessage);
                    }

                } else {
                    return Mono.when(deleteUserMessage, deleteCreatorMessage)
                        .then(EventMessageFormatter.getPreEventEmbed(pre, settings))
                        .flatMap(em -> Messages.sendMessage(
                            Messages.getMessage("Creator.Event.Recur.Specify", settings), em, event))
                        .doOnNext(pre::setCreatorMessage);
                }
            } else {
                return Messages.sendMessage(Messages.getMessage("Creator.Event.NotInit", settings), event);
            }
        }).then();
    }

    private Mono<Void> moduleFrequency(String[] args, MessageCreateEvent event, GuildSettings settings) {
        return Mono.defer(() -> {
            if (EventCreator.getCreator().hasPreEvent(settings.getGuildID())) {
                PreEvent pre = EventCreator.getCreator().getPreEvent(settings.getGuildID());

                Mono<Void> deleteUserMessage = Messages.deleteMessage(event);
                Mono<Void> deleteCreatorMessage = Messages.deleteMessage(pre.getCreatorMessage());

                if (args.length == 2) {
                    if (pre.shouldRecur()) {
                        if (EventFrequency.isValid(args[1])) {
                            pre.getRecurrence().setFrequency(EventFrequency.fromValue(args[1]));

                            return Mono.when(deleteUserMessage, deleteCreatorMessage)
                                .then(EventMessageFormatter.getPreEventEmbed(pre, settings))
                                .flatMap(em -> Messages.sendMessage(
                                    Messages.getMessage("Creator.Event.Frequency.Success.New", settings), em, event))
                                .doOnNext(pre::setCreatorMessage);
                        } else {
                            String values = Arrays.toString(EventFrequency.values()).replace("[", "").replace("]", "");

                            return Mono.when(deleteUserMessage, deleteCreatorMessage)
                                .then(EventMessageFormatter.getPreEventEmbed(pre, settings))
                                .flatMap(em -> Messages.sendMessage(
                                    Messages.getMessage("Creator.Event.Frequency.List", "%types%", values, settings)
                                    , em, event))
                                .doOnNext(pre::setCreatorMessage);
                        }
                    } else {
                        return Mono.when(deleteUserMessage, deleteCreatorMessage)
                            .then(EventMessageFormatter.getPreEventEmbed(pre, settings))
                            .flatMap(em -> Messages.sendMessage(
                                Messages.getMessage("Creator.Event.Recur.Not", settings), em, event))
                            .doOnNext(pre::setCreatorMessage);
                    }
                } else {
                    String values = Arrays.toString(EventFrequency.values()).replace("[", "").replace("]", "");

                    return Mono.when(deleteUserMessage, deleteCreatorMessage)
                        .then(EventMessageFormatter.getPreEventEmbed(pre, settings))
                        .flatMap(em -> Messages.sendMessage(
                            Messages.getMessage("Creator.Event.Frequency.Specify", "%types%", values, settings)
                            , em, event))
                        .doOnNext(pre::setCreatorMessage);
                }
            } else {
                return Messages.sendMessage(Messages.getMessage("Creator.Event.NotInit", settings), event);
            }
        }).then();
    }

    private Mono<Void> moduleCount(String[] args, MessageCreateEvent event, GuildSettings settings) {
        return Mono.defer(() -> {
            if (EventCreator.getCreator().hasPreEvent(settings.getGuildID())) {
                PreEvent pre = EventCreator.getCreator().getPreEvent(settings.getGuildID());

                Mono<Void> deleteUserMessage = Messages.deleteMessage(event);
                Mono<Void> deleteCreatorMessage = Messages.deleteMessage(pre.getCreatorMessage());

                if (args.length == 2) {
                    if (pre.shouldRecur()) {
                        int amount = 0;

                        try {
                            Integer.parseInt(args[1]);
                        } catch (NumberFormatException e) {
                            return Mono.when(deleteUserMessage, deleteCreatorMessage)
                                .then(EventMessageFormatter.getPreEventEmbed(pre, settings))
                                .flatMap(em -> Messages.sendMessage(
                                    Messages.getMessage("Notification.Args.Value.Integer", settings), em, event))
                                .doOnNext(pre::setCreatorMessage);
                        }

                        pre.getRecurrence().setCount(amount);

                        return Mono.when(deleteUserMessage, deleteCreatorMessage)
                            .then(EventMessageFormatter.getPreEventEmbed(pre, settings))
                            .flatMap(em -> Messages.sendMessage(
                                Messages.getMessage("Creator.Event.Count.Success.New", settings), em, event))
                            .doOnNext(pre::setCreatorMessage);
                    } else {
                        return Mono.when(deleteUserMessage, deleteCreatorMessage)
                            .then(EventMessageFormatter.getPreEventEmbed(pre, settings))
                            .flatMap(em -> Messages.sendMessage(
                                Messages.getMessage("Creator.Event.Recur.Not", settings), em, event))
                            .doOnNext(pre::setCreatorMessage);
                    }
                } else {
                    return Mono.when(deleteUserMessage, deleteCreatorMessage)
                        .then(EventMessageFormatter.getPreEventEmbed(pre, settings))
                        .flatMap(em -> Messages.sendMessage(
                            Messages.getMessage("Creator.Event.Count.Specify", settings), em, event))
                        .doOnNext(pre::setCreatorMessage);
                }
            } else {
                return Messages.sendMessage(Messages.getMessage("Creator.Event.NotInit", settings), event);
            }
        }).then();
    }

    private Mono<Void> moduleInterval(String[] args, MessageCreateEvent event, GuildSettings settings) {
        return Mono.defer(() -> {
            if (EventCreator.getCreator().hasPreEvent(settings.getGuildID())) {
                PreEvent pre = EventCreator.getCreator().getPreEvent(settings.getGuildID());

                Mono<Void> deleteUserMessage = Messages.deleteMessage(event);
                Mono<Void> deleteCreatorMessage = Messages.deleteMessage(pre.getCreatorMessage());

                if (args.length == 2) {
                    if (pre.shouldRecur()) {
                        int amount = 1;

                        try {
                            Integer.parseInt(args[1]);
                        } catch (NumberFormatException e) {
                            return Mono.when(deleteUserMessage, deleteCreatorMessage)
                                .then(EventMessageFormatter.getPreEventEmbed(pre, settings))
                                .flatMap(em -> Messages.sendMessage(
                                    Messages.getMessage("Notification.Args.Value.Integer", settings), em, event))
                                .doOnNext(pre::setCreatorMessage);
                        }

                        pre.getRecurrence().setInterval(amount);

                        return Mono.when(deleteUserMessage, deleteCreatorMessage)
                            .then(EventMessageFormatter.getPreEventEmbed(pre, settings))
                            .flatMap(em -> Messages.sendMessage(
                                Messages.getMessage("Creator.Event.Interval.Success.New", settings), em, event))
                            .doOnNext(pre::setCreatorMessage);
                    } else {
                        return Mono.when(deleteUserMessage, deleteCreatorMessage)
                            .then(EventMessageFormatter.getPreEventEmbed(pre, settings))
                            .flatMap(em -> Messages.sendMessage(
                                Messages.getMessage("Creator.Event.Recur.Not", settings), em, event))
                            .doOnNext(pre::setCreatorMessage);
                    }
                } else {
                    return Mono.when(deleteUserMessage, deleteCreatorMessage)
                        .then(EventMessageFormatter.getPreEventEmbed(pre, settings))
                        .flatMap(em -> Messages.sendMessage(
                            Messages.getMessage("Creator.Event.Interval.Specify", settings), em, event))
                        .doOnNext(pre::setCreatorMessage);
                }
            } else {
                return Messages.sendMessage(Messages.getMessage("Creator.Event.NotInit", settings), event);
            }
        }).then();
    }
}