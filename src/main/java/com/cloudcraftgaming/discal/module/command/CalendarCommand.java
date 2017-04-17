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
import sx.blah.discord.handle.obj.IMessage;
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
        	if (CalendarCreator.getCreator().getPreCalendar(guildId).getCreatorMessage() != null) {
				Message.editMessage(CalendarCreator.getCreator().getPreCalendar(guildId).getCreatorMessage(), "Calendar Creator already initialized!");
				Message.deleteMessage(event);
			} else {
        		Message.sendMessage("Calendar Creator already initialized!", event, client);
			}
        } else {
            if (calendarData.getCalendarId().equalsIgnoreCase("primary")) {
                if (args.length > 1) {
                    String name = GeneralUtils.getContent(args, 1);
                    PreCalendar calendar = CalendarCreator.getCreator().init(event, name, true);
                    if (calendar.getCreatorMessage() != null) {
						Message.deleteMessage(event);
					} else {
                    	Message.sendMessage("Calendar Creator initiated! Please specify the description with `!calendar description <desc, spaces allowed`", event, client);
					}
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
        	IMessage message = CalendarCreator.getCreator().getPreCalendar(guildId).getCreatorMessage();
            if (CalendarCreator.getCreator().terminate(event)) {
            	if (message != null) {
					Message.editMessage(message, "Calendar creation cancelled! Calendar creator terminated!");
					Message.deleteMessage(event);
				} else {
            		Message.sendMessage("Calendar creation cancelled! Calendar creator terminated!", event, client);
				}
            } else {
                Message.sendMessage("Failed to cancel calendar creation!", event, client);
                Message.deleteMessage(event);
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
            PreCalendar preCalendar = CalendarCreator.getCreator().getPreCalendar(guildId);
            if (preCalendar.getCreatorMessage() != null) {
				Message.editMessage(preCalendar.getCreatorMessage(), "Confirm calendar to complete setup `!calendar confirm` OR edit the values!", CalendarMessageFormatter.getPreCalendarEmbed(preCalendar));
				Message.deleteMessage(event);
			} else {
            	Message.sendMessage(CalendarMessageFormatter.getPreCalendarEmbed(preCalendar), "Confirm calendar to complete setup `!calendar confirm` OR edit the values!", event, client);
			}
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
                if (response.isEdited()) {
                	if (response.getCreatorMessage() != null) {
						Message.editMessage(response.getCreatorMessage(), "Calendar successfully edited!", CalendarMessageFormatter.getCalendarLinkEmbed(response.getCalendar()));
						Message.deleteMessage(event);
					} else {
                		Message.sendMessage(CalendarMessageFormatter.getCalendarLinkEmbed(response.getCalendar()), "Calendar successfully edited!", event, client);
					}
                } else {
                	if (response.getCalendar() != null) {
						Message.editMessage(response.getCreatorMessage(), "Calendar successfully created!", CalendarMessageFormatter.getCalendarLinkEmbed(response.getCalendar()));
						Message.deleteMessage(event);
					} else {
                		Message.sendMessage(CalendarMessageFormatter.getCalendarLinkEmbed(response.getCalendar()), "Calendar successfully created!", event, client);
					}
                }
            } else {
                if (response.isEdited()) {
                	if (response.getCreatorMessage() != null) {
						Message.editMessage(response.getCreatorMessage(), "Calendar Edit failed! Are you sure everything is correct?");
						Message.deleteMessage(event);
					} else {
                		Message.sendMessage("Calendar Edit failed! Are you sure everything is correct?", event, client);
					}
                } else {
                	if (response.getCreatorMessage() != null) {
						Message.editMessage(response.getCreatorMessage(), "Calendar Creation failed! Are you sure everything is correct?");
						Message.deleteMessage(event);
					} else {
                		Message.sendMessage("Calendar Creation failed! Are you sure everything is correct?", event, client);
					}
                }
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
        String guildId = event.getMessage().getGuild().getID();
        if (CalendarCreator.getCreator().hasPreCalendar(guildId)) {
        	if (CalendarCreator.getCreator().getPreCalendar(guildId).getCreatorMessage() != null) {
				Message.editMessage(CalendarCreator.getCreator().getPreCalendar(guildId).getCreatorMessage(), "Cannot delete calendar while in Calendar Creator!");
				Message.deleteMessage(event);
			} else {
        		Message.sendMessage("Cannot delete calendar while in the Calendar Creator/Editor!", event, client);
			}
            return;
        }
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
            	if (CalendarCreator.getCreator().getPreCalendar(guildId).getCreatorMessage() != null) {
					CalendarCreator.getCreator().getPreCalendar(guildId).setSummary(GeneralUtils.getContent(args, 1));
					Message.editMessage(CalendarCreator.getCreator().getPreCalendar(guildId).getCreatorMessage(), "Summary set!", CalendarMessageFormatter.getPreCalendarEmbed(CalendarCreator.getCreator().getPreCalendar(guildId)));
					Message.deleteMessage(event);
				} else {
            		Message.sendMessage("Summary set to: `" + GeneralUtils.getContent(args, 1) + "`", event, client);
				}
            } else {
                if (calendarData.getCalendarId().equalsIgnoreCase("primary")) {
                    Message.sendMessage("Calendar creator has not been initialized!", event, client);
                } else {
                    Message.sendMessage("A calendar has already been created!", event, client);
                }
            }
        } else {
            if (CalendarCreator.getCreator().hasPreCalendar(guildId)) {
            	if (CalendarCreator.getCreator().getPreCalendar(guildId).getCreatorMessage() != null) {
					Message.editMessage(CalendarCreator.getCreator().getPreCalendar(guildId).getCreatorMessage(), "Please specify the name/summary of the calendar with `!calendar summary <summary, spaces allowed>`");
					Message.deleteMessage(event);
				} else {
            		Message.sendMessage("Please specify the name/summary of the calendar with `!calendar summary <summary, spaces allowed>`", event, client);
				}
            } else {
                Message.sendMessage("Please specify the name/summary of the calendar with `!calendar summary <summary, spaces allowed>`", event, client);
            }
        }
    }

    private void moduleDescription(String[] args, MessageReceivedEvent event, IDiscordClient client, CalendarData calendarData) {
        String guildId = event.getMessage().getGuild().getID();
        if (args.length > 1) {
            if (CalendarCreator.getCreator().hasPreCalendar(guildId)) {
                CalendarCreator.getCreator().getPreCalendar(guildId).setDescription(GeneralUtils.getContent(args, 1));
                if (CalendarCreator.getCreator().getPreCalendar(guildId).getCreatorMessage() != null) {
					Message.editMessage(CalendarCreator.getCreator().getPreCalendar(guildId).getCreatorMessage(), "Calendar description set!"
							+ "Please specify the timezone!"
							+ Message.lineBreak
							+ "For a list of valid timezones: " + TIME_ZONE_DB, CalendarMessageFormatter.getPreCalendarEmbed(CalendarCreator.getCreator().getPreCalendar(guildId)));
					Message.deleteMessage(event);
				} else {
					Message.sendMessage("Calendar description set to `" + GeneralUtils.getContent(args, 1) + "`" + Message.lineBreak
							+ "Please specify the timezone!"
							+ Message.lineBreak
							+ "For a list of valid timezones: " + TIME_ZONE_DB, event, client);
				}
            } else {
                if (calendarData.getCalendarId().equalsIgnoreCase("primary")) {
                    Message.sendMessage("Calendar creator has not been initialized!", event, client);
                } else {
                    Message.sendMessage("A calendar has already been created!", event, client);
                }
            }
        } else {
            if (CalendarCreator.getCreator().hasPreCalendar(guildId)) {
            	if (CalendarCreator.getCreator().getPreCalendar(guildId).getCreatorMessage() != null) {
					Message.editMessage(CalendarCreator.getCreator().getPreCalendar(guildId).getCreatorMessage(), "Please specify the calendar description with `!calendar description <desc, spaces allowed>`");
					Message.deleteMessage(event);
				} else {
            		Message.sendMessage("Please specify the calendar description with `!calendar description <desc, spaces allowed>`", event, client);
				}
            } else {
                Message.sendMessage("Please specify the calendar description with `!calendar description <desc, spaces allowed>`", event, client);
            }
        }
    }

    private void moduleTimezone(String[] args, MessageReceivedEvent event, IDiscordClient client, CalendarData calendarData) {
        String guildId = event.getMessage().getGuild().getID();
        if (args.length == 2) {
            String value = args[1];
            if (CalendarCreator.getCreator().hasPreCalendar(guildId)) {
                if (TimeZoneUtils.isValid(value)) {
                    CalendarCreator.getCreator().getPreCalendar(guildId).setTimezone(value);

                    if (CalendarCreator.getCreator().getPreCalendar(guildId).getCreatorMessage() != null) {
						Message.editMessage(CalendarCreator.getCreator().getPreCalendar(guildId).getCreatorMessage(), "TimeZone set!", CalendarMessageFormatter.getPreCalendarEmbed(CalendarCreator.getCreator().getPreCalendar(guildId)));
						Message.deleteMessage(event);
					} else {
                    	Message.sendMessage("TimeZone set to `" + value + "`" + Message.lineBreak + "Please review your calendar with `!calendar review` to verify that the values are correct and confirm with `!calendar confirm`", event, client);
					}
                } else {
                	if (CalendarCreator.getCreator().getPreCalendar(guildId).getCreatorMessage() != null) {
						Message.editMessage(CalendarCreator.getCreator().getPreCalendar(guildId).getCreatorMessage(), "Invalid timezone specified! Please make sure the timezone is on this list: <" + TIME_ZONE_DB + ">" + Message.lineBreak + Message.lineBreak + "It is very important that you input the timezone correctly because it is case sensitive! (EX: Not `america/new_york` but rather `America/New_York`)", CalendarMessageFormatter.getPreCalendarEmbed(CalendarCreator.getCreator().getPreCalendar(guildId)));
						Message.deleteMessage(event);
					} else {
                		Message.sendMessage("Invalid timezone specified! Please make sure the timezone is on this list: <" + TIME_ZONE_DB + ">" + Message.lineBreak + Message.lineBreak + "It is very important that you input the timezone correctly because it is case sensitive! (EX: Not `america/new_york` but rather `America/New_York`)", event, client);
					}
                }
            } else {
                if (calendarData.getCalendarId().equalsIgnoreCase("primary")) {
                    Message.sendMessage("Calendar creator has not been initialized!", event, client);
                } else {
                    Message.sendMessage("A calendar has already been created!", event, client);
                }
            }
        } else {
            if (CalendarCreator.getCreator().hasPreCalendar(guildId)) {
                Message.editMessage(CalendarCreator.getCreator().getPreCalendar(guildId).getCreatorMessage(), "Please specify the timezone!"
                        + Message.lineBreak
                        + "Timezones are case sensitive. (Ex. America/New_York and not america/new_york)"
                        + Message.lineBreak
                        + Message.lineBreak
                        + "For a list of valid timezones: " + TIME_ZONE_DB);
                Message.deleteMessage(event);
            } else {
                Message.sendMessage("Please specify the timezone!"
                        + Message.lineBreak
                        + "Timezones are case sensitive. (Ex. America/New_York and not america/new_york)"
                        + Message.lineBreak
                        + Message.lineBreak
                        + "For a list of valid timezones: " + TIME_ZONE_DB, event, client);
            }
        }
    }

    private void moduleEdit(MessageReceivedEvent event, IDiscordClient client, CalendarData calendarData) {
        String guildId = event.getMessage().getGuild().getID();
        if (!CalendarCreator.getCreator().hasPreCalendar(guildId)) {
            if (!calendarData.getCalendarAddress().equalsIgnoreCase("primary")) {
                PreCalendar calendar = CalendarCreator.getCreator().edit(event, true);
                if (calendar.getCreatorMessage() != null) {
					Message.deleteMessage(event);
				} else {
                	Message.sendMessage(CalendarMessageFormatter.getPreCalendarEmbed(calendar), "Calendar Editor initiated! Edit the values and confirm with `!calendar confirm", event, client);
				}
            } else {
                Message.sendMessage("You cannot edit your calendar when you do not have one!", event, client);
            }
        } else {
        	if (CalendarCreator.getCreator().getPreCalendar(guildId).getCreatorMessage() != null) {
				Message.editMessage(CalendarCreator.getCreator().getPreCalendar(guildId).getCreatorMessage(), "Calendar Creator has already been initiated!");
				Message.deleteMessage(event);
			} else {
        		Message.sendMessage("Calendar Creator has already been initiated!", event, client);
			}
        }
    }
}