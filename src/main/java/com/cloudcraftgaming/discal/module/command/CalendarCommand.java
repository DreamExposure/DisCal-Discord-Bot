package com.cloudcraftgaming.discal.module.command;

import com.cloudcraftgaming.discal.database.DatabaseManager;
import com.cloudcraftgaming.discal.internal.calendar.calendar.*;
import com.cloudcraftgaming.discal.internal.data.CalendarData;
import com.cloudcraftgaming.discal.internal.data.GuildSettings;
import com.cloudcraftgaming.discal.module.command.info.CommandInfo;
import com.cloudcraftgaming.discal.utils.GeneralUtils;
import com.cloudcraftgaming.discal.utils.Message;
import com.cloudcraftgaming.discal.utils.PermissionChecker;
import com.cloudcraftgaming.discal.utils.TimeZoneUtils;
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
    private String TIME_ZONE_DB = "http://www.joda.org/joda-time/timezones.html";

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
        info.getSubCommands().add("description");
        info.getSubCommands().add("timezone");
        info.getSubCommands().add("edit");

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
            } else if (args.length >= 1) {
                String guildId = event.getMessage().getGuild().getID();
                GuildSettings settings = DatabaseManager.getManager().getSettings(guildId);
                //TODO: Add support for multiple calendars...
                CalendarData calendarData = DatabaseManager.getManager().getMainCalendar(guildId);

                switch (args[0].toLowerCase()) {
                    case "create":
                        moduleCreate(args, event, client, calendarData);
                        break;
                    case "cancel":
                        moduleCancel(event, client, calendarData);
                        break;
                    case "view":
                        moduleView(event, client, calendarData);
                        break;
                    case "review":
                        moduleView(event, client, calendarData);
                        break;
                    case "confirm":
                        moduleConfirm(event, client, calendarData);
                        break;
                    case "delete":
                        moduleDelete(event, client, calendarData);
                        break;
                    case "remove":
                        moduleDelete(event, client, calendarData);
                        break;
                    case "name":
                        moduleSummary(args, event, client, calendarData);
                        break;
                    case "summary":
                        moduleSummary(args, event, client, calendarData);
                        break;
                    case "description":
                        moduleDescription(args, event, client, calendarData);
                        break;
                    case "timezone":
                        moduleTimezone(args, event, client, calendarData);
                        break;
                    case "edit":
                        if (settings.isDevGuild()) {
                            moduleEdit(event, client, calendarData);
                        } else {
                            Message.sendMessage("This option is disabled for testing only!", event, client);
                        }
                        break;
                    default:
                        Message.sendMessage("Invalid function! Please view `!help calendar` for a full list of valid functions!", event, client);
                        break;
                }
            }
        } else {
            Message.sendMessage("You do not have sufficient permissions to use this DisCal command!", event, client);
        }
        return false;
    }

    private void moduleCreate(String[] args, MessageReceivedEvent event, IDiscordClient client, CalendarData calendarData) {
        String guildId = event.getMessage().getGuild().getID();
        if (CalendarCreator.getCreator().hasPreCalendar(guildId)) {
            Message.sendMessage("Calendar creator already initiated!", event, client);
        } else {
            if (calendarData.getCalendarId().equalsIgnoreCase("primary")) {
                if (args.length > 1) {
                    String name = GeneralUtils.getContent(args, 1);
                    CalendarCreator.getCreator().init(event, name);
                    Message.sendMessage("Calendar Creator initialized! Please specify the description with `!calendar description <desc, spaces allowed>`", event, client);
                } else {
                    Message.sendMessage("Please specify a name for the calendar!", event, client);
                }
            } else {
                Message.sendMessage("A calendar has already been created!", event, client);
            }
        }
    }

    private void moduleCancel(MessageReceivedEvent event, IDiscordClient client, CalendarData calendarData) {
        String guildId = event.getMessage().getGuild().getID();
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
    }

    private void moduleView(MessageReceivedEvent event, IDiscordClient client, CalendarData calendarData) {
        String guildId = event.getMessage().getGuild().getID();
        if (CalendarCreator.getCreator().hasPreCalendar(guildId)) {
            Message.sendMessage(CalendarMessageFormatter.getPreCalendarEmbed(CalendarCreator.getCreator().getPreCalendar(guildId)), "Confirm calendar to complete setup `!calendar confirm` OR edit the values!", event, client);
        } else {
            if (calendarData.getCalendarId().equalsIgnoreCase("primary")) {
                Message.sendMessage("Calendar creator has not been initialized! Use `!linkCalendar` to view your existing calendar!", event, client);
            } else {
                Message.sendMessage("A calendar has already been created!", event, client);
            }
        }
    }

    private void moduleConfirm(MessageReceivedEvent event, IDiscordClient client, CalendarData calendarData) {
        String guildId = event.getMessage().getGuild().getID();
        if (CalendarCreator.getCreator().hasPreCalendar(guildId)) {
            CalendarCreatorResponse response = CalendarCreator.getCreator().confirmCalendar(event);
            if (response.isSuccessful()) {
                Message.sendMessage("Calendar Created! "
                        + Message.lineBreak
                        + "Use `!linkCalendar` to display!", event, client);
            } else {
                Message.sendMessage("Calendar creation failed!" + Message.lineBreak + Message.lineBreak + "The most likely cause is that your TimeZone is incorrect! Please specify the proper TimeZone and try again!", event, client);
            }
        } else {
            if (calendarData.getCalendarId().equalsIgnoreCase("primary")) {
                Message.sendMessage("Calendar creator has not been initialized!", event, client);
            } else {
                Message.sendMessage("A calendar has already been created!", event, client);
            }
        }
    }

    private void moduleDelete(MessageReceivedEvent event, IDiscordClient client, CalendarData calendarData) {
        if(!event.getMessage().getAuthor().getPermissionsForGuild(event.getMessage().getGuild()).contains(
                Permissions.MANAGE_SERVER)) {
            Message.sendMessage("You need the \"Manage Server\" permission to run this command!", event, client);
            return;
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
    }

    private void moduleSummary(String[] args, MessageReceivedEvent event, IDiscordClient client, CalendarData calendarData) {
        String guildId = event.getMessage().getGuild().getID();
        if (args.length > 1) {
            if (CalendarCreator.getCreator().hasPreCalendar(guildId)) {
                CalendarCreator.getCreator().getPreCalendar(guildId).setSummary(GeneralUtils.getContent(args, 1));
                Message.sendMessage("Calendar summary set to '" + GeneralUtils.getContent(args, 1) + "'"
                        + Message.lineBreak + Message.lineBreak
                        + "Please specify the description!", event, client);
            } else {
                if (calendarData.getCalendarId().equalsIgnoreCase("primary")) {
                    Message.sendMessage("Calendar creator has not been initialized!", event, client);
                } else {
                    Message.sendMessage("A calendar has already been created!", event, client);
                }
            }
        } else {
            Message.sendMessage("Please specify the name/summary of the calendar with `!calendar summary <summary, spaces allowed>`", event, client);
        }
    }

    private void moduleDescription(String[] args, MessageReceivedEvent event, IDiscordClient client, CalendarData calendarData) {
        String guildId = event.getMessage().getGuild().getID();
        if (args.length > 1) {
            if (CalendarCreator.getCreator().hasPreCalendar(guildId)) {
                CalendarCreator.getCreator().getPreCalendar(guildId).setDescription(GeneralUtils.getContent(args, 1));
                Message.sendMessage("Calendar description set to '" + GeneralUtils.getContent(args, 1) + "' "
                        + Message.lineBreak + Message.lineBreak
                        + "Please specify the timezone!"
                        + Message.lineBreak
                        + "Timezones are case sensitive. (Ex. America/New_York and not america/new_york)"
                        + Message.lineBreak
                        + Message.lineBreak
                        + "For a list of valid timezones: " + TIME_ZONE_DB, event, client);
            } else {
                if (calendarData.getCalendarId().equalsIgnoreCase("primary")) {
                    Message.sendMessage("Calendar creator has not been initialized!", event, client);
                } else {
                    Message.sendMessage("A calendar has already been created!", event, client);
                }
            }
        } else {
            Message.sendMessage("Please specify the calendar description with `!calendar description <desc, spaces allowed>`", event, client);
        }
    }

    private void moduleTimezone(String[] args, MessageReceivedEvent event, IDiscordClient client, CalendarData calendarData) {
        String guildId = event.getMessage().getGuild().getID();
        if (args.length == 2) {
            String value = args[1];
            if (CalendarCreator.getCreator().hasPreCalendar(guildId)) {
                if (TimeZoneUtils.isValid(value)) {
                    CalendarCreator.getCreator().getPreCalendar(guildId).setTimezone(value);
                    Message.sendMessage("Calendar TimeZone set to: `" + value + "`"
                            + Message.lineBreak + Message.lineBreak
                            + "Calendar creation halted! "
                            + Message.lineBreak
                            + "View and/or confirm the calendar to make it official!", event, client);
                } else {
                    Message.sendMessage("Invalid timezone specified! Please make sure the timezone is on this list: <" + TIME_ZONE_DB + ">" + Message.lineBreak + Message.lineBreak + "It is very important that you input the timezone correctly because it is case sensitive! (EX: Not `america/new_york` but rather `America/New_York`)", event, client);
                }
            } else {
                if (calendarData.getCalendarId().equalsIgnoreCase("primary")) {
                    Message.sendMessage("Calendar creator has not been initialized!", event, client);
                } else {
                    Message.sendMessage("A calendar has already been created!", event, client);
                }
            }
        } else {
            Message.sendMessage("Please specify the timezone!"
                    + Message.lineBreak
                    + "Timezones are case sensitive. (Ex. America/New_York and not america/new_york)"
                    + Message.lineBreak
                    + Message.lineBreak
                    + "For a list of valid timezones: " + TIME_ZONE_DB, event, client);
        }
    }

    private void moduleEdit(MessageReceivedEvent event, IDiscordClient client, CalendarData calendarData) {
        String guildId = event.getMessage().getGuild().getID();
        if (!CalendarCreator.getCreator().hasPreCalendar(guildId)) {
            if (!calendarData.getCalendarAddress().equalsIgnoreCase("primary")) {
                PreCalendar preCalendar = CalendarCreator.getCreator().edit(event);
                if (preCalendar != null) {
                    Message.sendMessage(CalendarMessageFormatter.getPreCalendarEmbed(preCalendar), "Calendar Editor initiated! Edit the values and then confirm your edits with `!calendar confirm`", event, client);
                } else {
                    Message.sendMessage("An error has occurred! The developer has been emailed!", event, client);
                }
            } else {
                Message.sendMessage("You cannot edit your calendar when you do not have one!", event, client);
            }
        } else {
            Message.sendMessage("Calendar Creator has already been initiated!", event, client);
        }
    }
}