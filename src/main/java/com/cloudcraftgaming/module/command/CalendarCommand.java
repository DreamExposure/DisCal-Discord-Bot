package com.cloudcraftgaming.module.command;

import com.cloudcraftgaming.database.DatabaseManager;
import com.cloudcraftgaming.internal.calendar.calendar.CalendarCreator;
import com.cloudcraftgaming.internal.calendar.calendar.CalendarCreatorResponse;
import com.cloudcraftgaming.internal.calendar.calendar.CalendarMessageFormatter;
import com.cloudcraftgaming.utils.Message;
import com.cloudcraftgaming.utils.PermissionChecker;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;

/**
 * Created by Nova Fox on 1/4/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
@SuppressWarnings("FieldCanBeLocal")
public class CalendarCommand implements ICommand {
    private String TIME_ZONE_DB = "https://en.wikipedia.org/wiki/List_of_tz_database_time_zones#List";

    @Override
    public String getCommand() {
        return "calendar";
    }

    @Override
    public Boolean issueCommand(String[] args, MessageReceivedEvent event, IDiscordClient client) {
        if (PermissionChecker.hasSufficientRole(event)) {
            if (args.length < 1) {
                Message.sendMessage("You must specify a function to execute!", event, client);
            } else if (args.length == 1) {
                String function = args[0];
                String guildId = event.getMessage().getGuild().getID();

                if (function.equalsIgnoreCase("create")) {
                    if (CalendarCreator.getCreator().hasPreCalendar(guildId)) {
                        Message.sendMessage("Calendar creator already initiated!", event, client);
                    } else {
                        if (DatabaseManager.getManager().getData(guildId).getCalendarId().equalsIgnoreCase("primary")) {
                            Message.sendMessage("You must specify a name for the calendar!", event, client);
                        } else {
                            Message.sendMessage("A calendar has already been created!", event, client);
                        }
                    }
                } else if (function.equalsIgnoreCase("cancel")) {
                    if (CalendarCreator.getCreator().hasPreCalendar(guildId)) {
                        if (CalendarCreator.getCreator().terminate(event)) {
                            Message.sendMessage("Calendar creation cancelled! Calendar creator terminated!", event, client);
                        } else {
                            Message.sendMessage("Failed to cancel calendar creation!", event, client);
                        }
                    } else {
                        if (DatabaseManager.getManager().getData(guildId).getCalendarId().equalsIgnoreCase("primary")) {
                            Message.sendMessage("Calendar creator has not been initialized!", event, client);
                        } else {
                            Message.sendMessage("A calendar has already been created!", event, client);
                        }
                    }
                } else if (function.equalsIgnoreCase("view") || function.equalsIgnoreCase("review")) {
                    if (CalendarCreator.getCreator().hasPreCalendar(guildId)) {
                        Message.sendMessage(CalendarMessageFormatter.getFormatEventMessage(CalendarCreator.getCreator().getPreCalendar(guildId))
                                + Message.lineBreak + Message.lineBreak
                                + "Confirm calendar to complete setup OR edit the values!", event, client);
                    } else {
                        if (DatabaseManager.getManager().getData(guildId).getCalendarId().equalsIgnoreCase("primary")) {
                            Message.sendMessage("Calendar creator has not been initialized!", event, client);
                        } else {
                            Message.sendMessage("A calendar has already been created!", event, client);
                        }
                    }
                } else if (function.equalsIgnoreCase("confirm")) {
                    if (CalendarCreator.getCreator().hasPreCalendar(guildId)) {
                        CalendarCreatorResponse response = CalendarCreator.getCreator().confirmCalendar(event);
                        if (response.isSuccessful()) {
                            Message.sendMessage("Calendar Created! "
                                    + Message.lineBreak
                                    + "Use !linkCalendar to display!", event, client);
                        } else {
                            Message.sendMessage("Calendar creation failed!", event, client);
                        }
                    } else {
                        if (DatabaseManager.getManager().getData(guildId).getCalendarId().equalsIgnoreCase("primary")) {
                            Message.sendMessage("Calendar creator has not been initialized!", event, client);
                        } else {
                            Message.sendMessage("A calendar has already been created!", event, client);
                        }
                    }
                } else {
                    Message.sendMessage("Invalid function!", event, client);
                }
            } else if (args.length == 2) {
                String function = args[0];
                String value = args[1];
                String guildId = event.getMessage().getGuild().getID();
                if (function.equalsIgnoreCase("create")) {
                    if (!CalendarCreator.getCreator().hasPreCalendar(guildId)) {
                        if (DatabaseManager.getManager().getData(guildId).getCalendarId().equalsIgnoreCase("primary")) {
                            CalendarCreator.getCreator().init(event, value);
                            Message.sendMessage("Calendar creator initialized!"
                                    + Message.lineBreak
                                    + "Please specify a description!", event, client);
                        } else {
                            Message.sendMessage("A calendar has already been created!", event, client);
                        }
                    } else {
                        Message.sendMessage("Calendar creator has already been initialized!", event, client);
                    }
                } else if (function.equalsIgnoreCase("name") || function.equalsIgnoreCase("summery")) {
                    if (CalendarCreator.getCreator().hasPreCalendar(guildId)) {
                        CalendarCreator.getCreator().getPreCalendar(guildId).setSummery(value);
                        Message.sendMessage("Calendar summery/name set to '" + value + "'"
                                + Message.lineBreak + Message.lineBreak
                                + " Please specify the description!", event, client);
                    } else {
                        if (DatabaseManager.getManager().getData(guildId).getCalendarId().equalsIgnoreCase("primary")) {
                            Message.sendMessage("Calendar creator has not been initialized!", event, client);
                        } else {
                            Message.sendMessage("A calendar has already been created!", event, client);
                        }
                    }
                } else if (function.equalsIgnoreCase("description")) {
                    if (CalendarCreator.getCreator().hasPreCalendar(guildId)) {
                        CalendarCreator.getCreator().getPreCalendar(guildId).setDescription(value);
                        Message.sendMessage("Calendar description set to '" + value + "' "
                                + Message.lineBreak + Message.lineBreak
                                + "Please specify the timezone!"
                                + Message.lineBreak
                                + "For a list of valid timezones (within the 'TZ*' column): " + TIME_ZONE_DB, event, client);
                    } else {
                        if (DatabaseManager.getManager().getData(guildId).getCalendarId().equalsIgnoreCase("primary")) {
                            Message.sendMessage("Calendar creator has not been initialized!", event, client);
                        } else {
                            Message.sendMessage("A calendar has already been created!", event, client);
                        }
                    }
                } else if (function.equalsIgnoreCase("TimeZone")) {
                    if (CalendarCreator.getCreator().hasPreCalendar(guildId)) {
                        CalendarCreator.getCreator().getPreCalendar(guildId).setTimezone(value);
                        Message.sendMessage("Calendar TimeZone set to: " + value
                                + Message.lineBreak + Message.lineBreak
                                + "Calendar creation halted! "
                                + Message.lineBreak
                                + "View and/or confirm the calendar to make it official!", event, client);
                    } else {
                        if (DatabaseManager.getManager().getData(guildId).getCalendarId().equalsIgnoreCase("primary")) {
                            Message.sendMessage("Calendar creator has not been initialized!", event, client);
                        } else {
                            Message.sendMessage("A calendar has already been created!", event, client);
                        }
                    }
                } else {
                    Message.sendMessage("Invalid function!", event, client);
                }
            } else {
                String function = args[0];
                String guildId = event.getMessage().getGuild().getID();
                if (function.equalsIgnoreCase("create")) {
                    if (!CalendarCreator.getCreator().hasPreCalendar(guildId)) {
                        if (DatabaseManager.getManager().getData(guildId).getCalendarId().equalsIgnoreCase("primary")) {
                            CalendarCreator.getCreator().init(event, getContent(args));
                            Message.sendMessage("Calendar creator initialized!"
                                    + Message.lineBreak
                                    + "Please specify a description!", event, client);
                        } else {
                            Message.sendMessage("A calendar has already been created!", event, client);
                        }
                    } else {
                        Message.sendMessage("Calendar creator has already been initialized!", event, client);
                    }
                } else if (function.equalsIgnoreCase("name") || function.equalsIgnoreCase("summery")) {
                    if (CalendarCreator.getCreator().hasPreCalendar(guildId)) {
                        CalendarCreator.getCreator().getPreCalendar(guildId).setSummery(getContent(args));
                        Message.sendMessage("Calendar summer set to '" + getContent(args) + "'"
                                + Message.lineBreak + Message.lineBreak
                                + "Please specify the description!", event, client);
                    } else {
                        if (DatabaseManager.getManager().getData(guildId).getCalendarId().equalsIgnoreCase("primary")) {
                            Message.sendMessage("Calendar creator has not been initialized!", event, client);
                        } else {
                            Message.sendMessage("A calendar has already been created!", event, client);
                        }
                    }
                } else if (function.equalsIgnoreCase("description")) {
                    if (CalendarCreator.getCreator().hasPreCalendar(guildId)) {
                        CalendarCreator.getCreator().getPreCalendar(guildId).setDescription(getContent(args));
                        Message.sendMessage("Calendar description set to '" + getContent(args) + "' "
                                + Message.lineBreak + Message.lineBreak
                                + "Please specify the timezone!"
                                + Message.lineBreak
                                + "For a list of valid timezones (within the 'TZ*' column): " + TIME_ZONE_DB, event, client);
                    } else {
                        if (DatabaseManager.getManager().getData(guildId).getCalendarId().equalsIgnoreCase("primary")) {
                            Message.sendMessage("Calendar creator has not been initialized!", event, client);
                        } else {
                            Message.sendMessage("A calendar has already been created!", event, client);
                        }
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