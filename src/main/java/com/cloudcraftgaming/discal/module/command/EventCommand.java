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
import sx.blah.discord.handle.obj.IMessage;

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
        	if (EventCreator.getCreator().getPreEvent(guildId).getCreatorMessage() != null) {
        		Message.editMessage(EventCreator.getCreator().getPreEvent(guildId).getCreatorMessage(), MessageManager.getMessage("Creator.Event.AlreadyInit", event));
			} else {
				Message.sendMessage(MessageManager.getMessage("Creator.Event.AlreadyInit", event), event);
			}
        } else {
            if (!calendarData.getCalendarAddress().equalsIgnoreCase("primary")) {
                PreEvent e = EventCreator.getCreator().init(event, true);
                if (e.getCreatorMessage() == null) {
					Message.sendMessage(MessageManager.getMessage("Creator.Event.Create.Init", event), event);
				}
            } else {
                Message.sendMessage(MessageManager.getMessage("Creator.Event.NoCalendar", event), event);
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
								Message.sendMessage(EventMessageFormatter.getPreEventEmbed(preEvent), MessageManager.getMessage("Creator.Event.Copy.Init", event), event);
							}
                        } else {
                            Message.sendMessage(MessageManager.getMessage("Notification.Error.Unknown", event), event);
                        }
                    } else {
                        Message.sendMessage(MessageManager.getMessage("Creator.Event.NotFound", event), event);
                    }
                } else {
                    Message.sendMessage(MessageManager.getMessage("Creator.Event.Copy.Specify", event), event);
                }
            } else {
				if (EventCreator.getCreator().getPreEvent(guildId).getCreatorMessage() != null) {
					Message.editMessage(EventCreator.getCreator().getPreEvent(guildId).getCreatorMessage(), MessageManager.getMessage("Creator.Event.AlreadyInit", event));
				} else {
					Message.sendMessage(MessageManager.getMessage("Creator.Event.AlreadyInit", event), event);
				}
            }
        } else {
            Message.sendMessage(MessageManager.getMessage("Creator.Event.NoCalendar", event), event);
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
							Message.sendMessage(EventMessageFormatter.getPreEventEmbed(preEvent), MessageManager.getMessage("Creator.Event.Edit.Init", event), event);
						}
                    } else {
                        Message.sendMessage(MessageManager.getMessage("Creator.Event.NotFound", event), event);
                    }
                } else {
                    Message.sendMessage(MessageManager.getMessage("Creator.Event.Edit.Specify", event), event);
                }
            } else {
				if (EventCreator.getCreator().hasCreatorMessage(guildId)) {
					Message.editMessage(EventCreator.getCreator().getCreatorMessage(guildId), MessageManager.getMessage("Creator.Event.AlreadyInit", event));
				} else {
					Message.sendMessage(MessageManager.getMessage("Creator.Event.AlreadyInit", event), event);
				}
            }
        } else {
            Message.sendMessage(MessageManager.getMessage("Creator.Event.NoCalendar", event), event);
        }
    }

    private void moduleCancel(MessageReceivedEvent event) {
    	String guildId = event.getMessage().getGuild().getID();
    	IMessage msg = null;
    	if (EventCreator.getCreator().hasCreatorMessage(guildId))
    		msg = EventCreator.getCreator().getCreatorMessage(guildId);

        if (EventCreator.getCreator().terminate(event)) {
        	if (msg != null) {
        		Message.editMessage(msg, MessageManager.getMessage("Creator.Event.Cancel.Success", event));
			} else {
				Message.sendMessage(MessageManager.getMessage("Creator.Event.Cancel.Success", event), event);
			}
        } else {
            Message.sendMessage(MessageManager.getMessage("Creator.Event.NotInit", event), event);
        }
    }

    private void moduleDelete(String[] args, MessageReceivedEvent event, CalendarData calendarData) {
        String guildId = event.getMessage().getGuild().getID();
        if (args.length == 2) {
            if (!calendarData.getCalendarAddress().equalsIgnoreCase("primary")) {
                if (!EventCreator.getCreator().hasPreEvent(guildId)) {
                    if (EventUtils.deleteEvent(guildId, args[1])) {
                        Message.sendMessage(MessageManager.getMessage("Creator.Event.Delete.Success", event), event);
                    } else {
                        Message.sendMessage(MessageManager.getMessage("Creator.Event.NotFound", event), event);
                    }
                } else {
                	if (EventCreator.getCreator().hasCreatorMessage(guildId)) {
                		Message.editMessage(EventCreator.getCreator().getCreatorMessage(guildId), MessageManager.getMessage("Creator.Event.Delete.Failure.Creator", event));
					} else {
						Message.sendMessage(MessageManager.getMessage("Creator.Event.Delete.Failure.Creator", event), event);
					}
                }
            } else {
                Message.sendMessage(MessageManager.getMessage("Creator.Event.NoCalendar", event), event);
            }
        } else {
            Message.sendMessage(MessageManager.getMessage("Creator.Event.Delete.Specify", event), event);
        }
    }

    private void moduleView(String[] args, MessageReceivedEvent event, CalendarData calendarData) {
        String guildId = event.getMessage().getGuild().getID();
        if (args.length == 1) {
            if (EventCreator.getCreator().hasPreEvent(guildId)) {
            	if (EventCreator.getCreator().hasCreatorMessage(guildId)) {
					Message.editMessage(EventCreator.getCreator().getCreatorMessage(guildId), MessageManager.getMessage("Event.View.Creator.Confirm", event), EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(guildId)));
				} else {
					Message.sendMessage(EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(guildId)), MessageManager.getMessage("Event.View.Creator.Confirm", event), event);
				}
            } else {
                Message.sendMessage(MessageManager.getMessage("Event.View.Args.Few", event), event);
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
                        Message.sendMessage(MessageManager.getMessage("Creator.Event.NotFound", event), event);
                    }
                } else {
                    Message.sendMessage(MessageManager.getMessage("Creator.Event.NoCalendar", event), event);
                }
            } else {
            	if (EventCreator.getCreator().hasCreatorMessage(guildId)) {
					Message.editMessage(EventCreator.getCreator().getCreatorMessage(guildId), MessageManager.getMessage("Event.View.Creator.Active", event));
				} else {
					Message.sendMessage(MessageManager.getMessage("Event.View.Creator.Active", event), event);
				}
            }
        } else {
            Message.sendMessage(MessageManager.getMessage("Event.View.Specify", event), event);
        }
    }

    private void moduleConfirm(MessageReceivedEvent event, CalendarData calendarData) {
        String guildId = event.getMessage().getGuild().getID();
        if (EventCreator.getCreator().hasPreEvent(guildId)) {
            if (EventCreator.getCreator().getPreEvent(guildId).hasRequiredValues()) {
                if (!calendarData.getCalendarAddress().equalsIgnoreCase("primary")) {
                    EventCreatorResponse response = EventCreator.getCreator().confirmEvent(event);
                    if (response.isSuccessful()) {
                    	if (!response.isEdited()) {
                    		if (EventCreator.getCreator().hasCreatorMessage(guildId)) {
                    			Message.editMessage(EventCreator.getCreator().getCreatorMessage(guildId), MessageManager.getMessage("Creator.event.Confirm.Creator", event), EventMessageFormatter.getEventConfirmationEmbed(response, guildId));
							} else {
								Message.sendMessage(EventMessageFormatter.getEventConfirmationEmbed(response, guildId), MessageManager.getMessage("Creator.Event.Confirm.Create", event), event);
							}
						} else {
                    		if (EventCreator.getCreator().hasCreatorMessage(guildId)) {
                    			Message.editMessage(EventCreator.getCreator().getCreatorMessage(guildId), MessageManager.getMessage("Creator.Event.Confirm.Edit", event), EventMessageFormatter.getEventConfirmationEmbed(response, guildId));
							} else {
								Message.sendMessage(EventMessageFormatter.getEventConfirmationEmbed(response, guildId), MessageManager.getMessage("Creator.Event.Confirm.Edit", event), event);
							}
						}
                    } else {
                    	if (EventCreator.getCreator().hasCreatorMessage(guildId)) {
                    		Message.editMessage(EventCreator.getCreator().getCreatorMessage(guildId), MessageManager.getMessage("Creator.Event.Confirm.Failure", event));
						} else {
							Message.sendMessage(MessageManager.getMessage("Creator.Event.Confirm.Failure", event), event);
						}
                    }
                } else {
                	if (EventCreator.getCreator().hasCreatorMessage(guildId)) {
                		Message.editMessage(EventCreator.getCreator().getCreatorMessage(guildId), MessageManager.getMessage("Creator.Event.NoCalendar", event));
					} else {
						Message.sendMessage(MessageManager.getMessage("Creator.Event.NoCalendar", event), event);
					}
                }
            } else {
            	if (EventCreator.getCreator().hasCreatorMessage(guildId)) {
            		Message.editMessage(EventCreator.getCreator().getCreatorMessage(guildId), MessageManager.getMessage("Creator.Event.NoRequired", event));
				} else {
					Message.sendMessage(MessageManager.getMessage("Creator.Event.NoRequired", event), event);
				}
            }
        } else {
            Message.sendMessage(MessageManager.getMessage("Creator.Event.NotInit", event), event);
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

                            String msg = MessageManager.getMessage("Creator.Event.Start.Success", event);
                            msg = msg.replaceAll("%date%", EventMessageFormatter.getHumanReadableDate(eventDateTimeV)).replaceAll("%time%", EventMessageFormatter.getHumanReadableTime(eventDateTimeV));
                            Message.sendMessage(msg, event);
                        } else {
                            //Oops! Time is in the past or after end...
                            Message.sendMessage(MessageManager.getMessage("Creator.Event.Start.Failure.Illegal", event), event);
                        }
                    } catch (ParseException e) {
                        Message.sendMessage(MessageManager.getMessage("Creator.Event.Time.Invalid", event), event);
                    }
                } else {
                    Message.sendMessage(MessageManager.getMessage("Creator.Event.Time.InvalidFormat", event), event);
                }
            } else {
                Message.sendMessage(MessageManager.getMessage("Creator.Event.NotInit", event), event);
            }
        } else {
            Message.sendMessage(MessageManager.getMessage("Creator.Event.Start.Specify", event), event);
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

                            String msg = MessageManager.getMessage("Creator.Event.End.Success", event);
                            msg = msg.replaceAll("%date%", EventMessageFormatter.getHumanReadableDate(eventDateTimeV)).replaceAll("%time%", EventMessageFormatter.getHumanReadableTime(eventDateTimeV));
                            Message.sendMessage(msg, event);
                        } else {
                            //Oops! Time is in the past or before the starting time...
                            Message.sendMessage(MessageManager.getMessage("Creator.Event.End.Failure.Illegal", event), event);
                        }
                    } catch (ParseException e) {
                        Message.sendMessage(MessageManager.getMessage("Creator.Event.Time.Invalid", event), event);
                    }
                } else {
                    Message.sendMessage(MessageManager.getMessage("Creator.Event.Time.InvalidFormat", event), event);
                }
            } else {
                Message.sendMessage(MessageManager.getMessage("Creator.Event.NotInit", event), event);
            }
        } else {
            Message.sendMessage(MessageManager.getMessage("Creator.Event.End.Specify", event), event);
        }
    }

    private void moduleSummary(String[] args, MessageReceivedEvent event) {
        String guildId = event.getMessage().getGuild().getID();
        if (args.length > 1) {
            if (EventCreator.getCreator().hasPreEvent(guildId)) {
                String content = GeneralUtils.getContent(args, 1);
                EventCreator.getCreator().getPreEvent(guildId).setSummary(content);
                Message.sendMessage(MessageManager.getMessage("Creator.Event.Summary.Success", "%summary%", GeneralUtils.getContent(args, 1), event), event);
            } else {
                Message.sendMessage(MessageManager.getMessage("Creator.Event.NotInit", event), event);
            }
        } else {
            Message.sendMessage(MessageManager.getMessage("Creator.Event.Summary.Specify", event), event);
        }
    }

    private void moduleDescription(String[] args, MessageReceivedEvent event) {
        String guildId = event.getMessage().getGuild().getID();
        if (args.length  > 1) {
            if (EventCreator.getCreator().hasPreEvent(guildId)) {
                String content = GeneralUtils.getContent(args, 1);
                EventCreator.getCreator().getPreEvent(guildId).setDescription(content);
                Message.sendMessage(MessageManager.getMessage("Creator.Event.Description.Success", "%description%", content, event), event);
            } else {
                Message.sendMessage(MessageManager.getMessage("Creator.Event.NotInit", event), event);
            }
        } else {
            Message.sendMessage(MessageManager.getMessage("Creator.Event.Description.Specify", event), event);
        }
    }

    private void moduleColor(String[] args, MessageReceivedEvent event) {
        String guildId = event.getMessage().getGuild().getID();
        if (args.length == 2) {
            String value = args[1];
            if (value.equalsIgnoreCase("list") || value.equalsIgnoreCase("colors") || value.equalsIgnoreCase("colours")) {
				//TODO: Make this list pretty!!!
                StringBuilder list = new StringBuilder("All Colors: ");
                for (EventColor ec : EventColor.values()) {
                    list.append(Message.lineBreak).append("Name: ").append(ec.name()).append(", ID: ").append(ec.getId());
                }
                list.append(Message.lineBreak).append(Message.lineBreak).append(MessageManager.getMessage("Creator.Event.Color.List", event));

                Message.sendMessage(list.toString().trim(), event);
            } else {
                if (EventCreator.getCreator().hasPreEvent(guildId)) {
                    //Attempt to get color.
                    if (EventColor.exists(value)) {
                        EventColor color = EventColor.fromNameOrHexOrID(value);
                        EventCreator.getCreator().getPreEvent(guildId).setColor(color);
                        Message.sendMessage(MessageManager.getMessage("Creator.Event.Color.Success", "%color%", color.name(), event), event);
                    } else {
                        Message.sendMessage(MessageManager.getMessage("Creator.Event.Color.Invalid", event), event);
                    }
                } else {
                    Message.sendMessage(MessageManager.getMessage("Creator.Event.NotInit", event), event);
                }
            }
        } else {
            Message.sendMessage(MessageManager.getMessage("Creator.Event.Color.Specify", event), event);
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
                    Message.sendMessage(MessageManager.getMessage("Creator.Event.Recur.Failure.Child", "%id%", pre.getEventId().split(" ")[0], event),event);
                    return;
                }
                try {
                    boolean value = Boolean.valueOf(valueString);
                    EventCreator.getCreator().getPreEvent(guildId).setShouldRecur(value);
                    if (value) {
                        Message.sendMessage(MessageManager.getMessage("Creator.Event.Recur.True", event), event);
                    } else {
                        Message.sendMessage(MessageManager.getMessage("Creator.Event.Recur.False", event), event);
                    }
                } catch (Exception e) {
                    //Could not convert to boolean
                    Message.sendMessage(MessageManager.getMessage("Creator.Event.Recur.Failure.Invalid", event), event);
                }
            } else {
                Message.sendMessage(MessageManager.getMessage("Creator.Event.NotInit", event), event);
            }
        } else {
            Message.sendMessage(MessageManager.getMessage("Creator.Event.Recur.Specify", event), event);
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
                        Message.sendMessage(MessageManager.getMessage("Creator.Event.Frequency.Success", "%freq%", freq.name(), event), event);
                    } else {
                        String values = Arrays.toString(EventFrequency.values()).replace("[", "").replace("]", "");
                        Message.sendMessage(MessageManager.getMessage("Creator.Event.Frequency.List", "%types%", values, event), event);
                    }
                } else {
                    Message.sendMessage(MessageManager.getMessage("Creator.Event.Recur.Not", event), event);
                }
            } else {
                Message.sendMessage(MessageManager.getMessage("Creator.Event.NotInit", event), event);
            }
        } else {
            String values = Arrays.toString(EventFrequency.values()).replace("[", "").replace("]", "");
            Message.sendMessage(MessageManager.getMessage("Creator.Event.Frequency.Specify", "%types%", values, event), event);
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
                        Message.sendMessage(MessageManager.getMessage("Creator.Event.Count.Success", "%count%", amount + "", event), event);
                    } catch (NumberFormatException e) {
                        Message.sendMessage(MessageManager.getMessage("Notification.Args.Value.Integer", event), event);
                    }
                } else {
                    Message.sendMessage(MessageManager.getMessage("Creator.Event.Recur.Not", event), event);
                }
            } else {
                Message.sendMessage(MessageManager.getMessage("Creator.Event.NotInit", event), event);
            }
        } else {
            Message.sendMessage(MessageManager.getMessage("Creator.Event.Cont.Specify", event), event);
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
                        Message.sendMessage(MessageManager.getMessage("Creator.Event.Interval.Success", "%amount%", amount + "", event), event);
                    } catch (NumberFormatException e) {
                        Message.sendMessage(MessageManager.getMessage("Notification.Args.Value.Integer", event), event);
                    }
                } else {
                    Message.sendMessage(MessageManager.getMessage("Creator.Event.Recur.Not", event), event);
                }
            } else {
                Message.sendMessage("Event Creator not initialized!", event);
            }
        } else {
            Message.sendMessage(MessageManager.getMessage("Creator.Event.Interval.Specify", event), event);
        }
    }
}