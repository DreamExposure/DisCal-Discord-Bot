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
import org.apache.commons.lang3.time.DateUtils;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
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
        info.getSubCommands().add("image");
        info.getSubCommands().add("attachment");

        return info;
    }

    /**
     * Issues the command this Object is responsible for.
     * @param args The command arguments.
     * @param event The event received.
     * @return <code>true</code> if successful, else <code>false</code>.
     */
    @Override
    public Boolean issueCommand(String[] args, MessageReceivedEvent event, GuildSettings settings) {
        long guildId = event.getGuild().getLongID();
        //TODO: Add multiple calendar handling.
        CalendarData calendarData = DatabaseManager.getManager().getMainCalendar(guildId);
        if (PermissionChecker.hasSufficientRole(event)) {
            if (args.length < 1) {
                Message.sendMessage(MessageManager.getMessage("Notification.Args.Few", settings), event);
            } else {
                switch (args[0].toLowerCase()) {
                    case "create":
                        moduleCreate(args, event, calendarData, settings);
                        break;
                    case "copy":
                        moduleCopy(args, event, calendarData, settings);
                        break;
                    case "edit":
                        if (settings.isDevGuild()) {
                            moduleEdit(args, event, calendarData, settings);
                        } else {
                            Message.sendMessage(MessageManager.getMessage("Notification.Disabled", settings), event);
                        }
                        break;
                    case "cancel":
                        moduleCancel(event, settings);
                        break;
                    case "delete":
                        moduleDelete(args, event, calendarData, settings);
                        break;
                    case "view":
                        moduleView(args, event, calendarData, settings);
                        break;
                    case "review":
                        moduleView(args, event, calendarData, settings);
                        break;
                    case "confirm":
                        moduleConfirm(event, calendarData, settings);
                        break;
                    case "startdate":
                        moduleStartDate(args, event, settings);
                        break;
                    case "start":
                        moduleStartDate(args, event, settings);
                        break;
                    case "enddate":
                        moduleEndDate(args, event, settings);
                        break;
                    case "end":
                        moduleEndDate(args, event, settings);
                        break;
                    case "summary":
                        moduleSummary(args, event, settings);
                        break;
                    case "description":
                        moduleDescription(args, event, settings);
                        break;
                    case "color":
                        moduleColor(args, event, settings);
                        break;
                    case "colour":
                        moduleColor(args, event, settings);
                        break;
					case "image":
						if (settings.isDevGuild()) {
							moduleAttachment(args, event, settings);
						} else {
							Message.sendMessage(MessageManager.getMessage("Notification.Disabled", settings), event);
						}
						break;
					case "attachment":
						if (settings.isDevGuild()) {
							moduleAttachment(args, event, settings);
						} else {
							Message.sendMessage(MessageManager.getMessage("Notification.Disabled", settings), event);
						}
						break;
                    case "recur":
                        moduleRecur(args, event, settings);
                        break;
                    case "frequency":
                        moduleFrequency(args, event, settings);
                        break;
                    case "freq":
                        moduleFrequency(args, event, settings);
                        break;
                    case "count":
                        moduleCount(args, event, settings);
                        break;
                    case "interval":
                        moduleInterval(args, event, settings);
                        break;
                    default:
                    	if (EventCreator.getCreator().hasCreatorMessage(guildId)) {
							Message.deleteMessage(event);
							Message.deleteMessage(EventCreator.getCreator().getCreatorMessage(guildId));
							EventCreator.getCreator().setCreatorMessage(Message.sendMessage(EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(guildId), settings), MessageManager.getMessage("Notification.Args.Invalid", settings), event));
						} else {
							Message.sendMessage(MessageManager.getMessage("Notification.Args.Invalid", settings), event);
						}
                        break;
                }
            }
        } else {
            Message.sendMessage(MessageManager.getMessage("Notification.Perm.CONTROL_ROLE", settings), event);
        }
        return false;
    }


    private void moduleCreate(String[] args, MessageReceivedEvent event, CalendarData calendarData, GuildSettings settings) {
        long guildId = event.getGuild().getLongID();
        if (EventCreator.getCreator().hasPreEvent(guildId)) {
        	if (EventCreator.getCreator().getPreEvent(guildId).getCreatorMessage() != null) {
				Message.deleteMessage(event);
				Message.deleteMessage(EventCreator.getCreator().getCreatorMessage(guildId));
				EventCreator.getCreator().setCreatorMessage(Message.sendMessage(EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(guildId), settings), MessageManager.getMessage("Creator.Event.AlreadyInit", settings), event));
			} else {
				Message.sendMessage(MessageManager.getMessage("Creator.Event.AlreadyInit", settings), event);
			}
        } else {
            if (!calendarData.getCalendarAddress().equalsIgnoreCase("primary")) {
				PreEvent e;
            	if (args.length == 1) {
					e = EventCreator.getCreator().init(event, settings, true);
				} else {
            		e = EventCreator.getCreator().init(event, settings, GeneralUtils.getContent(args, 1), true);
				}
                if (e.getCreatorMessage() == null) {
					Message.sendMessage(MessageManager.getMessage("Creator.Event.Create.Init", settings), event);
				}
            } else {
                Message.sendMessage(MessageManager.getMessage("Creator.Event.NoCalendar", settings), event);
            }
        }
    }

    private void moduleCopy(String[] args, MessageReceivedEvent event, CalendarData calendarData, GuildSettings settings) {
        long guildId = event.getGuild().getLongID();
        if (!calendarData.getCalendarAddress().equalsIgnoreCase("primary")) {
            if (!EventCreator.getCreator().hasPreEvent(guildId)) {
                if (args.length == 2) {
                    String eventId = args[1];
                    if (EventUtils.eventExists(guildId, eventId)) {
                        PreEvent preEvent = EventCreator.getCreator().init(event, eventId, settings, true);
                        if (preEvent != null) {
                        	if (preEvent.getCreatorMessage() == null) {
								Message.sendMessage(EventMessageFormatter.getPreEventEmbed(preEvent, settings), MessageManager.getMessage("Creator.Event.Copy.Init", settings), event);
							}
                        } else {
                            Message.sendMessage(MessageManager.getMessage("Notification.Error.Unknown", settings), event);
                        }
                    } else {
                        Message.sendMessage(MessageManager.getMessage("Creator.Event.NotFound", settings), event);
                    }
                } else {
                    Message.sendMessage(MessageManager.getMessage("Creator.Event.Copy.Specify", settings), event);
                }
            } else {
				if (EventCreator.getCreator().getPreEvent(guildId).getCreatorMessage() != null) {
					Message.deleteMessage(event);
					Message.deleteMessage(EventCreator.getCreator().getCreatorMessage(guildId));
					EventCreator.getCreator().setCreatorMessage(Message.sendMessage(EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(guildId), settings), MessageManager.getMessage("Creator.Event.AlreadyInit", settings), event));
				} else {
					Message.sendMessage(MessageManager.getMessage("Creator.Event.AlreadyInit", settings), event);
				}
            }
        } else {
            Message.sendMessage(MessageManager.getMessage("Creator.Event.NoCalendar", settings), event);
        }
    }

    private void moduleEdit(String[] args, MessageReceivedEvent event, CalendarData calendarData, GuildSettings settings) {
        long guildId = event.getGuild().getLongID();
        if (!calendarData.getCalendarAddress().equalsIgnoreCase("primary")) {
            if (!EventCreator.getCreator().hasPreEvent(guildId)) {
                if (args.length == 2) {
                    String eventId = args[1];
                    if (EventUtils.eventExists(guildId, eventId)) {
                        PreEvent preEvent = EventCreator.getCreator().edit(event, eventId, settings, true);
						if (preEvent.getCreatorMessage() == null) {
							Message.sendMessage(EventMessageFormatter.getPreEventEmbed(preEvent, settings), MessageManager.getMessage("Creator.Event.Edit.Init", settings), event);
						}
                    } else {
                        Message.sendMessage(MessageManager.getMessage("Creator.Event.NotFound", settings), event);
                    }
                } else {
                    Message.sendMessage(MessageManager.getMessage("Creator.Event.Edit.Specify", settings), event);
                }
            } else {
				if (EventCreator.getCreator().hasCreatorMessage(guildId)) {
					Message.deleteMessage(event);
					Message.deleteMessage(EventCreator.getCreator().getCreatorMessage(guildId));
					EventCreator.getCreator().setCreatorMessage(Message.sendMessage(EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(guildId), settings), MessageManager.getMessage("Creator.Event.AlreadyInit", settings), event));
				} else {
					Message.sendMessage(MessageManager.getMessage("Creator.Event.AlreadyInit", settings), event);
				}
            }
        } else {
            Message.sendMessage(MessageManager.getMessage("Creator.Event.NoCalendar", settings), event);
        }
    }

    private void moduleCancel(MessageReceivedEvent event, GuildSettings settings) {
    	long guildId = event.getGuild().getLongID();
    	IMessage msg = null;
    	if (EventCreator.getCreator().hasCreatorMessage(guildId))
    		msg = EventCreator.getCreator().getCreatorMessage(guildId);

        if (EventCreator.getCreator().terminate(event)) {
        	if (msg != null) {
				Message.deleteMessage(event);
				Message.deleteMessage(msg);
				Message.sendMessage(MessageManager.getMessage("Creator.Event.Cancel.Success", settings), event);
			} else {
				Message.sendMessage(MessageManager.getMessage("Creator.Event.Cancel.Success", settings), event);
			}
        } else {
            Message.sendMessage(MessageManager.getMessage("Creator.Event.NotInit", settings), event);
        }
    }

    private void moduleDelete(String[] args, MessageReceivedEvent event, CalendarData calendarData, GuildSettings settings) {
        long guildId = event.getGuild().getLongID();
        if (args.length == 2) {
            if (!calendarData.getCalendarAddress().equalsIgnoreCase("primary")) {
                if (!EventCreator.getCreator().hasPreEvent(guildId)) {
                    if (EventUtils.deleteEvent(guildId, args[1])) {
                        Message.sendMessage(MessageManager.getMessage("Creator.Event.Delete.Success", settings), event);
                    } else {
                        Message.sendMessage(MessageManager.getMessage("Creator.Event.NotFound", settings), event);
                    }
                } else {
                	if (EventCreator.getCreator().hasCreatorMessage(guildId)) {
						Message.deleteMessage(event);
						Message.deleteMessage(EventCreator.getCreator().getCreatorMessage(guildId));
						EventCreator.getCreator().setCreatorMessage(Message.sendMessage(EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(guildId), settings), MessageManager.getMessage("Creator.Event.Delete.Failure.Creator", settings), event));
					} else {
						Message.sendMessage(MessageManager.getMessage("Creator.Event.Delete.Failure.Creator", settings), event);
					}
                }
            } else {
                Message.sendMessage(MessageManager.getMessage("Creator.Event.NoCalendar", settings), event);
            }
        } else {
            Message.sendMessage(MessageManager.getMessage("Creator.Event.Delete.Specify", settings), event);
        }
    }

    private void moduleView(String[] args, MessageReceivedEvent event, CalendarData calendarData, GuildSettings settings) {
        long guildId = event.getGuild().getLongID();
        if (args.length == 1) {
            if (EventCreator.getCreator().hasPreEvent(guildId)) {
            	if (EventCreator.getCreator().hasCreatorMessage(guildId)) {
					Message.deleteMessage(event);
					Message.deleteMessage(EventCreator.getCreator().getCreatorMessage(guildId));
					EventCreator.getCreator().setCreatorMessage(Message.sendMessage(EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(guildId), settings), MessageManager.getMessage("Event.View.Creator.Confirm", settings), event));
				} else {
					Message.sendMessage(EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(guildId), settings), MessageManager.getMessage("Event.View.Creator.Confirm", settings), event);
				}
            } else {
                Message.sendMessage(MessageManager.getMessage("Event.View.Args.Few", settings), event);
            }
        } else if (args.length == 2) {
            //Try to get the event by ID.
            if (!EventCreator.getCreator().hasPreEvent(guildId)) {
                if (!calendarData.getCalendarAddress().equalsIgnoreCase("primary")) {
                    try {
                        Calendar service = CalendarAuth.getCalendarService();
                        Event calEvent = service.events().get(calendarData.getCalendarAddress(), args[1]).execute();
                        Message.sendMessage(EventMessageFormatter.getEventEmbed(calEvent, settings), event);
                    } catch (IOException e) {
                        //Event probably doesn't exist...
                        Message.sendMessage(MessageManager.getMessage("Creator.Event.NotFound", settings), event);
                    }
                } else {
                    Message.sendMessage(MessageManager.getMessage("Creator.Event.NoCalendar", settings), event);
                }
            } else {
            	if (EventCreator.getCreator().hasCreatorMessage(guildId)) {
					Message.deleteMessage(event);
					Message.deleteMessage(EventCreator.getCreator().getCreatorMessage(guildId));
					EventCreator.getCreator().setCreatorMessage(Message.sendMessage(EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(guildId), settings), MessageManager.getMessage("Event.View.Creator.Active", settings), event));
				} else {
					Message.sendMessage(MessageManager.getMessage("Event.View.Creator.Active", settings), event);
				}
            }
        } else {
            Message.sendMessage(MessageManager.getMessage("Event.View.Specify", settings), event);
        }
    }

    private void moduleConfirm(MessageReceivedEvent event, CalendarData calendarData, GuildSettings settings) {
        long guildId = event.getGuild().getLongID();
        if (EventCreator.getCreator().hasPreEvent(guildId)) {
            if (EventCreator.getCreator().getPreEvent(guildId).hasRequiredValues()) {
                if (!calendarData.getCalendarAddress().equalsIgnoreCase("primary")) {
                    EventCreatorResponse response = EventCreator.getCreator().confirmEvent(event);
                    if (response.isSuccessful()) {
                    	if (!response.isEdited()) {
                    		if (EventCreator.getCreator().hasCreatorMessage(guildId)) {
								Message.deleteMessage(event);
								Message.deleteMessage(EventCreator.getCreator().getCreatorMessage(guildId));
								EventCreator.getCreator().setCreatorMessage(Message.sendMessage(EventMessageFormatter.getEventConfirmationEmbed(response, settings), MessageManager.getMessage("Creator.Event.Confirm.Create", settings), event));
							} else {
								Message.sendMessage(EventMessageFormatter.getEventConfirmationEmbed(response, settings), MessageManager.getMessage("Creator.Event.Confirm.Create", settings), event);
							}
						} else {
                    		if (EventCreator.getCreator().hasCreatorMessage(guildId)) {
								Message.deleteMessage(event);
								Message.deleteMessage(EventCreator.getCreator().getCreatorMessage(guildId));
								EventCreator.getCreator().setCreatorMessage(Message.sendMessage(EventMessageFormatter.getEventConfirmationEmbed(response, settings), MessageManager.getMessage("Creator.Event.Confirm.Edit", settings), event));
							} else {
								Message.sendMessage(EventMessageFormatter.getEventConfirmationEmbed(response, settings), MessageManager.getMessage("Creator.Event.Confirm.Edit", settings), event);
							}
						}
                    } else {
                    	if (EventCreator.getCreator().hasCreatorMessage(guildId)) {
							Message.deleteMessage(event);
							Message.deleteMessage(EventCreator.getCreator().getCreatorMessage(guildId));
							EventCreator.getCreator().setCreatorMessage(Message.sendMessage(EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(guildId), settings), MessageManager.getMessage("Creator.Event.Confirm.Failure", settings), event));
						} else {
							Message.sendMessage(MessageManager.getMessage("Creator.Event.Confirm.Failure", settings), event);
						}
                    }
                } else {
                	if (EventCreator.getCreator().hasCreatorMessage(guildId)) {
						Message.deleteMessage(event);
						Message.deleteMessage(EventCreator.getCreator().getCreatorMessage(guildId));
						EventCreator.getCreator().setCreatorMessage(Message.sendMessage(EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(guildId), settings), MessageManager.getMessage("Creator.Event.NoCalendar", settings), event));
					} else {
						Message.sendMessage(MessageManager.getMessage("Creator.Event.NoCalendar", settings), event);
					}
                }
            } else {
            	if (EventCreator.getCreator().hasCreatorMessage(guildId)) {
					Message.deleteMessage(event);
					Message.deleteMessage(EventCreator.getCreator().getCreatorMessage(guildId));
					EventCreator.getCreator().setCreatorMessage(Message.sendMessage(EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(guildId), settings), MessageManager.getMessage("Creator.Event.NoRequired", settings), event));
				} else {
					Message.sendMessage(MessageManager.getMessage("Creator.Event.NoRequired", settings), event);
				}
            }
        } else {
            Message.sendMessage(MessageManager.getMessage("Creator.Event.NotInit", settings), event);
        }
    }

    private void moduleStartDate(String[] args, MessageReceivedEvent event, GuildSettings settings) {
        long guildId = event.getGuild().getLongID();
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

                            //To streamline, check if event end is null, if so, apply 2 hour duration!
							if (EventCreator.getCreator().getPreEvent(guildId).getEndDateTime() == null) {
								//Actual date -- Fuck me, need to do conversions again because its stupid.
								String endRaw = EventUtils.applyHoursToRawUserInput(dateRaw, 2);
								Date endDateObj = sdf.parse(endRaw);
								DateTime endDateTime = new DateTime(endDateObj);
								EventDateTime eventEndDateTime = new EventDateTime();
								eventDateTime.setDateTime(endDateTime);
								EventCreator.getCreator().getPreEvent(guildId).setEndDateTime(eventEndDateTime);

								//Viewable date
								Date endDateObjV = DateUtils.addHours(dateObjV, 2);
								DateTime endDateTimeV = new DateTime(endDateObjV);
								EventDateTime eventEndDateTimeV = new EventDateTime();
								eventEndDateTimeV.setDateTime(endDateTimeV);
								EventCreator.getCreator().getPreEvent(guildId).setViewableEndDate(eventEndDateTimeV);

								//I know, the above is stupid but I think it works :/
							}

                            if (EventCreator.getCreator().hasCreatorMessage(guildId)) {
								Message.deleteMessage(event);
								Message.deleteMessage(EventCreator.getCreator().getCreatorMessage(guildId));
								EventCreator.getCreator().setCreatorMessage(Message.sendMessage(EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(guildId), settings), MessageManager.getMessage("Creator.Event.Start.Success.New", settings), event));
							} else {
								String msg = MessageManager.getMessage("Creator.Event.Start.Success", settings);
								msg = msg.replaceAll("%date%", EventMessageFormatter.getHumanReadableDate(eventDateTimeV)).replaceAll("%time%", EventMessageFormatter.getHumanReadableTime(eventDateTimeV));
								Message.sendMessage(msg, event);
							}
                        } else {
							//Oops! Time is in the past or after end...
							if (EventCreator.getCreator().hasCreatorMessage(guildId)) {
								Message.deleteMessage(event);
								Message.deleteMessage(EventCreator.getCreator().getCreatorMessage(guildId));
								EventCreator.getCreator().setCreatorMessage(Message.sendMessage(EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(guildId), settings), MessageManager.getMessage("Creator.Event.Start.Failure.Illegal", settings), event));
							} else {
								Message.sendMessage(MessageManager.getMessage("Creator.Event.Start.Failure.Illegal", settings), event);
							}
						}
                    } catch (ParseException e) {
						if (EventCreator.getCreator().hasCreatorMessage(guildId)) {
							Message.deleteMessage(event);
							Message.deleteMessage(EventCreator.getCreator().getCreatorMessage(guildId));
							EventCreator.getCreator().setCreatorMessage(Message.sendMessage(EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(guildId), settings), MessageManager.getMessage("Creator.Event.Time.Invalid", settings), event));
						} else {
							Message.sendMessage(MessageManager.getMessage("Creator.Event.Time.Invalid", settings), event);
						}
					}
                } else {
					if (EventCreator.getCreator().hasCreatorMessage(guildId)) {
						Message.deleteMessage(event);
						Message.deleteMessage(EventCreator.getCreator().getCreatorMessage(guildId));
						EventCreator.getCreator().setCreatorMessage(Message.sendMessage(EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(guildId), settings), MessageManager.getMessage("Creator.Event.Time.InvalidFormat", settings), event));
					} else {
						Message.sendMessage(MessageManager.getMessage("Creator.Event.Time.InvalidFormat", settings), event);
					}
				}
            } else {
                Message.sendMessage(MessageManager.getMessage("Creator.Event.NotInit", settings), event);
            }
        } else {
			if (EventCreator.getCreator().hasCreatorMessage(guildId)) {
				Message.deleteMessage(event);
				Message.deleteMessage(EventCreator.getCreator().getCreatorMessage(guildId));
				EventCreator.getCreator().setCreatorMessage(Message.sendMessage(EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(guildId), settings), MessageManager.getMessage("Creator.Event.Start.Specify", settings), event));
			} else {
				Message.sendMessage(MessageManager.getMessage("Creator.Event.Start.Specify", settings), event);
			}
		}
    }

    private void moduleEndDate(String[] args, MessageReceivedEvent event, GuildSettings settings) {
        long guildId = event.getGuild().getLongID();
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

                            if (EventCreator.getCreator().hasCreatorMessage(guildId)) {
								Message.deleteMessage(event);
								Message.deleteMessage(EventCreator.getCreator().getCreatorMessage(guildId));
								EventCreator.getCreator().setCreatorMessage(Message.sendMessage(EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(guildId), settings), MessageManager.getMessage("Creator.Event.End.Success.New", settings), event));
							} else {
								String msg = MessageManager.getMessage("Creator.Event.End.Success", settings);
								msg = msg.replaceAll("%date%", EventMessageFormatter.getHumanReadableDate(eventDateTimeV)).replaceAll("%time%", EventMessageFormatter.getHumanReadableTime(eventDateTimeV));
								Message.sendMessage(msg, event);
							}
                        } else {
							//Oops! Time is in the past or before the starting time...
							if (EventCreator.getCreator().hasCreatorMessage(guildId)) {
								Message.deleteMessage(event);
								Message.deleteMessage(EventCreator.getCreator().getCreatorMessage(guildId));
								EventCreator.getCreator().setCreatorMessage(Message.sendMessage(EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(guildId), settings), MessageManager.getMessage("Creator.Event.End.Failure.Illegal", settings), event));
							} else {
								Message.sendMessage(MessageManager.getMessage("Creator.Event.End.Failure.Illegal", settings), event);
							}
						}
                    } catch (ParseException e) {
						if (EventCreator.getCreator().hasCreatorMessage(guildId)) {
							Message.deleteMessage(event);
							Message.deleteMessage(EventCreator.getCreator().getCreatorMessage(guildId));
							EventCreator.getCreator().setCreatorMessage(Message.sendMessage(EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(guildId), settings), MessageManager.getMessage("Creator.Event.Time.Invalid", settings), event));
						} else {
							Message.sendMessage(MessageManager.getMessage("Creator.Event.Time.Invalid", settings), event);
						}
					}
                } else {
					if (EventCreator.getCreator().hasCreatorMessage(guildId)) {
						Message.deleteMessage(event);
						Message.deleteMessage(EventCreator.getCreator().getCreatorMessage(guildId));
						EventCreator.getCreator().setCreatorMessage(Message.sendMessage(EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(guildId), settings), MessageManager.getMessage("Creator.Event.Time.InvalidFormat", settings), event));
					} else {
						Message.sendMessage(MessageManager.getMessage("Creator.Event.Time.InvalidFormat", settings), event);
					}
				}
            } else {
                Message.sendMessage(MessageManager.getMessage("Creator.Event.NotInit", settings), event);
            }
        } else {
			if (EventCreator.getCreator().hasCreatorMessage(guildId)) {
				Message.deleteMessage(event);
				Message.deleteMessage(EventCreator.getCreator().getCreatorMessage(guildId));
				EventCreator.getCreator().setCreatorMessage(Message.sendMessage(EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(guildId), settings), MessageManager.getMessage("Creator.Event.End.Specify", settings), event));
			} else {
				Message.sendMessage(MessageManager.getMessage("Creator.Event.End.Specify", settings), event);
			}
		}
    }

    private void moduleSummary(String[] args, MessageReceivedEvent event, GuildSettings settings) {
        long guildId = event.getGuild().getLongID();
        if (args.length > 1) {
            if (EventCreator.getCreator().hasPreEvent(guildId)) {
                String content = GeneralUtils.getContent(args, 1);
                EventCreator.getCreator().getPreEvent(guildId).setSummary(content);
                if (EventCreator.getCreator().hasCreatorMessage(guildId)) {
					Message.deleteMessage(event);
					Message.deleteMessage(EventCreator.getCreator().getCreatorMessage(guildId));
					EventCreator.getCreator().setCreatorMessage(Message.sendMessage(EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(guildId), settings), MessageManager.getMessage("Creator.Event.Summary.Success.New", settings), event));
				} else {
					Message.sendMessage(MessageManager.getMessage("Creator.Event.Summary.Success", "%summary%", GeneralUtils.getContent(args, 1), settings), event);
				}
            } else {
                Message.sendMessage(MessageManager.getMessage("Creator.Event.NotInit", settings), event);
            }
        } else {
			if (EventCreator.getCreator().hasCreatorMessage(guildId)) {
				Message.deleteMessage(event);
				Message.deleteMessage(EventCreator.getCreator().getCreatorMessage(guildId));
				EventCreator.getCreator().setCreatorMessage(Message.sendMessage(EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(guildId), settings), MessageManager.getMessage("Creator.Event.Summary.Specify", settings), event));
			} else {
				Message.sendMessage(MessageManager.getMessage("Creator.Event.Summary.Specify", settings), event);
			}
		}
    }

    private void moduleDescription(String[] args, MessageReceivedEvent event, GuildSettings settings) {
        long guildId = event.getGuild().getLongID();
        if (args.length  > 1) {
            if (EventCreator.getCreator().hasPreEvent(guildId)) {
                String content = GeneralUtils.getContent(args, 1);
                EventCreator.getCreator().getPreEvent(guildId).setDescription(content);
                if (EventCreator.getCreator().hasCreatorMessage(guildId)) {
					Message.deleteMessage(event);
					Message.deleteMessage(EventCreator.getCreator().getCreatorMessage(guildId));
					EventCreator.getCreator().setCreatorMessage(Message.sendMessage(EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(guildId), settings), MessageManager.getMessage("Creator.Event.Description.Success.New", settings), event));
				} else {
					Message.sendMessage(MessageManager.getMessage("Creator.Event.Description.Success", "%description%", content, settings), event);
				}
            } else {
                Message.sendMessage(MessageManager.getMessage("Creator.Event.NotInit", settings), event);
            }
        } else {
			if (EventCreator.getCreator().hasCreatorMessage(guildId)) {
				Message.deleteMessage(event);
				Message.deleteMessage(EventCreator.getCreator().getCreatorMessage(guildId));
				EventCreator.getCreator().setCreatorMessage(Message.sendMessage(EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(guildId), settings), MessageManager.getMessage("Creator.Event.Description.Specify", settings), event));
			} else {
				Message.sendMessage(MessageManager.getMessage("Creator.Event.Description.Specify", settings), event);
			}
		}
    }

    private void moduleColor(String[] args, MessageReceivedEvent event, GuildSettings settings) {
        long guildId = event.getGuild().getLongID();
        if (args.length == 2) {
            String value = args[1];
            if (value.equalsIgnoreCase("list") || value.equalsIgnoreCase("colors") || value.equalsIgnoreCase("colours")) {
				//TODO: Make this list pretty!!!
                StringBuilder list = new StringBuilder("All Colors: ");
                for (EventColor ec : EventColor.values()) {
                    list.append(Message.lineBreak).append("Name: ").append(ec.name()).append(", ID: ").append(ec.getId());
                }
                list.append(Message.lineBreak).append(Message.lineBreak).append(MessageManager.getMessage("Creator.Event.Color.List", settings));

                if (EventCreator.getCreator().hasCreatorMessage(guildId)) {
					Message.deleteMessage(event);
					Message.deleteMessage(EventCreator.getCreator().getCreatorMessage(guildId));
					EventCreator.getCreator().setCreatorMessage(Message.sendMessage(EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(guildId), settings), list.toString().trim(), event));
				} else {
					Message.sendMessage(list.toString().trim(), event);
				}
            } else {
                if (EventCreator.getCreator().hasPreEvent(guildId)) {
                    //Attempt to get color.
                    if (EventColor.exists(value)) {
                        EventColor color = EventColor.fromNameOrHexOrID(value);
                        EventCreator.getCreator().getPreEvent(guildId).setColor(color);
                        if (EventCreator.getCreator().hasCreatorMessage(guildId)) {
							Message.deleteMessage(event);
							Message.deleteMessage(EventCreator.getCreator().getCreatorMessage(guildId));
							EventCreator.getCreator().setCreatorMessage(Message.sendMessage(EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(guildId), settings), MessageManager.getMessage("Creator.Event.Color.Success.New", settings), event));
						} else {
							Message.sendMessage(MessageManager.getMessage("Creator.Event.Color.Success", "%color%", color.name(), settings), event);
						}
                    } else {
						if (EventCreator.getCreator().hasCreatorMessage(guildId)) {
							Message.deleteMessage(event);
							Message.deleteMessage(EventCreator.getCreator().getCreatorMessage(guildId));
							EventCreator.getCreator().setCreatorMessage(Message.sendMessage(EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(guildId), settings), MessageManager.getMessage("Creator.Event.Color.Invalid", settings), event));
						} else {
							Message.sendMessage(MessageManager.getMessage("Creator.Event.Color.Invalid", settings), event);
						}
					}
                } else {
                    Message.sendMessage(MessageManager.getMessage("Creator.Event.NotInit", settings), event);
                }
            }
        } else {
			if (EventCreator.getCreator().hasCreatorMessage(guildId)) {
				Message.deleteMessage(event);
				Message.deleteMessage(EventCreator.getCreator().getCreatorMessage(guildId));
				EventCreator.getCreator().setCreatorMessage(Message.sendMessage(EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(guildId), settings), MessageManager.getMessage("Creator.Event.Color.Specify", settings), event));
			} else {
				Message.sendMessage(MessageManager.getMessage("Creator.Event.Color.Specify", settings), event);
			}
		}
    }

    private void moduleAttachment(String[] args, MessageReceivedEvent event, GuildSettings settings) {
    	long guildId = event.getGuild().getLongID();
    	if (args.length == 2) {
    		String value = args[1];
			if (EventCreator.getCreator().hasPreEvent(guildId)) {
				if (ImageUtils.validate(value)) {
					EventCreator.getCreator().getPreEvent(guildId).getEventData().setImageLink(value);

					if (EventCreator.getCreator().hasCreatorMessage(guildId)) {
						Message.deleteMessage(event);
						Message.deleteMessage(EventCreator.getCreator().getCreatorMessage(guildId));
						EventCreator.getCreator().setCreatorMessage(Message.sendMessage(EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(guildId), settings), MessageManager.getMessage("Creator.Event.Attachment.Success", settings), event));
					} else {
						Message.sendMessage(MessageManager.getMessage("Creator.Event.Attachment.Success", settings), event);
					}
				} else {
					if (EventCreator.getCreator().hasCreatorMessage(guildId)) {
						Message.deleteMessage(event);
						Message.deleteMessage(EventCreator.getCreator().getCreatorMessage(guildId));
						EventCreator.getCreator().setCreatorMessage(Message.sendMessage(EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(guildId), settings), MessageManager.getMessage("Creator.Event.Attachment.Failure", settings), event));
					}
				}
			} else {
				Message.sendMessage(MessageManager.getMessage("Creator.Event.NotInit", settings), event);
			}
		} else {
			Message.sendMessage(MessageManager.getMessage("Creator.Event.Attachment.Specify", settings), event);
		}
	}

    //Event recurrence settings
    private void moduleRecur(String[] args, MessageReceivedEvent event, GuildSettings settings) {
        long guildId = event.getGuild().getLongID();
        if (args.length == 2) {
            String valueString = args[1];
            if (EventCreator.getCreator().hasPreEvent(guildId)) {
                PreEvent pre = EventCreator.getCreator().getPreEvent(guildId);
                if (pre.isEditing() && pre.getEventId().contains("_")) {
                	if (EventCreator.getCreator().hasCreatorMessage(guildId)) {
						Message.deleteMessage(event);
						Message.deleteMessage(EventCreator.getCreator().getCreatorMessage(guildId));
						EventCreator.getCreator().setCreatorMessage(Message.sendMessage(EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(guildId), settings), MessageManager.getMessage("Creator.Event.Recur.Failure.Child", "%id%", pre.getEventId().split("_")[0], settings), event));
					} else {
						Message.sendMessage(MessageManager.getMessage("Creator.Event.Recur.Failure.Child", "%id%", pre.getEventId().split("_")[0], settings), event);
					}
                    return;
                }
                try {
                    boolean value = Boolean.valueOf(valueString);
                    EventCreator.getCreator().getPreEvent(guildId).setShouldRecur(value);
                    if (value) {
                    	if (EventCreator.getCreator().hasCreatorMessage(guildId)) {
							Message.deleteMessage(event);
							Message.deleteMessage(EventCreator.getCreator().getCreatorMessage(guildId));
							EventCreator.getCreator().setCreatorMessage(Message.sendMessage(EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(guildId), settings), MessageManager.getMessage("Creator.Event.Recur.True", settings), event));
						} else {
							Message.sendMessage(MessageManager.getMessage("Creator.Event.Recur.True", settings), event);
						}
                    } else {
						if (EventCreator.getCreator().hasCreatorMessage(guildId)) {
							Message.deleteMessage(event);
							Message.deleteMessage(EventCreator.getCreator().getCreatorMessage(guildId));
							EventCreator.getCreator().setCreatorMessage(Message.sendMessage(EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(guildId), settings), MessageManager.getMessage("Creator.Event.Recur.False", settings), event));
						} else {
							Message.sendMessage(MessageManager.getMessage("Creator.Event.Recur.False", settings), event);
						}
					}
                } catch (Exception e) {
					//Could not convert to boolean
					if (EventCreator.getCreator().hasCreatorMessage(guildId)) {
						Message.deleteMessage(event);
						Message.deleteMessage(EventCreator.getCreator().getCreatorMessage(guildId));
						EventCreator.getCreator().setCreatorMessage(Message.sendMessage(EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(guildId), settings), MessageManager.getMessage("Creator.Event.Recur.Failure.Invalid", settings), event));
					} else {
						Message.sendMessage(MessageManager.getMessage("Creator.Event.Recur.Failure.Invalid", settings), event);
					}
				}
            } else {
                Message.sendMessage(MessageManager.getMessage("Creator.Event.NotInit", settings), event);
            }
        } else {
			if (EventCreator.getCreator().hasCreatorMessage(guildId)) {
				Message.deleteMessage(event);
				Message.deleteMessage(EventCreator.getCreator().getCreatorMessage(guildId));
				EventCreator.getCreator().setCreatorMessage(Message.sendMessage(EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(guildId), settings), MessageManager.getMessage("Creator.Event.Recur.Specify", settings), event));
			} else {
				Message.sendMessage(MessageManager.getMessage("Creator.Event.Recur.Specify", settings), event);
			}
		}
    }

    private void moduleFrequency(String[] args, MessageReceivedEvent event, GuildSettings settings) {
        long guildId = event.getGuild().getLongID();
        if (args.length == 2) {
            if (EventCreator.getCreator().hasPreEvent(guildId)) {
                if (EventCreator.getCreator().getPreEvent(guildId).shouldRecur()) {
                    String value = args[1];
                    if (EventFrequency.isValid(value)) {
                        EventFrequency freq = EventFrequency.fromValue(value);
                        EventCreator.getCreator().getPreEvent(guildId).getRecurrence().setFrequency(freq);
                        if (EventCreator.getCreator().hasCreatorMessage(guildId)) {
                        	Message.deleteMessage(event);
                        	Message.deleteMessage(EventCreator.getCreator().getCreatorMessage(guildId));
                        	EventCreator.getCreator().setCreatorMessage(Message.sendMessage(EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(guildId), settings), MessageManager.getMessage("Creator.Event.Frequency.Success.New", settings), event));
						} else {
							Message.sendMessage(MessageManager.getMessage("Creator.Event.Frequency.Success", "%freq%", freq.name(), settings), event);
						}
                    } else {
						String values = Arrays.toString(EventFrequency.values()).replace("[", "").replace("]", "");
						if (EventCreator.getCreator().hasCreatorMessage(guildId)) {
							Message.deleteMessage(event);
							Message.deleteMessage(EventCreator.getCreator().getCreatorMessage(guildId));
							EventCreator.getCreator().setCreatorMessage(Message.sendMessage(EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(guildId), settings), MessageManager.getMessage("Creator.Event.Frequency.List", "%types%", value, settings), event));
						} else {
							Message.sendMessage(MessageManager.getMessage("Creator.Event.Frequency.List", "%types%", values, settings), event);
						}
					}
                } else {
					if (EventCreator.getCreator().hasCreatorMessage(guildId)) {
						Message.deleteMessage(event);
						Message.deleteMessage(EventCreator.getCreator().getCreatorMessage(guildId));
						EventCreator.getCreator().setCreatorMessage(Message.sendMessage(EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(guildId), settings), MessageManager.getMessage("Creator.Event.Recur.Not", settings), event));
					} else {
						Message.sendMessage(MessageManager.getMessage("Creator.Event.Recur.Not", settings), event);
					}
				}
            } else {
                Message.sendMessage(MessageManager.getMessage("Creator.Event.NotInit", settings), event);
            }
        } else {
			String values = Arrays.toString(EventFrequency.values()).replace("[", "").replace("]", "");
			if (EventCreator.getCreator().hasCreatorMessage(guildId)) {
				Message.deleteMessage(event);
				Message.deleteMessage(EventCreator.getCreator().getCreatorMessage(guildId));
				EventCreator.getCreator().setCreatorMessage(Message.sendMessage(EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(guildId), settings), MessageManager.getMessage("Creator.Event.Frequency.Specify", "%types%", values, settings), event));
			} else {
				Message.sendMessage(MessageManager.getMessage("Creator.Event.Frequency.Specify", "%types%", values, settings), event);
			}
		}
    }

    private void moduleCount(String[] args, MessageReceivedEvent event, GuildSettings settings) {
        long guildId = event.getGuild().getLongID();
        if (args.length == 2) {
            if (EventCreator.getCreator().hasPreEvent(guildId)) {
                if (EventCreator.getCreator().getPreEvent(guildId).shouldRecur()) {
                    try {
                        Integer amount = Integer.valueOf(args[1]);
                        EventCreator.getCreator().getPreEvent(guildId).getRecurrence().setCount(amount);
                        if (EventCreator.getCreator().hasCreatorMessage(guildId)) {
                        	Message.deleteMessage(event);
							Message.deleteMessage(EventCreator.getCreator().getCreatorMessage(guildId));
							EventCreator.getCreator().setCreatorMessage(Message.sendMessage(EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(guildId), settings), MessageManager.getMessage("Creator.Event.Count.Success.New", settings), event));
						} else {
							Message.sendMessage(MessageManager.getMessage("Creator.Event.Count.Success", "%count%", amount + "", settings), event);
						}
                    } catch (NumberFormatException e) {
						if (EventCreator.getCreator().hasCreatorMessage(guildId)) {
							Message.deleteMessage(event);
							Message.deleteMessage(EventCreator.getCreator().getCreatorMessage(guildId));
							EventCreator.getCreator().setCreatorMessage(Message.sendMessage(EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(guildId), settings), MessageManager.getMessage("Notification.Args.Value.Integer", settings), event));
						} else {
							Message.sendMessage(MessageManager.getMessage("Notification.Args.Value.Integer", settings), event);
						}
					}
                } else {
					if (EventCreator.getCreator().hasCreatorMessage(guildId)) {
						Message.deleteMessage(event);
						Message.deleteMessage(EventCreator.getCreator().getCreatorMessage(guildId));
						EventCreator.getCreator().setCreatorMessage(Message.sendMessage(EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(guildId), settings), MessageManager.getMessage("Creator.Event.Recur.Not", settings), event));
					} else {
						Message.sendMessage(MessageManager.getMessage("Creator.Event.Recur.Not", settings), event);
					}
				}
            } else {
                Message.sendMessage(MessageManager.getMessage("Creator.Event.NotInit", settings), event);
            }
        } else {
			if (EventCreator.getCreator().hasCreatorMessage(guildId)) {
				Message.deleteMessage(event);
				Message.deleteMessage(EventCreator.getCreator().getCreatorMessage(guildId));
				EventCreator.getCreator().setCreatorMessage(Message.sendMessage(EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(guildId), settings), MessageManager.getMessage("Creator.Event.Count.Specify", settings), event));
			} else {
				Message.sendMessage(MessageManager.getMessage("Creator.Event.Count.Specify", settings), event);
			}
		}
    }

    private void moduleInterval(String[] args, MessageReceivedEvent event, GuildSettings settings) {
        long guildId = event.getGuild().getLongID();
        if (args.length == 2) {
            if (EventCreator.getCreator().hasPreEvent(guildId)) {
                if (EventCreator.getCreator().getPreEvent(guildId).shouldRecur()) {
                    try {
                        Integer amount = Integer.valueOf(args[1]);
                        EventCreator.getCreator().getPreEvent(guildId).getRecurrence().setInterval(amount);
						if (EventCreator.getCreator().hasCreatorMessage(guildId)) {
							Message.deleteMessage(event);
							Message.deleteMessage(EventCreator.getCreator().getCreatorMessage(guildId));
							EventCreator.getCreator().setCreatorMessage(Message.sendMessage(EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(guildId), settings), MessageManager.getMessage("Creator.Event.Interval.Success.New", settings), event));
						} else {
							Message.sendMessage(MessageManager.getMessage("Creator.Event.Interval.Success", "%amount%", amount + "", settings), event);
						}
                    } catch (NumberFormatException e) {
						if (EventCreator.getCreator().hasCreatorMessage(guildId)) {
							Message.deleteMessage(event);
							Message.deleteMessage(EventCreator.getCreator().getCreatorMessage(guildId));
							EventCreator.getCreator().setCreatorMessage(Message.sendMessage(EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(guildId), settings), MessageManager.getMessage("Notification.Args.Value.Integer", settings), event));
						} else {
							Message.sendMessage(MessageManager.getMessage("Notification.Args.Value.Integer", settings), event);
						}
					}
                } else {
                	if (EventCreator.getCreator().hasCreatorMessage(guildId)) {
						Message.deleteMessage(event);
						Message.deleteMessage(EventCreator.getCreator().getCreatorMessage(guildId));
						EventCreator.getCreator().setCreatorMessage(Message.sendMessage(EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(guildId), settings), MessageManager.getMessage("Creator.Event.Recur.Not", settings), event));
					}
                    Message.sendMessage(MessageManager.getMessage("Creator.Event.Recur.Not", settings), event);
                }
            } else {
                Message.sendMessage("Event Creator not initialized!", event);
            }
        } else {
			if (EventCreator.getCreator().hasCreatorMessage(guildId)) {
				Message.deleteMessage(event);
				Message.deleteMessage(EventCreator.getCreator().getCreatorMessage(guildId));
				EventCreator.getCreator().setCreatorMessage(Message.sendMessage(EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(guildId), settings), MessageManager.getMessage("Creator.Event.Interval.Specify", settings), event));
			} else {
				Message.sendMessage(MessageManager.getMessage("Creator.Event.Interval.Specify", settings), event);
			}
		}
    }
}