package com.cloudcraftgaming.discal.bot.module.command;

import com.cloudcraftgaming.discal.api.DisCalAPI;
import com.cloudcraftgaming.discal.api.calendar.CalendarAuth;
import com.cloudcraftgaming.discal.api.database.DatabaseManager;
import com.cloudcraftgaming.discal.api.enums.event.EventColor;
import com.cloudcraftgaming.discal.api.enums.event.EventFrequency;
import com.cloudcraftgaming.discal.api.message.Message;
import com.cloudcraftgaming.discal.api.message.MessageManager;
import com.cloudcraftgaming.discal.api.object.GuildSettings;
import com.cloudcraftgaming.discal.api.object.calendar.CalendarData;
import com.cloudcraftgaming.discal.api.object.command.CommandInfo;
import com.cloudcraftgaming.discal.api.object.event.EventCreatorResponse;
import com.cloudcraftgaming.discal.api.object.event.PreEvent;
import com.cloudcraftgaming.discal.api.utils.*;
import com.cloudcraftgaming.discal.bot.internal.calendar.event.EventCreator;
import com.cloudcraftgaming.discal.bot.internal.calendar.event.EventMessageFormatter;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;

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
	 *
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
		ArrayList<String> a = new ArrayList<>();
		a.add("e");

		return a;
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

		info.getSubCommands().put("create", "Creates a new event");
		info.getSubCommands().put("copy", "Copies an existing event");
		info.getSubCommands().put("edit", "Edits an existing event");
		info.getSubCommands().put("cancel", "Cancels the creator/editor");
		info.getSubCommands().put("restart", "Restarts the creator/editor");
		info.getSubCommands().put("delete", "Deletes an existing event");
		info.getSubCommands().put("view", "Views an existing event");
		info.getSubCommands().put("review", "Reviews the event in the creator/editor");
		info.getSubCommands().put("confirm", "Confirms and creates/edits the event");
		info.getSubCommands().put("start", "Sets the start of the event (format: yyyy/MM/dd-hh:mm:ss)");
		info.getSubCommands().put("startdate", "Sets the start of the event (format: yyyy/MM/dd-hh:mm:ss)");
		info.getSubCommands().put("end", "Sets the end of the event (format: yyyy/MM/dd-hh:mm:ss)");
		info.getSubCommands().put("enddate", "Sets the end of the event (format: yyyy/MM/dd-hh:mm:ss)");
		info.getSubCommands().put("summary", "Sets the summary/name of the event");
		info.getSubCommands().put("description", "Sets the description of the event");
		info.getSubCommands().put("color", "Sets the color of the event");
		info.getSubCommands().put("colour", "Sets the colour of the event");
		info.getSubCommands().put("location", "Sets the location of the event");
		info.getSubCommands().put("loc", "Sets the location of the event");
		info.getSubCommands().put("recur", "True/False whether or not the event should recur");
		info.getSubCommands().put("frequency", "Sets how often the event should recur");
		info.getSubCommands().put("freq", "Sets how often the event should recur");
		info.getSubCommands().put("count", "Sets how many times the event should recur (`-1` or `0` for infinite)");
		info.getSubCommands().put("interval", "Sets the interval at which the event should recur according to the frequency");
		info.getSubCommands().put("image", "Sets the event's image");
		info.getSubCommands().put("attachment", "Sets the event's image");

		return info;
	}

	/**
	 * Issues the command this Object is responsible for.
	 *
	 * @param args  The command arguments.
	 * @param event The event received.
	 * @return <code>true</code> if successful, else <code>false</code>.
	 */
	@Override
	public boolean issueCommand(String[] args, MessageReceivedEvent event, GuildSettings settings) {
		long guildId = event.getGuild().getLongID();
		//TODO: Add multiple calendar handling.
		CalendarData calendarData = DatabaseManager.getManager().getMainCalendar(guildId);
		if (args.length < 1) {
			Message.sendMessage(MessageManager.getMessage("Notification.Args.Few", settings), event);
		} else {
			switch (args[0].toLowerCase()) {
				case "create":
					if (PermissionChecker.hasSufficientRole(event))
						moduleCreate(args, event, calendarData, settings);
					else
						Message.sendMessage(MessageManager.getMessage("Notification.Perm.CONTROL_ROLE", settings), event);
					break;
				case "copy":
					if (PermissionChecker.hasSufficientRole(event))
						moduleCopy(args, event, calendarData, settings);
					else
						Message.sendMessage(MessageManager.getMessage("Notification.Perm.CONTROL_ROLE", settings), event);
					break;
				case "edit":
					if (PermissionChecker.hasSufficientRole(event))
						moduleEdit(args, event, calendarData, settings);
					else
						Message.sendMessage(MessageManager.getMessage("Notification.Perm.CONTROL_ROLE", settings), event);
					break;
				case "restart":
					if (PermissionChecker.hasSufficientRole(event))
						moduleRestart(args, event, calendarData, settings);
					else
						Message.sendMessage(MessageManager.getMessage("Notification.Perm.CONTROL_ROLE", settings), event);
					break;
				case "cancel":
					if (PermissionChecker.hasSufficientRole(event))
						moduleCancel(event, settings);
					else
						Message.sendMessage(MessageManager.getMessage("Notification.Perm.CONTROL_ROLE", settings), event);
					break;
				case "delete":
					if (PermissionChecker.hasSufficientRole(event))
						moduleDelete(args, event, calendarData, settings);
					else
						Message.sendMessage(MessageManager.getMessage("Notification.Perm.CONTROL_ROLE", settings), event);
					break;
				case "view":
					moduleView(args, event, calendarData, settings);
					break;
				case "review":
					moduleView(args, event, calendarData, settings);
					break;
				case "confirm":
					if (PermissionChecker.hasSufficientRole(event))
						moduleConfirm(event, calendarData, settings);
					else
						Message.sendMessage(MessageManager.getMessage("Notification.Perm.CONTROL_ROLE", settings), event);
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
				case "location":
					moduleLocation(args, event, settings);
					break;
				case "loc":
					moduleLocation(args, event, settings);
					break;
				case "image":
					moduleAttachment(args, event, settings);
					break;
				case "attachment":
					moduleAttachment(args, event, settings);
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
				if (args.length == 1)
					e = EventCreator.getCreator().init(event, settings, true);
				else
					e = EventCreator.getCreator().init(event, settings, GeneralUtils.getContent(args, 1), true);

				if (e.getCreatorMessage() == null)
					Message.sendMessage(MessageManager.getMessage("Creator.Event.Create.Init", settings), event);
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
					if (EventUtils.eventExists(settings, eventId)) {
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
					if (EventUtils.eventExists(settings, eventId)) {
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

	private void moduleRestart(String[] args, MessageReceivedEvent event, CalendarData calendarData, GuildSettings settings) {
		long guildId = event.getGuild().getLongID();
		IMessage msg = null;
		boolean editing = false;
		if (EventCreator.getCreator().hasPreEvent(guildId))
			editing = EventCreator.getCreator().getPreEvent(guildId).isEditing();



		if (EventCreator.getCreator().hasCreatorMessage(guildId))
			msg = EventCreator.getCreator().getCreatorMessage(guildId);

		if (EventCreator.getCreator().terminate(event)) {
			if (msg != null) {
				Message.deleteMessage(msg);
				Message.deleteMessage(event);
			}
			if (!editing)
				moduleCreate(args, event, calendarData, settings);
			else
				moduleEdit(args, event, calendarData, settings);
		} else {
			Message.sendMessage(MessageManager.getMessage("Creator.Event.NotInit", settings), event);
		}
	}

	private void moduleDelete(String[] args, MessageReceivedEvent event, CalendarData calendarData, GuildSettings settings) {
		long guildId = event.getGuild().getLongID();
		if (args.length == 2) {
			if (!calendarData.getCalendarAddress().equalsIgnoreCase("primary")) {
				if (!EventCreator.getCreator().hasPreEvent(guildId)) {
					if (EventUtils.deleteEvent(settings, args[1])) {
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
						Calendar service = CalendarAuth.getCalendarService(settings);

						Event calEvent = service.events().get(calendarData.getCalendarAddress(), args[1]).execute();
						Message.sendMessage(EventMessageFormatter.getEventEmbed(calEvent, settings), event);
					} catch (Exception e) {
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
					EventCreatorResponse response = EventCreator.getCreator().confirmEvent(event, settings);
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
						if (!TimeUtils.inPast(dateRaw, tz) && !TimeUtils.startAfterEnd(dateRaw, tz, EventCreator.getCreator().getPreEvent(guildId))) {
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
								EventDateTime end = EventCreator.getCreator().getPreEvent(guildId).getStartDateTime().clone();
								long endLong = end.getDateTime().getValue() + 3600000; //Add an hour

								end.setDateTime(new DateTime(endLong));

								EventCreator.getCreator().getPreEvent(guildId).setEndDateTime(end);


								//Viewable date
								EventDateTime endV = EventCreator.getCreator().getPreEvent(guildId).getViewableStartDate().clone();
								long endVLong = endV.getDateTime().getValue() + 3600000; //Add an hour

								endV.setDateTime(new DateTime(endVLong));

								EventCreator.getCreator().getPreEvent(guildId).setViewableEndDate(endV);
							}

							if (EventCreator.getCreator().hasCreatorMessage(guildId)) {
								Message.deleteMessage(event);
								Message.deleteMessage(EventCreator.getCreator().getCreatorMessage(guildId));
								EventCreator.getCreator().setCreatorMessage(Message.sendMessage(EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(guildId), settings), MessageManager.getMessage("Creator.Event.Start.Success.New", settings), event));
							} else {
								String msg = MessageManager.getMessage("Creator.Event.Start.Success", settings);
								msg = msg.replaceAll("%date%", EventMessageFormatter.getHumanReadableDate(eventDateTimeV, settings, true)).replaceAll("%time%", EventMessageFormatter.getHumanReadableTime(eventDateTimeV, settings, true));
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
						if (!TimeUtils.inPast(dateRaw, tz) && !TimeUtils.endBeforeStart(dateRaw, tz, EventCreator.getCreator().getPreEvent(guildId))) {
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
								msg = msg.replaceAll("%date%", EventMessageFormatter.getHumanReadableDate(eventDateTimeV, settings, true)).replaceAll("%time%", EventMessageFormatter.getHumanReadableTime(eventDateTimeV, settings, true));
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
		if (args.length > 1) {
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
				EmbedBuilder em = new EmbedBuilder();
				em.withAuthorIcon(DisCalAPI.getAPI().iconUrl);
				em.withAuthorName("DisCal!");

				em.withTitle("Available Colors");
				em.withUrl("https://discalbot.com/docs/event/colors");
				em.withColor(56, 138, 237);
				em.withFooterText("Click Title for previews of the colors!");

				for (EventColor ec : EventColor.values()) {
					em.appendField(ec.name(), ec.getId() + "", true);
				}

				if (EventCreator.getCreator().hasCreatorMessage(guildId)) {
					Message.deleteMessage(event);
					Message.deleteMessage(EventCreator.getCreator().getCreatorMessage(guildId));
					EventCreator.getCreator().setCreatorMessage(Message.sendMessage(em.build(), "All Supported Colors. Use either the name or ID in the command: `!event color <name/id>`", event));
				} else {
					Message.sendMessage(em.build(), "All Supported Colors. Use either the name or ID in the command: `!event color <name/id>`", event);
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

	private void moduleLocation(String[] args, MessageReceivedEvent event, GuildSettings settings) {
		long guildId = event.getGuild().getLongID();
		if (args.length > 1) {
			if (EventCreator.getCreator().hasPreEvent(guildId)) {
				String content = GeneralUtils.getContent(args, 1);
				if (!content.equalsIgnoreCase("clear")) {
					EventCreator.getCreator().getPreEvent(guildId).setLocation(content);
					if (EventCreator.getCreator().hasCreatorMessage(guildId)) {
						Message.deleteMessage(event);
						Message.deleteMessage(EventCreator.getCreator().getCreatorMessage(guildId));
						EventCreator.getCreator().setCreatorMessage(Message.sendMessage(EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(guildId), settings), MessageManager.getMessage("Creator.Event.Location.Success.New", settings), event));
					} else {
						Message.sendMessage(MessageManager.getMessage("Creator.Event.Location.Success", "%location%", content, settings), event);
					}
				} else {
					EventCreator.getCreator().getPreEvent(guildId).setLocation(null);
					if (EventCreator.getCreator().hasCreatorMessage(guildId)) {
						Message.deleteMessage(event);
						Message.deleteMessage(EventCreator.getCreator().getCreatorMessage(guildId));
						EventCreator.getCreator().setCreatorMessage(Message.sendMessage(EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(guildId), settings), MessageManager.getMessage("Creator.Event.Location.Success.Clear", settings), event));
					} else {
						Message.sendMessage(MessageManager.getMessage("Creator.Event.Location.Success.Clear", settings), event);
					}
				}
			} else {
				Message.sendMessage(MessageManager.getMessage("Creator.Event.NotInit", settings), event);
			}
		} else {
			if (EventCreator.getCreator().hasCreatorMessage(guildId)) {
				Message.deleteMessage(event);
				Message.deleteMessage(EventCreator.getCreator().getCreatorMessage(guildId));
				EventCreator.getCreator().setCreatorMessage(Message.sendMessage(EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(guildId), settings), MessageManager.getMessage("Creator.Event.Location.Specify", settings), event));
			} else {
				Message.sendMessage(MessageManager.getMessage("Creator.Event.Location.Specify", settings), event);
			}
		}
	}

	private void moduleAttachment(String[] args, MessageReceivedEvent event, GuildSettings settings) {
		long guildId = event.getGuild().getLongID();
		if (args.length == 2) {
			String value = args[1];
			if (EventCreator.getCreator().hasPreEvent(guildId)) {
				if (value.equalsIgnoreCase("delete") || value.equalsIgnoreCase("remove") || value.equalsIgnoreCase("clear")) {
					//Delete picture from event
					EventCreator.getCreator().getPreEvent(guildId).getEventData().setImageLink(null);

					if (EventCreator.getCreator().hasCreatorMessage(guildId)) {
						Message.deleteMessage(event);
						Message.deleteMessage(EventCreator.getCreator().getCreatorMessage(guildId));
						EventCreator.getCreator().setCreatorMessage(Message.sendMessage(EventMessageFormatter.getPreEventEmbed(EventCreator.getCreator().getPreEvent(guildId), settings), MessageManager.getMessage("Creator.Event.Attachment.Delete", settings), event));
					} else {
						Message.sendMessage(MessageManager.getMessage("Creator.Event.Attachment.Delete", settings), event);
					}
				} else if (ImageUtils.validate(value)) {
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