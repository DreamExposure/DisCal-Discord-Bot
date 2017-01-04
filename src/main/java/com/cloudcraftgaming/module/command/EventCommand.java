package com.cloudcraftgaming.module.command;

import com.cloudcraftgaming.internal.calendar.event.EventCreator;
import com.cloudcraftgaming.internal.calendar.event.EventCreatorResponse;
import com.cloudcraftgaming.internal.calendar.event.EventMessageFormatter;
import com.cloudcraftgaming.internal.calendar.event.EventUtils;
import com.cloudcraftgaming.utils.Message;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.EventDateTime;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
        if (args.length < 1) {
            Message.sendMessage("Please specify the function you would like to execute.", event, client);
        } else if (args.length == 1) {
            String function = args[0];
            String guildId = event.getMessage().getGuild().getID();
            if (function.equalsIgnoreCase("create")) {
                Message.sendMessage("Please specify a name for the event!", event, client);
            } else if (function.equalsIgnoreCase("cancel")) {
                if (EventCreator.getCreator().terminate(event)) {
                    Message.sendMessage("Event creation canceled! Event creator terminated!", event, client);
                } else {
                    Message.sendMessage("Event Creation could not be cancelled because it was never started!", event, client);
                }
            } else if (function.equalsIgnoreCase("view") || function.equalsIgnoreCase("review")) {
                if (EventCreator.getCreator().hasPreEvent(guildId)) {
                    Message.sendMessage(EventMessageFormatter.getFormatEventMessage(EventCreator.getCreator().getPreEvent(guildId)), event, client);
                    Message.sendMessage("Confirm event to add to calendar OR edit the values!", event, client);
                } else {
                    Message.sendMessage("Event Creator has not been initialized! Create an event to initialize!", event, client);
                }
            } else if (function.equalsIgnoreCase("confirm")) {
                if (EventCreator.getCreator().hasPreEvent(guildId)) {
                    if (EventCreator.getCreator().getPreEvent(guildId).hasRequiredValues()) {
                        EventCreatorResponse response = EventCreator.getCreator().confirmEvent(event);
                        if (response.isSuccessful()) {
                            Message.sendMessage("Event confirmed! " + response.getEvent().getHtmlLink(), event, client);
                        } else {
                            Message.sendMessage("Event created failed!", event, client);
                        }
                    } else {
                        Message.sendMessage("Required data not set! Please review event!", event, client);
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
            String guildId = event.getMessage().getGuild().getID();
            if (function.equalsIgnoreCase("create")) {
                if (EventCreator.getCreator().hasPreEvent(guildId)) {
                    Message.sendMessage("Event Creator already started!", event, client);
                } else {
                    EventCreator.getCreator().init(event, args[1]);
                    Message.sendMessage("Event Creator initiated! Please specify event summery.", event, client);
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
                            Date dateObj = sdf.parse(dateRaw);
                            DateTime dateTime = new DateTime(dateObj);
                            EventDateTime eventDateTime = new EventDateTime();
                            eventDateTime.setDateTime(dateTime);

                            //Date shuffling done, now actually apply all that damn stuff here.
                            EventCreator.getCreator().getPreEvent(guildId).setStartDateTime(eventDateTime);
                            Message.sendMessage("Event start date (yyyy/MM/dd) set to: " + EventMessageFormatter.getHumanReadableDate(eventDateTime), event, client);
                            Message.sendMessage("Event start time (HH:mm, military) set to: " + EventMessageFormatter.getHumanReadableTime(eventDateTime), event, client);
                            Message.sendMessage("Please specify one of the following: ", event, client);

                            Message.sendMessage("For an ALL DAY event, please specify end date in yyyy/MM/dd format!", event, client);
                            Message.sendMessage("For a TIMED EVENT event, please specify end date & ending time(military) in yyyy/MM/dd-HH:mm:ss format!", event, client);
                        } catch (ParseException e) {
                            Message.sendMessage("Invalid Date & Time specified!", event, client);
                        }
                    } else {
                        try {
                            //Do a lot of date shuffling to get to proper formats and shit like that.
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
                            Date dateObj = sdf.parse(dateRaw);
                            DateTime dateTime = new DateTime(dateObj);
                            EventDateTime eventDateTime = new EventDateTime();
                            eventDateTime.setDate(dateTime);

                            //Date shuffling done, now actually apply all that damn stuff here.
                            EventCreator.getCreator().getPreEvent(guildId).setStartDateTime(eventDateTime);
                            Message.sendMessage("Event start date (yyyy/MM/dd) set to: " + EventMessageFormatter.getHumanReadableDate(eventDateTime), event, client);

                            Message.sendMessage("Please specify one of the following: ", event, client);
                            Message.sendMessage("For an ALL DAY event, please specify end date in yyyy/MM/dd format!", event, client);
                            Message.sendMessage("For a TIMED EVENT event, please specify end date & ending time(military) in yyyy/MM/dd-HH:mm:ss format!", event, client);
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
                            Date dateObj = sdf.parse(dateRaw);
                            DateTime dateTime = new DateTime(dateObj);
                            EventDateTime eventDateTime = new EventDateTime();
                            eventDateTime.setDateTime(dateTime);

                            //Date shuffling done, now actually apply all that damn stuff here.
                            EventCreator.getCreator().getPreEvent(guildId).setEndDateTime(eventDateTime);
                            Message.sendMessage("Event end date (yyyy/MM/dd) set to: " + EventMessageFormatter.getHumanReadableDate(eventDateTime), event, client);
                            Message.sendMessage("Event end time (HH:mm, military) set to: " + EventMessageFormatter.getHumanReadableTime(eventDateTime), event, client);

                            Message.sendMessage("Event creation halted! View and/or confirm the event to make it official!", event, client);
                        } catch (ParseException e) {
                            Message.sendMessage("Invalid Date & Time specified!", event, client);
                        }
                    } else {
                        try {
                            //Do a lot of date shuffling to get to proper formats and shit like that.
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
                            Date dateObj = sdf.parse(dateRaw);
                            DateTime dateTime = new DateTime(dateObj);
                            EventDateTime eventDateTime = new EventDateTime();
                            eventDateTime.setDate(dateTime);

                            //Date shuffling done, now actually apply all that damn stuff here.
                            EventCreator.getCreator().getPreEvent(guildId).setStartDateTime(eventDateTime);
                            Message.sendMessage("Event end date (yyyy/MM/dd) set to: " + EventMessageFormatter.getHumanReadableDate(eventDateTime), event, client);

                            Message.sendMessage("Event creation halted! View and/or confirm the event to make it official!", event, client);
                        } catch (ParseException e) {
                            Message.sendMessage("Invalid Date specified!", event, client);
                        }
                    }
                } else {
                    Message.sendMessage("Event Creator has not been initialized! Create an event to initialize!", event, client);
                }
            } else if (function.equalsIgnoreCase("summery")) {
                if (EventCreator.getCreator().hasPreEvent(guildId)) {
                    String content = getContent(args);
                    EventCreator.getCreator().getPreEvent(guildId).setSummery(content);
                    Message.sendMessage("Event summery set to: '" + content + "'", event, client);
                    Message.sendMessage("Please specify the event description!", event, client);
                } else {
                    Message.sendMessage("Event Creator has not been initialized! Create an event to initialize!", event, client);
                }
            } else if (function.equalsIgnoreCase("description")) {
                if (EventCreator.getCreator().hasPreEvent(guildId)) {
                    String content = getContent(args);
                    EventCreator.getCreator().getPreEvent(guildId).setDescription(content);
                    Message.sendMessage("Event description set to: '" + content + "'", event, client);
                    Message.sendMessage("Please specify one of the following: ", event, client);
                    Message.sendMessage("For an ALL DAY event, please specify start date in yyyy/MM/dd format!", event, client);
                    Message.sendMessage("For a TIMED EVENT event, please specify start date & starting time(military) in yyyy/MM/dd-HH:mm:ss format!", event, client);
                } else {
                    Message.sendMessage("Event Creator has not been initialized! Create an event to initialize!", event, client);
                }
            } else {
                Message.sendMessage("Invalid function!", event, client);
            }
        } else {
            String function = args[0];
            String guildId = event.getMessage().getGuild().getID();
            if (function.equalsIgnoreCase("create")) {
                Message.sendMessage("Event name can only be one(1) word!", event, client);
            } else if (function.equalsIgnoreCase("summery")) {
                if (EventCreator.getCreator().hasPreEvent(guildId)) {
                    String content = getContent(args);
                    EventCreator.getCreator().getPreEvent(guildId).setSummery(content);
                    Message.sendMessage("Event summery set to: '" + content + "'", event, client);
                    Message.sendMessage("Please specify the event description!", event, client);
                } else {
                    Message.sendMessage("Event Creator has not been initialized! Create an event to initialize!", event, client);
                }
            } else if (function.equalsIgnoreCase("description")) {
                if (EventCreator.getCreator().hasPreEvent(guildId)) {
                    String content = getContent(args);
                    EventCreator.getCreator().getPreEvent(guildId).setDescription(content);
                    Message.sendMessage("Event description set to: '" + content + "'", event, client);
                    Message.sendMessage("Please specify one of the following: ", event, client);
                    Message.sendMessage("For an ALL DAY event, please specify date in yyyy/MM/dd format!", event, client);
                    Message.sendMessage("For a TIMED EVENT event, please specify date & starting time(military) in yyyy/MM/dd-HH:mm:ss format!", event, client);
                } else {
                    Message.sendMessage("Event Creator has not been initialized! Create an event to initialize!", event, client);
                }
            } else {
                Message.sendMessage("Invalid function!", event, client);
            }
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
