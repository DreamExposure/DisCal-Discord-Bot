package com.cloudcraftgaming.module.command;

import com.cloudcraftgaming.database.DatabaseManager;
import com.cloudcraftgaming.internal.calendar.CalendarAuth;
import com.cloudcraftgaming.internal.calendar.event.EventCreator;
import com.cloudcraftgaming.internal.calendar.event.EventCreatorResponse;
import com.cloudcraftgaming.internal.calendar.event.EventMessageFormatter;
import com.cloudcraftgaming.internal.calendar.event.EventUtils;
import com.cloudcraftgaming.internal.data.BotData;
import com.cloudcraftgaming.utils.Message;
import com.cloudcraftgaming.utils.PermissionChecker;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Nova Fox on 1/3/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
@SuppressWarnings("Duplicates")
public class EventCommand implements ICommand {
    @Override
    public String getCommand() {
        return "event";
    }

    @Override
    public Boolean issueCommand(String[] args, MessageReceivedEvent event, IDiscordClient client) {
        String guildId = event.getMessage().getGuild().getID();
        if (PermissionChecker.hasSufficientRole(event)) {
            if (args.length < 1) {
                Message.sendMessage("Please specify the function you would like to execute.", event, client);
            } else if (args.length == 1) {
                String function = args[0];
                if (function.equalsIgnoreCase("create")) {
                    if (EventCreator.getCreator().hasPreEvent(guildId)) {
                        Message.sendMessage("Event Creator already started!", event, client);
                    } else {
                        EventCreator.getCreator().init(event);
                        Message.sendMessage("Event Creator initiated! Please specify event summary.", event, client);
                    }
                } else if (function.equalsIgnoreCase("cancel")) {
                    if (EventCreator.getCreator().terminate(event)) {
                        Message.sendMessage("Event creation canceled! Event creator terminated!", event, client);
                    } else {
                        Message.sendMessage("Event Creation could not be cancelled because it was never started!", event, client);
                    }
                } else if (function.equalsIgnoreCase("view") || function.equalsIgnoreCase("review")) {
                    if (EventCreator.getCreator().hasPreEvent(guildId)) {
                        Message.sendMessage(EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(guildId)), "Confirm event `!event confirm` to add to calendar OR edit the values!", event, client);
                    } else {
                        Message.sendMessage("To review an event you must have the event creator initialized OR use `!event view <event ID>` to view an event in the calendar!", event, client);
                    }
                } else if (function.equalsIgnoreCase("confirm")) {
                    if (EventCreator.getCreator().hasPreEvent(guildId)) {
                        if (EventCreator.getCreator().getPreEvent(guildId).hasRequiredValues()) {
                            EventCreatorResponse response = EventCreator.getCreator().confirmEvent(event);
                            if (response.isSuccessful()) {
                                Message.sendMessage(EventMessageFormatter.getEventConfirmationEmbed(response), "Event confirmed!", event, client);
                            } else {
                                Message.sendMessage("Event created failed!", event, client);
                            }
                        } else {
                            Message.sendMessage("Required data not set! Please review event with `!event review`", event, client);
                        }
                    } else {
                        Message.sendMessage("Event Creator has not been initialized! Create an event to initialize!", event, client);
                    }
                } else if (function.equalsIgnoreCase("delete")) {
                    if (!EventCreator.getCreator().hasPreEvent(guildId)) {
                        Message.sendMessage("Please specify the Id of the event to delete!", event, client);
                    } else {
                        Message.sendMessage("You cannot delete an event while in the creator!", event, client);
                    }
                }
            } else if (args.length == 2) {
                String function = args[0];
                if (function.equalsIgnoreCase("create")) {
                    if (EventCreator.getCreator().hasPreEvent(guildId)) {
                        Message.sendMessage("Event Creator already started!", event, client);
                    } else {
                        EventCreator.getCreator().init(event);
                        Message.sendMessage("Event Creator initiated! Please specify event summary.", event, client);
                    }
                } else if (function.equalsIgnoreCase("view")) {
                    if (!EventCreator.getCreator().hasPreEvent(guildId)) {
                        //Try to get the event by ID.
                        try {
                            Calendar service = CalendarAuth.getCalendarService();
                            BotData data = DatabaseManager.getManager().getData(guildId);
                            Event calEvent = service.events().get(data.getCalendarAddress(), args[1]).execute();
                            Message.sendMessage(EventMessageFormatter.getEventEmbed(calEvent, guildId), event, client);
                        } catch (IOException e) {
                            //Event probably doesn't exist...
                            Message.sendMessage("Oops! Something went wrong! Are you sure the event ID is correct?", event, client);
                        }
                    } else {
                        Message.sendMessage("The event creator is active! You cannot view another event while the creator is active!", event, client);
                    }
                } else if (function.equalsIgnoreCase("delete")) {
                    if (!EventCreator.getCreator().hasPreEvent(guildId)) {
                        if (EventUtils.deleteEvent(guildId, args[1])) {
                            Message.sendMessage("Event successfully deleted!", event, client);
                        } else {
                            Message.sendMessage("Failed to delete event! Is the Event ID correct?", event, client);
                        }
                    } else {
                        Message.sendMessage("You cannot delete an event while in the creator!", event, client);
                    }
                } else if (function.equalsIgnoreCase("startDate")) {
                    if (EventCreator.getCreator().hasPreEvent(guildId)) {
                        String dateRaw = args[1].trim();
                        if (dateRaw.length() > 10) {
                            try {
                                //Do a lot of date shuffling to get to proper formats and shit like that.
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss");
                                sdf.setTimeZone(TimeZone.getTimeZone(EventCreator.getCreator().getPreEvent(guildId).getTimeZone()));
                                Date dateObj = sdf.parse(dateRaw);
                                DateTime dateTime = new DateTime(dateObj);
                                EventDateTime eventDateTime = new EventDateTime();
                                eventDateTime.setDateTime(dateTime);

                                //Date shuffling done, now actually apply all that damn stuff here.
                                EventCreator.getCreator().getPreEvent(guildId).setStartDateTime(eventDateTime);

                                //Apply viewable date/times...
                                SimpleDateFormat sdfV = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss");
                                Date dateObjV = sdfV.parse(dateRaw);
                                DateTime dateTimeV = new DateTime(dateObjV);
                                EventDateTime eventDateTimeV = new EventDateTime();
                                eventDateTimeV.setDateTime(dateTimeV);
                                EventCreator.getCreator().getPreEvent(guildId).setViewableStartDate(eventDateTimeV);

                                Message.sendMessage("Event start date (yyyy/MM/dd) set to: `" +
                                        EventMessageFormatter.getHumanReadableDate(eventDateTimeV) + "`"
                                        + Message.lineBreak
                                        + "Event start time (HH:mm) set to: `"
                                        + EventMessageFormatter.getHumanReadableTime(eventDateTimeV) + "`"
                                        + Message.lineBreak + Message.lineBreak
                                        + "Please specify the following: "
                                        + Message.lineBreak
                                        + "For a TIMED EVENT event, please specify end date & ending time(military) in `yyyy/MM/dd-HH:mm:ss` format!", event, client);
                            } catch (ParseException e) {
                                Message.sendMessage("Invalid Date & Time specified!", event, client);
                            }
                        } else {
                            try {
                                //Do a lot of date shuffling to get to proper formats and shit like that.
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
                                sdf.setTimeZone(TimeZone.getTimeZone(EventCreator.getCreator().getPreEvent(guildId).getTimeZone()));
                                Date dateObj = sdf.parse(dateRaw);
                                DateTime dateTime = new DateTime(dateObj);
                                EventDateTime eventDateTime = new EventDateTime();
                                eventDateTime.setDate(dateTime);

                                //Date shuffling done, now actually apply all that damn stuff here.
                                EventCreator.getCreator().getPreEvent(guildId).setStartDateTime(eventDateTime);

                                //Apply viewable date/times...
                                SimpleDateFormat sdfV = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss");
                                Date dateObjV = sdfV.parse(dateRaw);
                                DateTime dateTimeV = new DateTime(dateObjV);
                                EventDateTime eventDateTimeV = new EventDateTime();
                                eventDateTimeV.setDateTime(dateTimeV);
                                EventCreator.getCreator().getPreEvent(guildId).setViewableStartDate(eventDateTimeV);
                                Message.sendMessage("Event start date (yyyy/MM/dd) set to: `" + EventMessageFormatter.getHumanReadableDate(eventDateTimeV) + "`"
                                        + Message.lineBreak + Message.lineBreak
                                        + "Please specify the following: "
                                        + "End date & ending time(military) in `yyyy/MM/dd-HH:mm:ss` format!", event, client);
                            } catch (ParseException e) {
                                Message.sendMessage("Invalid Date specified!", event, client);
                            }
                        }
                    } else {
                        Message.sendMessage("Event Creator has not been initialized! Create an event to initialize!", event, client);
                    }
                } else if (function.equalsIgnoreCase("endDate")) {
                    if (EventCreator.getCreator().hasPreEvent(guildId)) {
                        String dateRaw = args[1].trim();
                        if (dateRaw.length() > 10) {
                            try {
                                //Do a lot of date shuffling to get to proper formats and shit like that.
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss");
                                sdf.setTimeZone(TimeZone.getTimeZone(EventCreator.getCreator().getPreEvent(guildId).getTimeZone()));
                                Date dateObj = sdf.parse(dateRaw);
                                DateTime dateTime = new DateTime(dateObj);
                                EventDateTime eventDateTime = new EventDateTime();
                                eventDateTime.setDateTime(dateTime);

                                //Date shuffling done, now actually apply all that damn stuff here.
                                EventCreator.getCreator().getPreEvent(guildId).setEndDateTime(eventDateTime);

                                //Apply viewable date/times...
                                SimpleDateFormat sdfV = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss");
                                Date dateObjV = sdfV.parse(dateRaw);
                                DateTime dateTimeV = new DateTime(dateObjV);
                                EventDateTime eventDateTimeV = new EventDateTime();
                                eventDateTimeV.setDateTime(dateTimeV);
                                EventCreator.getCreator().getPreEvent(guildId).setViewableEndDate(eventDateTimeV);
                                Message.sendMessage("Event end date (yyyy/MM/dd) set to: `" + EventMessageFormatter.getHumanReadableDate(eventDateTimeV) + "`"
                                        + Message.lineBreak
                                        + "Event end time (HH:mm) set to: `"
                                        + EventMessageFormatter.getHumanReadableTime(eventDateTimeV) + "`"
                                        + Message.lineBreak + Message.lineBreak
                                        + "Event creation halted! View `!event review` and/or confirm the event `!event confirm` to make it official!", event, client);
                            } catch (ParseException e) {
                                Message.sendMessage("Invalid Date & Time specified!", event, client);
                            }
                        } else {
                            try {
                                //Do a lot of date shuffling to get to proper formats and shit like that.
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
                                sdf.setTimeZone(TimeZone.getTimeZone(EventCreator.getCreator().getPreEvent(guildId).getTimeZone()));
                                Date dateObj = sdf.parse(dateRaw);
                                DateTime dateTime = new DateTime(dateObj);
                                EventDateTime eventDateTime = new EventDateTime();
                                eventDateTime.setDate(dateTime);

                                //Date shuffling done, now actually apply all that damn stuff here.
                                EventCreator.getCreator().getPreEvent(guildId).setEndDateTime(eventDateTime);

                                //Apply viewable date/times...
                                SimpleDateFormat sdfV = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss");
                                Date dateObjV = sdfV.parse(dateRaw);
                                DateTime dateTimeV = new DateTime(dateObjV);
                                EventDateTime eventDateTimeV = new EventDateTime();
                                eventDateTimeV.setDateTime(dateTimeV);
                                EventCreator.getCreator().getPreEvent(guildId).setViewableEndDate(eventDateTimeV);

                                Message.sendMessage("Event end date (yyyy/MM/dd) set to: `"
                                        + EventMessageFormatter.getHumanReadableDate(eventDateTime) + "`"
                                        + Message.lineBreak + Message.lineBreak
                                        + "Event creation halted! View `!event review` and/or confirm the event `!event confirm` to make it official!", event, client);
                            } catch (ParseException e) {
                                Message.sendMessage("Invalid Date specified!", event, client);
                            }
                        }
                    } else {
                        Message.sendMessage("Event Creator has not been initialized! Create an event to initialize!", event, client);
                    }
                } else if (function.equalsIgnoreCase("summary")) {
                    if (EventCreator.getCreator().hasPreEvent(guildId)) {
                        String content = getContent(args);
                        EventCreator.getCreator().getPreEvent(guildId).setSummary(content);
                        Message.sendMessage("Event summary set to: '" + content + "'"
                                + Message.lineBreak + Message.lineBreak
                                + "Please specify the event description with `!event description <desc>`", event, client);
                    } else {
                        Message.sendMessage("Event Creator has not been initialized! Create an event to initialize!", event, client);
                    }
                } else if (function.equalsIgnoreCase("description")) {
                    if (EventCreator.getCreator().hasPreEvent(guildId)) {
                        String content = getContent(args);
                        EventCreator.getCreator().getPreEvent(guildId).setDescription(content);
                        Message.sendMessage("Event description set to: '" + content + "'"
                                + Message.lineBreak + Message.lineBreak
                                + "Please specify the following: "
                                + Message.lineBreak
                                + "Start date & starting time(military) in `yyyy/MM/dd-HH:mm:ss` format!", event, client);
                    } else {
                        Message.sendMessage("Event Creator has not been initialized! Create an event to initialize!", event, client);
                    }
                } else {
                    Message.sendMessage("Invalid function!", event, client);
                }
            } else {
                String function = args[0];
                if (function.equalsIgnoreCase("create")) {
                    if (EventCreator.getCreator().hasPreEvent(guildId)) {
                        Message.sendMessage("Event Creator already started!", event, client);
                    } else {
                        EventCreator.getCreator().init(event);
                        Message.sendMessage("Event Creator initiated! Please specify event summary with `!event summary <summary>`", event, client);
                    }
                } else if (function.equalsIgnoreCase("summary")) {
                    if (EventCreator.getCreator().hasPreEvent(guildId)) {
                        String content = getContent(args);
                        EventCreator.getCreator().getPreEvent(guildId).setSummary(content);
                        Message.sendMessage("Event summary set to: '" + content + "'"
                                + Message.lineBreak + Message.lineBreak
                                + "Please specify the event description with `!event description <desc>`", event, client);
                    } else {
                        Message.sendMessage("Event Creator has not been initialized! Create an event to initialize!", event, client);
                    }
                } else if (function.equalsIgnoreCase("description")) {
                    if (EventCreator.getCreator().hasPreEvent(guildId)) {
                        String content = getContent(args);
                        EventCreator.getCreator().getPreEvent(guildId).setDescription(content);
                        Message.sendMessage("Event description set to: '" + content + "'"
                                + Message.lineBreak + Message.lineBreak
                                + "Please specify the following: "
                                + Message.lineBreak
                                + "Starting date & starting time(military) in `yyyy/MM/dd-HH:mm:ss` format!", event, client);
                    } else {
                        Message.sendMessage("Event Creator has not been initialized! Create an event to initialize!", event, client);
                    }
                } else {
                    Message.sendMessage("Invalid function!", event, client);
                }
            }
        } else {
            Message.sendMessage("You do not have sufficient permissions to use this DisCal command!", event, client);
        }
        return false;
    }

    private String getContent(String[] args) {
        String content = "";
        for (int i = 1; i < args.length; i++) {
            content = content + args[i] + " ";
        }
        return content.trim();
    }
}