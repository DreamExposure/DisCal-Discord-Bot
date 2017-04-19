package com.cloudcraftgaming.discal.module.command;

import com.cloudcraftgaming.discal.database.DatabaseManager;
import com.cloudcraftgaming.discal.internal.calendar.calendar.*;
import com.cloudcraftgaming.discal.internal.data.CalendarData;
import com.cloudcraftgaming.discal.internal.data.GuildSettings;
import com.cloudcraftgaming.discal.module.command.info.CommandInfo;
import com.cloudcraftgaming.discal.utils.*;
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
     * @return <code>true</code> if successful, else <code>false</code>.
     */
    @Override
    public Boolean issueCommand(String[] args, MessageReceivedEvent event) {
        if (PermissionChecker.hasSufficientRole(event)) {
            if (args.length < 1) {
                Message.sendMessage(MessageManager.getMessage("Notification.Args.Few", event), event);
            } else if (args.length >= 1) {
                String guildId = event.getMessage().getGuild().getID();
                GuildSettings settings = DatabaseManager.getManager().getSettings(guildId);
                //TODO: Add support for multiple calendars...
                CalendarData calendarData = DatabaseManager.getManager().getMainCalendar(guildId);

                switch (args[0].toLowerCase()) {
                    case "create":
                        moduleCreate(args, event, calendarData);
                        break;
                    case "cancel":
                        moduleCancel(event, calendarData);
                        break;
                    case "view":
                        moduleView(event, calendarData);
                        break;
                    case "review":
                        moduleView(event, calendarData);
                        break;
                    case "confirm":
                        moduleConfirm(event, calendarData);
                        break;
                    case "delete":
                        moduleDelete(event, calendarData);
                        break;
                    case "remove":
                        moduleDelete(event, calendarData);
                        break;
                    case "name":
                        moduleSummary(args, event, calendarData);
                        break;
                    case "summary":
                        moduleSummary(args, event, calendarData);
                        break;
                    case "description":
                        moduleDescription(args, event, calendarData);
                        break;
                    case "timezone":
                        moduleTimezone(args, event, calendarData);
                        break;
                    case "edit":
                        if (settings.isDevGuild()) {
                            moduleEdit(event, calendarData);
                        } else {
                            Message.sendMessage("Notification.Disabled", event);
                        }
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

    private void moduleCreate(String[] args, MessageReceivedEvent event, CalendarData calendarData) {
        String guildId = event.getMessage().getGuild().getID();
        if (CalendarCreator.getCreator().hasPreCalendar(guildId)) {
        	if (CalendarCreator.getCreator().getPreCalendar(guildId).getCreatorMessage() != null) {
				Message.editMessage(CalendarCreator.getCreator().getPreCalendar(guildId).getCreatorMessage(), MessageManager.getMessage("Creator.Calendar.AlreadyInit", event));
				Message.deleteMessage(event);
			} else {
        		Message.sendMessage(MessageManager.getMessage("Creator.Calendar.AlreadyInit", event), event);
			}
        } else {
            if (calendarData.getCalendarId().equalsIgnoreCase("primary")) {
                if (args.length > 1) {
                    String name = GeneralUtils.getContent(args, 1);
                    PreCalendar calendar = CalendarCreator.getCreator().init(event, name, true);
                    if (calendar.getCreatorMessage() != null) {
						Message.deleteMessage(event);
					} else {
                    	Message.sendMessage(MessageManager.getMessage("Creator.Calendar.Create.Init", event), event);
					}
                } else {
                    Message.sendMessage(MessageManager.getMessage("Creator.Calendar.Create.Name", event), event);
                }
            } else {
                Message.sendMessage(MessageManager.getMessage("Creator.Calendar.HasCalendar", event), event);
            }
        }
    }

    private void moduleCancel(MessageReceivedEvent event, CalendarData calendarData) {
        String guildId = event.getMessage().getGuild().getID();
        if (CalendarCreator.getCreator().hasPreCalendar(guildId)) {
        	IMessage message = CalendarCreator.getCreator().getPreCalendar(guildId).getCreatorMessage();
            if (CalendarCreator.getCreator().terminate(event)) {
            	if (message != null) {
					Message.editMessage(message, MessageManager.getMessage("Creator.Calendar.Cancel.Success", event));
					Message.deleteMessage(event);
				} else {
            		Message.sendMessage(MessageManager.getMessage("Creator.Calendar.Cancel.Success", event), event);
				}
            } else {
                Message.sendMessage(MessageManager.getMessage("Creator.Calendar.Cancel.Failure", event), event);
                Message.deleteMessage(event);
            }
        } else {
            if (calendarData.getCalendarId().equalsIgnoreCase("primary")) {
                Message.sendMessage(MessageManager.getMessage("Creator.Calendar.AlreadyInit", event), event);
            } else {
                Message.sendMessage(MessageManager.getMessage("Creator.Calendar.HasCalendar", event), event);
            }
        }
    }

    private void moduleView(MessageReceivedEvent event, CalendarData calendarData) {
        String guildId = event.getMessage().getGuild().getID();
        if (CalendarCreator.getCreator().hasPreCalendar(guildId)) {
            PreCalendar preCalendar = CalendarCreator.getCreator().getPreCalendar(guildId);
            if (preCalendar.getCreatorMessage() != null) {
				Message.editMessage(preCalendar.getCreatorMessage(), MessageManager.getMessage("Creator.Calendar.Review", event), CalendarMessageFormatter.getPreCalendarEmbed(preCalendar));
				Message.deleteMessage(event);
			} else {
            	Message.sendMessage(CalendarMessageFormatter.getPreCalendarEmbed(preCalendar), MessageManager.getMessage("Creator.Calendar.Review", event), event);
			}
        } else {
            if (calendarData.getCalendarId().equalsIgnoreCase("primary")) {
                Message.sendMessage(MessageManager.getMessage("Creator.Calendar.NoCalendar", event), event);
            } else {
                Message.sendMessage(MessageManager.getMessage("Creator.Calendar.HasCalendar", event), event);
            }
        }
    }

    private void moduleConfirm(MessageReceivedEvent event, CalendarData calendarData) {
        String guildId = event.getMessage().getGuild().getID();
        if (CalendarCreator.getCreator().hasPreCalendar(guildId)) {
            CalendarCreatorResponse response = CalendarCreator.getCreator().confirmCalendar(event);
            if (response.isSuccessful()) {
                if (response.isEdited()) {
                	if (response.getCreatorMessage() != null) {
						Message.editMessage(response.getCreatorMessage(), MessageManager.getMessage("Creator.Calendar.Confirm.Edit.Success", event), CalendarMessageFormatter.getCalendarLinkEmbed(response.getCalendar()));
						Message.deleteMessage(event);
					} else {
                		Message.sendMessage(CalendarMessageFormatter.getCalendarLinkEmbed(response.getCalendar()), MessageManager.getMessage("Creator.Calendar.Confirm.Edit.Success", event), event);
					}
                } else {
                	if (response.getCalendar() != null) {
						Message.editMessage(response.getCreatorMessage(), MessageManager.getMessage("Creator.Calendar.Confirm.Create.Success", event), CalendarMessageFormatter.getCalendarLinkEmbed(response.getCalendar()));
						Message.deleteMessage(event);
					} else {
                		Message.sendMessage(CalendarMessageFormatter.getCalendarLinkEmbed(response.getCalendar()), MessageManager.getMessage("Creator.Calendar.Confirm.Create.Success", event), event);
					}
                }
            } else {
                if (response.isEdited()) {
                	if (response.getCreatorMessage() != null) {
						Message.editMessage(response.getCreatorMessage(), MessageManager.getMessage("Creator.Calendar.Confirm.Edit.Failure", event));
						Message.deleteMessage(event);
					} else {
                		Message.sendMessage(MessageManager.getMessage("Creator.Calendar.Confirm.Edit.Failure", event), event);
					}
                } else {
                	if (response.getCreatorMessage() != null) {
						Message.editMessage(response.getCreatorMessage(), MessageManager.getMessage("Creator.Calendar.Confirm.Create.Failure", event));
						Message.deleteMessage(event);
					} else {
                		Message.sendMessage(MessageManager.getMessage("Creator.Calendar.Confirm.Create.Failure", event), event);
					}
                }
            }
        } else {
            if (calendarData.getCalendarId().equalsIgnoreCase("primary")) {
                Message.sendMessage(MessageManager.getMessage("Creator.Calendar.NoCalendar", event), event);
            } else {
                Message.sendMessage(MessageManager.getMessage("Creator.Calendar.HasCalendar", event), event);
            }
        }
    }

    private void moduleDelete(MessageReceivedEvent event, CalendarData calendarData) {
        String guildId = event.getMessage().getGuild().getID();
        if (CalendarCreator.getCreator().hasPreCalendar(guildId)) {
        	if (CalendarCreator.getCreator().getPreCalendar(guildId).getCreatorMessage() != null) {
				Message.editMessage(CalendarCreator.getCreator().getPreCalendar(guildId).getCreatorMessage(), MessageManager.getMessage("Creator.Calendar.Delete.Failure.InCreator", event));
				Message.deleteMessage(event);
			} else {
        		Message.sendMessage(MessageManager.getMessage("Creator.Calendar.Delete.Failure.InCreator", event), event);
			}
            return;
        }
        if(!event.getMessage().getAuthor().getPermissionsForGuild(event.getMessage().getGuild()).contains(
                Permissions.MANAGE_SERVER)) {
            Message.sendMessage(MessageManager.getMessage("Notification.Perm.MANAGE_SERVER", event), event);
            return;
        }
        if (!calendarData.getCalendarId().equalsIgnoreCase("primary")) {
            //Delete calendar
            if (CalendarUtils.deleteCalendar(calendarData)) {
                Message.sendMessage(MessageManager.getMessage("Creator.Calendar.Delete.Success", event), event);
            } else {
                Message.sendMessage(MessageManager.getMessage("Creator.Calendar.Delete.Failure.Unknown", event), event);
            }
        } else {
            //No calendar to delete
            Message.sendMessage(MessageManager.getMessage("Creator.Calendar.Delete.Failure.NoCalendar", event), event);
        }
    }

    private void moduleSummary(String[] args, MessageReceivedEvent event, CalendarData calendarData) {
        String guildId = event.getMessage().getGuild().getID();
        if (args.length > 1) {
            if (CalendarCreator.getCreator().hasPreCalendar(guildId)) {
            	if (CalendarCreator.getCreator().getPreCalendar(guildId).getCreatorMessage() != null) {
					CalendarCreator.getCreator().getPreCalendar(guildId).setSummary(GeneralUtils.getContent(args, 1));
					Message.editMessage(CalendarCreator.getCreator().getPreCalendar(guildId).getCreatorMessage(), MessageManager.getMessage("Creator.Calendar.Summary.N.Success", event), CalendarMessageFormatter.getPreCalendarEmbed(CalendarCreator.getCreator().getPreCalendar(guildId)));
					Message.deleteMessage(event);
				} else {
					String msg = MessageManager.getMessage("Creator.Calendar.Summary.O.Success", "%summary%", GeneralUtils.getContent(args, 1), event);
            		Message.sendMessage(msg, event);
				}
            } else {
                if (calendarData.getCalendarId().equalsIgnoreCase("primary")) {
                    Message.sendMessage(MessageManager.getMessage("Creator.Calendar.NoCalendar", event), event);
                } else {
                    Message.sendMessage(MessageManager.getMessage("Creator.Calendar.HasCalendar", event), event);
                }
            }
        } else {
            if (CalendarCreator.getCreator().hasPreCalendar(guildId)) {
            	if (CalendarCreator.getCreator().getPreCalendar(guildId).getCreatorMessage() != null) {
					Message.editMessage(CalendarCreator.getCreator().getPreCalendar(guildId).getCreatorMessage(), MessageManager.getMessage("Creator.Calendar.Summary.Specify", event));
					Message.deleteMessage(event);
				} else {
            		Message.sendMessage(MessageManager.getMessage("Creator.Calendar.Summary.Specify", event), event);
				}
            } else {
                Message.sendMessage(MessageManager.getMessage("Creator.Calendar.Summary.Specify", event), event);
            }
        }
    }

    private void moduleDescription(String[] args, MessageReceivedEvent event, CalendarData calendarData) {
        String guildId = event.getMessage().getGuild().getID();
        if (args.length > 1) {
            if (CalendarCreator.getCreator().hasPreCalendar(guildId)) {
                CalendarCreator.getCreator().getPreCalendar(guildId).setDescription(GeneralUtils.getContent(args, 1));
                if (CalendarCreator.getCreator().getPreCalendar(guildId).getCreatorMessage() != null) {
					Message.editMessage(CalendarCreator.getCreator().getPreCalendar(guildId).getCreatorMessage(), MessageManager.getMessage("Creator.Calendar.Description.N.Success", event) + TIME_ZONE_DB, CalendarMessageFormatter.getPreCalendarEmbed(CalendarCreator.getCreator().getPreCalendar(guildId)));
					Message.deleteMessage(event);
				} else {
					Message.sendMessage(MessageManager.getMessage("Creator.Calendar.Description.O.Success", "%desc%", GeneralUtils.getContent(args, 1), event) + TIME_ZONE_DB, event);
				}
            } else {
                if (calendarData.getCalendarId().equalsIgnoreCase("primary")) {
                    Message.sendMessage(MessageManager.getMessage("Creator.Calendar.NoCalendar", event), event);
                } else {
                    Message.sendMessage(MessageManager.getMessage("Creator.Calendar.HasCalendar", event), event);
                }
            }
        } else {
            if (CalendarCreator.getCreator().hasPreCalendar(guildId)) {
            	if (CalendarCreator.getCreator().getPreCalendar(guildId).getCreatorMessage() != null) {
					Message.editMessage(CalendarCreator.getCreator().getPreCalendar(guildId).getCreatorMessage(), MessageManager.getMessage("Creator.Calendar.Description.Specify", event));
					Message.deleteMessage(event);
				} else {
            		Message.sendMessage(MessageManager.getMessage("Creator.Calendar.Description.Specify", event), event);
				}
            } else {
                Message.sendMessage(MessageManager.getMessage("Creator.Calendar.Description.Specify", event), event);
            }
        }
    }

    private void moduleTimezone(String[] args, MessageReceivedEvent event, CalendarData calendarData) {
        String guildId = event.getMessage().getGuild().getID();
        if (args.length == 2) {
            String value = args[1];
            if (CalendarCreator.getCreator().hasPreCalendar(guildId)) {
                if (TimeZoneUtils.isValid(value)) {
                    CalendarCreator.getCreator().getPreCalendar(guildId).setTimezone(value);

                    if (CalendarCreator.getCreator().getPreCalendar(guildId).getCreatorMessage() != null) {
						Message.editMessage(CalendarCreator.getCreator().getPreCalendar(guildId).getCreatorMessage(), MessageManager.getMessage("Creator.Calendar.TimeZone.N.Success", event), CalendarMessageFormatter.getPreCalendarEmbed(CalendarCreator.getCreator().getPreCalendar(guildId)));
						Message.deleteMessage(event);
					} else {
                    	Message.sendMessage(MessageManager.getMessage("Creator.Calendar.TimeZone.O.Success", "%tz%", value, event), event);
					}
                } else {
                	if (CalendarCreator.getCreator().getPreCalendar(guildId).getCreatorMessage() != null) {
						Message.editMessage(CalendarCreator.getCreator().getPreCalendar(guildId).getCreatorMessage(), MessageManager.getMessage("Creator.Calendar.TimeZone.Invalid", "%tz_db%", TIME_ZONE_DB, event), CalendarMessageFormatter.getPreCalendarEmbed(CalendarCreator.getCreator().getPreCalendar(guildId)));
						Message.deleteMessage(event);
					} else {
                		Message.sendMessage(MessageManager.getMessage("Creator.Calendar.TimeZone.Invalid", "%tz_db%", TIME_ZONE_DB, event), event);
					}
                }
            } else {
                if (calendarData.getCalendarId().equalsIgnoreCase("primary")) {
                    Message.sendMessage(MessageManager.getMessage("Creator.Calendar.NoCalendar", event), event);
                } else {
                    Message.sendMessage(MessageManager.getMessage("Creator.Calendar.HasCalendar", event), event);
                }
            }
        } else {
            if (CalendarCreator.getCreator().hasPreCalendar(guildId)) {
                Message.editMessage(CalendarCreator.getCreator().getPreCalendar(guildId).getCreatorMessage(), MessageManager.getMessage("Creator.Calendar.TimeZone.Specify", event) + TIME_ZONE_DB);
                Message.deleteMessage(event);
            } else {
                Message.sendMessage(MessageManager.getMessage("Creator.Calendar.TimeZone.Specify", event) + TIME_ZONE_DB, event);
            }
        }
    }

    private void moduleEdit(MessageReceivedEvent event, CalendarData calendarData) {
        String guildId = event.getMessage().getGuild().getID();
        if (!CalendarCreator.getCreator().hasPreCalendar(guildId)) {
            if (!calendarData.getCalendarAddress().equalsIgnoreCase("primary")) {
                PreCalendar calendar = CalendarCreator.getCreator().edit(event, true);
                if (calendar.getCreatorMessage() != null) {
					Message.deleteMessage(event);
				} else {
                	Message.sendMessage(CalendarMessageFormatter.getPreCalendarEmbed(calendar), MessageManager.getMessage("Creator.Calendar.Edit.Init", event), event);
				}
            } else {
                Message.sendMessage(MessageManager.getMessage("Creator.Calendar.NoCalendar", event), event);
            }
        } else {
        	if (CalendarCreator.getCreator().getPreCalendar(guildId).getCreatorMessage() != null) {
				Message.editMessage(CalendarCreator.getCreator().getPreCalendar(guildId).getCreatorMessage(), MessageManager.getMessage("Creator.Calendar.AlreadyInit", event));
				Message.deleteMessage(event);
			} else {
        		Message.sendMessage(MessageManager.getMessage("Creator.Calendar.AlreadyInit", event), event);
			}
        }
    }
}