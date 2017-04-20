package com.cloudcraftgaming.discal.module.command;

import com.cloudcraftgaming.discal.database.DatabaseManager;
import com.cloudcraftgaming.discal.internal.calendar.CalendarAuth;
import com.cloudcraftgaming.discal.internal.calendar.event.*;
import com.cloudcraftgaming.discal.internal.data.CalendarData;
import com.cloudcraftgaming.discal.internal.data.GuildSettings;
import com.cloudcraftgaming.discal.module.command.info.CommandInfo;
import com.cloudcraftgaming.discal.utils.*;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Nova Fox on 1/3/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
@SuppressWarnings("Duplicates")
public class EventCommand implements ICommand {
    /**
     * Gets the command this Object is responsible for.
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
        info.setDescription("Used for all event related functions");
        info.setExample("!event <function> (value(s))");

        info.getSubCommands().add("create");
        info.getSubCommands().add("copy");
        info.getSubCommands().add("edit");
        info.getSubCommands().add("cancel");
        info.getSubCommands().add("delete");
        info.getSubCommands().add("view");
        info.getSubCommands().add("review");
        info.getSubCommands().add("confirm");
        info.getSubCommands().add("start");
        info.getSubCommands().add("startDate");
        info.getSubCommands().add("end");
        info.getSubCommands().add("endDate");
        info.getSubCommands().add("summary");
        info.getSubCommands().add("description");
        info.getSubCommands().add("color");
        info.getSubCommands().add("colour");
        info.getSubCommands().add("recur");
        info.getSubCommands().add("frequency");
        info.getSubCommands().add("freq");
        info.getSubCommands().add("count");
        info.getSubCommands().add("interval");

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
        String guildId = event.getMessage().getGuild().getID();
        //TODO: Add multiple calendar handling.
        CalendarData calendarData = DatabaseManager.getManager().getMainCalendar(guildId);
        GuildSettings settings = DatabaseManager.getManager().getSettings(guildId);
        if (PermissionChecker.hasSufficientRole(event)) {
            if (args.length < 1) {
                Message.sendMessage(MessageManager.getMessage("Notification.Args.Few", event), event);
            } else {
                switch (args[0].toLowerCase()) {
                    case "create":
                        moduleCreate(event, calendarData);
                        break;
                    case "copy":
                        moduleCopy(args, event, calendarData);
                        break;
                    case "edit":
                        if (settings.isDevGuild()) {
                            moduleEdit(args, event, calendarData);
                        } else {
                            Message.sendMessage(MessageManager.getMessage("Notification.Disabled", event), event);
                        }
                        break;
                    case "cancel":
                        moduleCancel(event);
                        break;
                    case "delete":
                        moduleDelete(args, event, calendarData);
                        break;
                    case "view":
                        moduleView(args, event, calendarData);
                        break;
                    case "review":
                        moduleView(args, event, calendarData);
                        break;
                    case "confirm":
                        moduleConfirm(event, calendarData);
                        break;
                    case "startdate":
                        moduleStartDate(args, event);
                        break;
                    case "start":
                        moduleStartDate(args, event);
                        break;
                    case "enddate":
                        moduleEndDate(args, event);
                        break;
                    case "end":
                        moduleEndDate(args, event);
                        break;
                    case "summary":
                        moduleSummary(args, event);
                        break;
                    case "description":
                        moduleDescription(args, event);
                        break;
                    case "color":
                        moduleColor(args, event);
                        break;
                    case "colour":
                        moduleColor(args, event);
                        break;
                    case "recur":
                        moduleRecur(args, event);
                        break;
                    case "frequency":
                        moduleFrequency(args, event);
                        break;
                    case "freq":
                        moduleFrequency(args, event);
                        break;
                    case "count":
                        moduleCount(args, event);
                        break;
                    case "interval":
                        moduleInterval(args, event);
                        break;
                    default:
                        Message.sendMessage(MessageManager.getMessage("Notification.Args.Invalid", event), event);
                        break;
                }
            }
        } else {
            Message.sendMessage(MessageManager.getMessage("Notification.Perm.CONTROL_ROLE", event), event);
        }
        return false;
    }


    private void moduleCreate(MessageReceivedEvent event, CalendarData calendarData) {
        String guildId = event.getMessage().getGuild().getID();
        if (EventCreator.getCreator().hasPreEvent(guildId)) {
            Message.sendMessage("Event Creator already started!", event);
        } else {
            if (!calendarData.getCalendarAddress().equalsIgnoreCase("primary")) {
                PreEvent e = EventCreator.getCreator().init(event, true);
                if (e.getCreatorMessage() == null) {
					Message.sendMessage("Event Creator initiated! Please specify event summary.", event);
				}
            } else {
                Message.sendMessage("You cannot create an event when you do not have a calendar!", event);
            }
        }
    }

    private void moduleCopy(String[] args, MessageReceivedEvent event, CalendarData calendarData) {
        String guildId = event.getMessage().getGuild().getID();
        if (!calendarData.getCalendarAddress().equalsIgnoreCase("primary")) {
            if (!EventCreator.getCreator().hasPreEvent(guildId)) {
                if (args.length == 2) {
                    String eventId = args[1];
                    if (EventUtils.eventExists(guildId, eventId)) {
                        PreEvent preEvent = EventCreator.getCreator().init(event, eventId, true);
                        if (preEvent != null) {
                        	if (preEvent.getCreatorMessage() == null) {
								Message.sendMessage(EventMessageFormatter.getPreEventEmbed(preEvent), "Event Creator initialized! Event details copied! Please specify the date/times!", event);
							}
                        } else {
                            Message.sendMessage("Something went wrong! I'm sorry, try again!", event);
                        }
                    } else {
                        Message.sendMessage("I can't find that event! Are you sure the ID is correct?", event);
                    }
                } else {
                    Message.sendMessage("Please input the ID of the event to copy with `!event copy <ID>`", event);
                }
            } else {
                Message.sendMessage("Event Creator already initialized!", event);
            }
        } else {
            Message.sendMessage("Cannot copy event when you do not have a calendar!", event);
        }
    }

    private void moduleEdit(String[] args, MessageReceivedEvent event, CalendarData calendarData) {
        String guildId = event.getMessage().getGuild().getID();
        if (!calendarData.getCalendarAddress().equalsIgnoreCase("primary")) {
            if (!EventCreator.getCreator().hasPreEvent(guildId)) {
                if (args.length == 2) {
                    String eventId = args[1];
                    if (EventUtils.eventExists(guildId, eventId)) {
                        PreEvent preEvent = EventCreator.getCreator().edit(event, eventId, true);
						if (preEvent.getCreatorMessage() == null) {
							Message.sendMessage(EventMessageFormatter.getPreEventEmbed(preEvent), "Event Editor initiated! Edit the values and then confirm your edits with `!event confirm`", event);
						}
                    } else {
                        Message.sendMessage("I can't find that event! Are you sure the ID is correct?", event);
                    }
                } else {
                    Message.sendMessage("Please input the ID of the event to edit with `!event edit <ID>`", event);
                }
            } else {
                Message.sendMessage("Event Creator already initialized!", event);
            }
        } else {
            Message.sendMessage("Cannot edit event when you do not have a calendar", event);
        }
    }

    private void moduleCancel(MessageReceivedEvent event) {
        if (EventCreator.getCreator().terminate(event)) {
            Message.sendMessage("Event creation canceled! Event creator terminated!", event);
        } else {
            Message.sendMessage("Event Creation could not be cancelled because it was never started!", event);
        }
    }

    private void moduleDelete(String[] args, MessageReceivedEvent event, CalendarData calendarData) {
        String guildId = event.getMessage().getGuild().getID();
        if (args.length == 2) {
            if (!calendarData.getCalendarAddress().equalsIgnoreCase("primary")) {
                if (!EventCreator.getCreator().hasPreEvent(guildId)) {
                    if (EventUtils.deleteEvent(guildId, args[1])) {
                        Message.sendMessage("Event successfully deleted!", event);
                    } else {
                        Message.sendMessage("Failed to delete event! Is the Event ID correct?", event);
                    }
                } else {
                    Message.sendMessage("You cannot delete an event while in the creator!", event);
                }
            } else {
                Message.sendMessage("You cannot delete an event when you do not have a calendar!", event);
            }
        } else {
            Message.sendMessage("Please specify the ID of the event to delete with `!event delete <ID>`", event);
        }
    }

    private void moduleView(String[] args, MessageReceivedEvent event, CalendarData calendarData) {
        String guildId = event.getMessage().getGuild().getID();
        if (args.length == 1) {
            if (EventCreator.getCreator().hasPreEvent(guildId)) {
                Message.sendMessage(EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(guildId)), "Confirm event `!event confirm` to add to calendar OR edit the values!", event);
            } else {
                Message.sendMessage("To review an event you must have the event creator initialized OR use `!event view <event ID>` to view an event in the calendar!", event);
            }
        } else if (args.length == 2) {
            //Try to get the event by ID.
            if (!EventCreator.getCreator().hasPreEvent(guildId)) {
                if (!calendarData.getCalendarAddress().equalsIgnoreCase("primary")) {
                    try {
                        Calendar service = CalendarAuth.getCalendarService();
                        Event calEvent = service.events().get(calendarData.getCalendarAddress(), args[1]).execute();
                        Message.sendMessage(EventMessageFormatter.getEventEmbed(calEvent, guildId), event);
                    } catch (IOException e) {
                        //Event probably doesn't exist...
                        Message.sendMessage("Oops! Something went wrong! Are you sure the event ID is correct?", event);
                    }
                } else {
                    Message.sendMessage("You cannot view an event when you do not have a calendar!", event);
                }
            } else {
                Message.sendMessage("The event creator is active! You cannot view another event while the creator is active!", event);
            }
        } else {
            Message.sendMessage("Please specify the ID of the event you wish to view with `!event view <ID>`", event);
        }
    }

    private void moduleConfirm(MessageReceivedEvent event, CalendarData calendarData) {
        String guildId = event.getMessage().getGuild().getID();
        if (EventCreator.getCreator().hasPreEvent(guildId)) {
            if (EventCreator.getCreator().getPreEvent(guildId).hasRequiredValues()) {
                if (!calendarData.getCalendarAddress().equalsIgnoreCase("primary")) {
                    EventCreatorResponse response = EventCreator.getCreator().confirmEvent(event);
                    if (response.isSuccessful()) {
                        Message.sendMessage(EventMessageFormatter.getEventConfirmationEmbed(response), "Event confirmed!", event);
                    } else {
                        Message.sendMessage("Event created failed!", event);
                    }
                } else {
                    Message.sendMessage("You cannot confirm an event when you do not have a calendar!", event);
                }
            } else {
                Message.sendMessage("Required data not set! Please review event with `!event review`", event);
            }
        } else {
            Message.sendMessage("Event Creator has not been initialized! Create an event to initialize!", event);
        }
    }

    private void moduleStartDate(String[] args, MessageReceivedEvent event) {
        String guildId = event.getMessage().getGuild().getID();
        if (args.length == 2) {
            if (EventCreator.getCreator().hasPreEvent(guildId)) {
                String dateRaw = args[1].trim();
                if (dateRaw.length() > 10) {
                    try {
                        //Do a lot of date shuffling to get to proper formats and shit like that.
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss");
                        TimeZone tz = TimeZone.getTimeZone(EventCreator.getCreator().getPreEvent(guildId).getTimeZone());
                        sdf.setTimeZone(tz);
                        Date dateObj = sdf.parse(dateRaw);
                        DateTime dateTime = new DateTime(dateObj);
                        EventDateTime eventDateTime = new EventDateTime();
                        eventDateTime.setDateTime(dateTime);

                        //Wait! Lets check now if its in the future and not the past!
                        if (!Validator.inPast(dateRaw, tz) && !Validator.startAfterEnd(dateRaw, tz, EventCreator.getCreator().getPreEvent(guildId))) {
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
                                    + "End date & ending time(military) in `yyyy/MM/dd-HH:mm:ss` format with the command `!event end <DateAndTime>`", event);
                        } else {
                            //Oops! Time is in the past or after end...
                            Message.sendMessage("Sorry >.< but I can't schedule an event that is in the past or has a starting time that is after the ending time!!! Please make sure you typed everything correctly.", event);
                        }
                    } catch (ParseException e) {
                        Message.sendMessage("Invalid Date & Time specified!", event);
                    }
                } else {
                    Message.sendMessage("Invalid date/time format! Use `yyyy/MM/dd-HH:mm:ss`", event);
                }
            } else {
                Message.sendMessage("Event Creator has not been initialized! Create an event to initialize!", event);
            }
        } else {
            Message.sendMessage("Please specify the start date/time with `!event start <date/time>`", event);
        }
    }

    private void moduleEndDate(String[] args, MessageReceivedEvent event) {
        String guildId = event.getMessage().getGuild().getID();
        if (args.length == 2) {
            if (EventCreator.getCreator().hasPreEvent(guildId)) {
                String dateRaw = args[1].trim();
                if (dateRaw.length() > 10) {
                    try {
                        //Do a lot of date shuffling to get to proper formats and shit like that.
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss");
                        TimeZone tz = TimeZone.getTimeZone(EventCreator.getCreator().getPreEvent(guildId).getTimeZone());
                        sdf.setTimeZone(tz);
                        Date dateObj = sdf.parse(dateRaw);
                        DateTime dateTime = new DateTime(dateObj);
                        EventDateTime eventDateTime = new EventDateTime();
                        eventDateTime.setDateTime(dateTime);

                        //Wait! Lets check now if its in the future and not the past!
                        if (!Validator.inPast(dateRaw, tz) && !Validator.endBeforeStart(dateRaw, tz, EventCreator.getCreator().getPreEvent(guildId))) {
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
                                    + "If you would like a specific color for your event use `!event color <name OR id>` to list all colors use `!event color list`" + Message.lineBreak + Message.lineBreak + "If you would like to make this event recurring please use `!event recur true`" + Message.lineBreak + Message.lineBreak + "Otherwise use `!event review` to review the event!", event);
                        } else {
                            //Oops! Time is in the past or before the starting time...
                            Message.sendMessage("Sorry >.< but I can't schedule an event that is in the past or has an ending before the starting time!!! Please make sure you typed everything correctly.", event);
                        }
                    } catch (ParseException e) {
                        Message.sendMessage("Invalid Date & Time specified!", event);
                    }
                } else {
                    Message.sendMessage("Invalid date/time format! Use `yyyy/MM/dd-HH:mm:ss`", event);
                }
            } else {
                Message.sendMessage("Event Creator has not been initialized! Create an event to initialize!", event);
            }
        } else {
            Message.sendMessage("Please specify the end/date time with `!event end <Date/Time>`", event);
        }
    }

    private void moduleSummary(String[] args, MessageReceivedEvent event) {
        String guildId = event.getMessage().getGuild().getID();
        if (args.length > 1) {
            if (EventCreator.getCreator().hasPreEvent(guildId)) {
                String content = GeneralUtils.getContent(args, 1);
                EventCreator.getCreator().getPreEvent(guildId).setSummary(content);
                Message.sendMessage("Event summary set to: ```" + content + "```"
                        + Message.lineBreak + Message.lineBreak
                        + "Please specify the event description with `!event description <desc>`", event);
            } else {
                Message.sendMessage("Event Creator has not been initialized! Create an event to initialize!", event);
            }
        } else {
            Message.sendMessage("Please specify the event summary with `!event summary <summary, spaces allowed>`", event);
        }
    }

    private void moduleDescription(String[] args, MessageReceivedEvent event) {
        String guildId = event.getMessage().getGuild().getID();
        if (args.length  > 1) {
            if (EventCreator.getCreator().hasPreEvent(guildId)) {
                String content = GeneralUtils.getContent(args, 1);
                EventCreator.getCreator().getPreEvent(guildId).setDescription(content);
                Message.sendMessage("Event description set to: '" + content + "'"
                        + Message.lineBreak + Message.lineBreak
                        + "Please specify the following: "
                        + Message.lineBreak
                        + "Starting date & starting time(military) in `yyyy/MM/dd-HH:mm:ss` format with the command `!event start <DateAndTime>`", event);
            } else {
                Message.sendMessage("Event Creator has not been initialized! Create an event to initialize!", event);
            }
        } else {
            Message.sendMessage("Please specify the description with `!event description <desc, spaces allowed>`", event);
        }
    }

    private void moduleColor(String[] args, MessageReceivedEvent event) {
        String guildId = event.getMessage().getGuild().getID();
        if (args.length == 2) {
            String value = args[1];
            if (value.equalsIgnoreCase("list") || value.equalsIgnoreCase("colors") || value.equalsIgnoreCase("colours")) {

                StringBuilder list = new StringBuilder("All Colors: ");
                for (EventColor ec : EventColor.values()) {
                    list.append(Message.lineBreak).append("Name: ").append(ec.name()).append(", ID: ").append(ec.getId());
                }
                list.append(Message.lineBreak).append(Message.lineBreak).append("Use `!event color <name OR ID>` to set an event's color!");

                Message.sendMessage(list.toString().trim(), event);
            } else {
                if (EventCreator.getCreator().hasPreEvent(guildId)) {
                    //Attempt to get color.
                    if (EventColor.exists(value)) {
                        EventColor color = EventColor.fromNameOrHexOrID(value);
                        EventCreator.getCreator().getPreEvent(guildId).setColor(color);
                        Message.sendMessage("Event color set to: `" + color.name() + "`" + Message.lineBreak + Message.lineBreak + "Review the event with `!event review` to verify everything is correct and then confirm it with `!event confirm`", event);
                    } else {
                        Message.sendMessage("Invalid/Unsupported color! Use `!event color list` to view all supported colors!", event);
                    }
                } else {
                    Message.sendMessage("Event Creator has not been initialized! Create an event to initialize!", event);
                }
            }
        } else {
            Message.sendMessage("Please specify the color or function with `!event color <color/function>`", event);
        }
    }

    //Event recurrence settings
    private void moduleRecur(String[] args, MessageReceivedEvent event) {
        String guildId = event.getMessage().getGuild().getID();
        if (args.length == 2) {
            String valueString = args[1];
            if (EventCreator.getCreator().hasPreEvent(guildId)) {
                PreEvent pre = EventCreator.getCreator().getPreEvent(guildId);
                if (pre.isEditing() && pre.getEventId().contains("_")) {
                    Message.sendMessage("You cannot set the Recurring rules for a child event of a recurring event! Use `!event edit " + pre.getEventId().split("_")[0] + "` to edit the recurring settings!",event);
                    return;
                }
                try {
                    boolean value = Boolean.valueOf(valueString);
                    EventCreator.getCreator().getPreEvent(guildId).setShouldRecur(value);
                    if (value) {
                        Message.sendMessage("Event will recur! Please specify the frequency it will recur with `!event freq <TYPE>`", event);
                    } else {
                        Message.sendMessage("Event will not recur!", event);
                    }
                } catch (Exception e) {
                    //Could not convert to boolean
                    Message.sendMessage("Acceptable values are only `true` or `false`", event);
                }
            } else {
                Message.sendMessage("Event Creator has not been initialized!", event);
            }
        } else {
            Message.sendMessage("Please specify if the event should recur with `!event recur <true/false>`", event);
        }
    }

    private void moduleFrequency(String[] args, MessageReceivedEvent event) {
        String guildId = event.getMessage().getGuild().getID();
        if (args.length == 2) {
            if (EventCreator.getCreator().hasPreEvent(guildId)) {
                if (EventCreator.getCreator().getPreEvent(guildId).shouldRecur()) {
                    String value = args[1];
                    if (EventFrequency.isValid(value)) {
                        EventFrequency freq = EventFrequency.fromValue(value);
                        EventCreator.getCreator().getPreEvent(guildId).getRecurrence().setFrequency(freq);
                        Message.sendMessage("Event frequency set to: `" + freq.name() + "`" + Message.lineBreak + "Please specify how many times this event should recur with `!event count <amount>` Use `-1` for infinite!", event);
                    } else {
                        String values = Arrays.toString(EventFrequency.values()).replace("[", "").replace("]", "");
                        Message.sendMessage("Invalid frequency type specified! Valid types are as follows: `" + values + "`", event);
                    }
                } else {
                    Message.sendMessage("Event is not recurring, use `!event recur true` to enable recurring!", event);
                }
            } else {
                Message.sendMessage("Event Creator not initialized!", event);
            }
        } else {
            String values = Arrays.toString(EventFrequency.values()).replace("[", "").replace("]", "");
            Message.sendMessage("Pleas specify the frequency with `!event freq <TYPE>`! Valid types are as follows: `" + values + "`", event);
        }
    }

    private void moduleCount(String[] args, MessageReceivedEvent event) {
        String guildId = event.getMessage().getGuild().getID();
        if (args.length == 2) {
            if (EventCreator.getCreator().hasPreEvent(guildId)) {
                if (EventCreator.getCreator().getPreEvent(guildId).shouldRecur()) {
                    try {
                        Integer amount = Integer.valueOf(args[1]);
                        EventCreator.getCreator().getPreEvent(guildId).getRecurrence().setCount(amount);
                        Message.sendMessage("Event count set to: `" + amount + "`" + Message.lineBreak + Message.lineBreak + "To set the interval (optionally) use `!event interval <amount`" + Message.lineBreak + Message.lineBreak + "The interval is how often to schedule following the frequency. (EX: if daily and interval is 2, it will be scheduled for every other day)", event);
                    } catch (NumberFormatException e) {
                        Message.sendMessage("Invalid value specified! Count must be a valid number (EX `1` or `24`)", event);
                    }
                } else {
                    Message.sendMessage("Event is not set to recur! Use `!event recur true` to make the event recur!", event);
                }
            } else {
                Message.sendMessage("Event Creator not initialized!", event);
            }
        } else {
            Message.sendMessage("Please specify how many times this event should recur with `!event count <amount>` Use `-1` for infinite!", event);
        }
    }

    private void moduleInterval(String[] args, MessageReceivedEvent event) {
        String guildId = event.getMessage().getGuild().getID();
        if (args.length == 2) {
            if (EventCreator.getCreator().hasPreEvent(guildId)) {
                if (EventCreator.getCreator().getPreEvent(guildId).shouldRecur()) {
                    try {
                        Integer amount = Integer.valueOf(args[1]);
                        EventCreator.getCreator().getPreEvent(guildId).getRecurrence().setInterval(amount);
                        Message.sendMessage("Event interval set to: `" + amount + "`" + Message.lineBreak + Message.lineBreak + "Please use `!event review` to review that all info entered is correct and confirm with `!event confirm`", event);
                    } catch (NumberFormatException e) {
                        Message.sendMessage("Invalid value specified! Interval must be a valid number (EX `1` or `24`)", event);
                    }
                } else {
                    Message.sendMessage("Event is not set to recur! Use `!event recur true` to make the event recur!", event);
                }
            } else {
                Message.sendMessage("Event Creator not initialized!", event);
            }
        } else {
            Message.sendMessage("Please specify the interval rule for the event with`!event interval <amount>` Defaulted to `1`" + Message.lineBreak + Message.lineBreak + "he interval is how often to schedule following the frequency. (EX: if daily and interval is 2, it will be scheduled for every other day)", event);
        }
    }
}