package com.cloudcraftgaming.discal.module.command;

import com.cloudcraftgaming.discal.database.DatabaseManager;
import com.cloudcraftgaming.discal.internal.calendar.calendar.CalendarCreator;
import com.cloudcraftgaming.discal.internal.calendar.calendar.CalendarCreatorResponse;
import com.cloudcraftgaming.discal.internal.calendar.calendar.CalendarMessageFormatter;
import com.cloudcraftgaming.discal.internal.calendar.calendar.CalendarUtils;
import com.cloudcraftgaming.discal.internal.data.CalendarData;
import com.cloudcraftgaming.discal.module.command.info.CommandInfo;
import com.cloudcraftgaming.discal.utils.Message;
import com.cloudcraftgaming.discal.utils.PermissionChecker;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.Permissions;

import java.util.ArrayList;

/**
 * Created by Nova Fox on 1/4/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
@SuppressWarnings("FieldCanBeLocal")
public class CalendarCommand implements ICommand {
    private String TIME_ZONE_DB = "https://en.wikipedia.org/wiki/List_of_tz_database_time_zones#List";

    /**
     * Gets the command this Object is responsible for.
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
        return aliases;
    }

    /**
     * Gets the info on the command (not sub command) to be used in help menus.
     *
     * @return The command info.
     */
    @Override
    public CommandInfo getCommandInfo() {
        CommandInfo info = new CommandInfo("calendar");
        info.setDescription("Used for direct interaction with your DisCal Calendar.");
        info.setExample("!calendar <subCommand> (value)");

        info.getSubCommands().add("create");
        info.getSubCommands().add("cancel");
        info.getSubCommands().add("view");
        info.getSubCommands().add("review");
        info.getSubCommands().add("confirm");
        info.getSubCommands().add("delete");
        info.getSubCommands().add("remove");
        info.getSubCommands().add("name");
        info.getSubCommands().add("summary");
        info.getSubCommands().add("timezone");
        info.getSubCommands().add("channel");

        return info;
    }

    /**
     * Issues the command this Object is responsible for.
     * @param args The command arguments.
     * @param event The event received.
     * @param client The Client associated with the Bot.
     * @return <code>true</code> if successful, else <code>false</code>.
     */
    @Override
    public Boolean issueCommand(String[] args, MessageReceivedEvent event, IDiscordClient client) {
        if (PermissionChecker.hasSufficientRole(event)) {
            if (args.length < 1) {
                Message.sendMessage("You must specify a function to execute!", event, client);
            } else if (args.length == 1) {
                String function = args[0];
                String guildId = event.getMessage().getGuild().getID();
                //TODO: Add support for multiple calendars...
                CalendarData calendarData = DatabaseManager.getManager().getMainCalendar(guildId);

                if (function.equalsIgnoreCase("create")) {
                    if (CalendarCreator.getCreator().hasPreCalendar(guildId)) {
                        Message.sendMessage("Calendar creator already initiated!", event, client);
                    } else {
                        if (calendarData.getCalendarId().equalsIgnoreCase("primary")) {
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
                        if (calendarData.getCalendarId().equalsIgnoreCase("primary")) {
                            Message.sendMessage("Calendar creator has not been initialized!", event, client);
                        } else {
                            Message.sendMessage("A calendar has already been created!", event, client);
                        }
                    }
                } else if (function.equalsIgnoreCase("view") || function.equalsIgnoreCase("review")) {
                    if (CalendarCreator.getCreator().hasPreCalendar(guildId)) {
                        Message.sendMessage(CalendarMessageFormatter.getPreCalendarEmbed(CalendarCreator.getCreator().getPreCalendar(guildId)), "Confirm calendar to complete setup `!calendar confirm` OR edit the values!", event, client);
                    } else {
                        if (calendarData.getCalendarId().equalsIgnoreCase("primary")) {
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
                                    + "Use `!linkCalendar` to display!", event, client);
                        } else {
                            Message.sendMessage("Calendar creation failed!", event, client);
                        }
                    } else {
                        if (calendarData.getCalendarId().equalsIgnoreCase("primary")) {
                            Message.sendMessage("Calendar creator has not been initialized!", event, client);
                        } else {
                            Message.sendMessage("A calendar has already been created!", event, client);
                        }
                    }
                } else if (function.equalsIgnoreCase("delete") || function.equalsIgnoreCase("remove")) {
                    if(!event.getMessage().getAuthor().getPermissionsForGuild(event.getMessage().getGuild()).contains(
		                        Permissions.MANAGE_SERVER)) {
                        	Message.sendMessage("You need the \"Manage Server\" permission to run this command!", event, client);
                        	return false;
                    }
                    if (!calendarData.getCalendarId().equalsIgnoreCase("primary")) {
                        //Delete calendar
                        if (CalendarUtils.deleteCalendar(calendarData)) {
                            Message.sendMessage("Calendar deleted! You may create a new one if you so wish!", event, client);
                        } else {
                            Message.sendMessage("Oops! Something went wrong! I failed to delete your calendar!", event, client);
                        }
                    } else {
                        //No calendar to delete
                        Message.sendMessage("Cannot delete calendar as one does not exist!", event, client);
                    }
                } else {
                    Message.sendMessage("Invalid function!", event, client);
                }
            } else if (args.length == 2) {
                String function = args[0];
                String value = args[1];
                String guildId = event.getMessage().getGuild().getID();
                //TODO: Add support for multiple calendars...
                CalendarData calendarData = DatabaseManager.getManager().getMainCalendar(guildId);

                if (function.equalsIgnoreCase("create")) {
                    if (!CalendarCreator.getCreator().hasPreCalendar(guildId)) {
                        if (calendarData.getCalendarId().equalsIgnoreCase("primary")) {
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
                } else if (function.equalsIgnoreCase("name") || function.equalsIgnoreCase("summary")) {
                    if (CalendarCreator.getCreator().hasPreCalendar(guildId)) {
                        CalendarCreator.getCreator().getPreCalendar(guildId).setSummary(value);
                        Message.sendMessage("Calendar summary/name set to `" + value + "`"
                                + Message.lineBreak + Message.lineBreak
                                + " Please specify the description!", event, client);
                    } else {
                        if (calendarData.getCalendarId().equalsIgnoreCase("primary")) {
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
                        if (calendarData.getCalendarId().equalsIgnoreCase("primary")) {
                            Message.sendMessage("Calendar creator has not been initialized!", event, client);
                        } else {
                            Message.sendMessage("A calendar has already been created!", event, client);
                        }
                    }
                } else if (function.equalsIgnoreCase("TimeZone")) {
                    if (CalendarCreator.getCreator().hasPreCalendar(guildId)) {
                        CalendarCreator.getCreator().getPreCalendar(guildId).setTimezone(value);
                        Message.sendMessage("Calendar TimeZone set to: `" + value + "`"
                                + Message.lineBreak + Message.lineBreak
                                + "Calendar creation halted! "
                                + Message.lineBreak
                                + "View and/or confirm the calendar to make it official!", event, client);
                    } else {
                        if (calendarData.getCalendarId().equalsIgnoreCase("primary")) {
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
                //TODO: Add support for multiple calendars...
                CalendarData calendarData = DatabaseManager.getManager().getMainCalendar(guildId);

                if (function.equalsIgnoreCase("create")) {
                    if (!CalendarCreator.getCreator().hasPreCalendar(guildId)) {
                        if (calendarData.getCalendarId().equalsIgnoreCase("primary")) {
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
                } else if (function.equalsIgnoreCase("name") || function.equalsIgnoreCase("summary")) {
                    if (CalendarCreator.getCreator().hasPreCalendar(guildId)) {
                        CalendarCreator.getCreator().getPreCalendar(guildId).setSummary(getContent(args));
                        Message.sendMessage("Calendar summary set to '" + getContent(args) + "'"
                                + Message.lineBreak + Message.lineBreak
                                + "Please specify the description!", event, client);
                    } else {
                        if (calendarData.getCalendarId().equalsIgnoreCase("primary")) {
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
                        if (calendarData.getCalendarId().equalsIgnoreCase("primary")) {
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

    /**
     * Gets the contents of the message at an offset.
     * @param args The args of the command.
     * @return The contents of the message at an offset.
     */
    private String getContent(String[] args) {
        StringBuilder content = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            content.append(args[i]).append(" ");
        }
        return content.toString().trim();
    }
}
