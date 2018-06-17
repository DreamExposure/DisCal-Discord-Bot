package com.cloudcraftgaming.discal.bot.module.command;

import com.cloudcraftgaming.discal.api.calendar.CalendarAuth;
import com.cloudcraftgaming.discal.api.database.DatabaseManager;
import com.cloudcraftgaming.discal.api.message.MessageManager;
import com.cloudcraftgaming.discal.api.object.GuildSettings;
import com.cloudcraftgaming.discal.api.object.calendar.CalendarData;
import com.cloudcraftgaming.discal.api.object.command.CommandInfo;
import com.cloudcraftgaming.discal.bot.internal.calendar.event.EventMessageFormatter;
import com.cloudcraftgaming.discal.logger.Logger;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nova Fox on 1/3/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class EventListCommand implements ICommand {
    /**
     * Gets the command this Object is responsible for.
     * @return The command this Object is responsible for.
     */
    @Override
    public String getCommand() {
        return "events";
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
        CommandInfo info = new CommandInfo("events");
        info.setDescription("Lists the specified amount of events from the guild calendar.");
        info.setExample("!events (number or function) (other args if applicable)");

        info.getSubCommands().put("search", "Searches for events based on specific criteria rather than just the next upcoming events");
        return info;
    }

    /**
     * Issues the command this Object is responsible for.
     * @param args The command arguments.
     * @param event The event received.
     * @return <code>true</code> if successful, else <code>false</code>.
     */
    @Override
	public boolean issueCommand(String[] args, MessageReceivedEvent event, GuildSettings settings) {
        //Get events from calendar
        if (args.length < 1) {
            moduleSimpleList(args, event, settings);
        } else {
            switch (args[0].toLowerCase()) {
                case "search":
					if (settings.isDevGuild())
						moduleSearch(args, event, settings);
					else
						MessageManager.sendMessage(MessageManager.getMessage("Notification.Disabled", settings), event);
                    break;
				case "today":
					if (settings.isDevGuild())
						moduleDay(args, event, settings);
					else
						MessageManager.sendMessage(MessageManager.getMessage("Notification.Disabled", settings), event);
					break;
                default:
                    moduleSimpleList(args, event, settings);
                    break;
            }
        }
        return false;
    }

	@SuppressWarnings("Duplicates")
	private void moduleSimpleList(String[] args, MessageReceivedEvent event, GuildSettings settings) {
    	if (args.length == 0) {
			try {
				Calendar service = CalendarAuth.getCalendarService(settings);

				DateTime now = new DateTime(System.currentTimeMillis());
				CalendarData calendarData = DatabaseManager.getManager().getMainCalendar(event.getGuild().getLongID());
				Events events = service.events().list(calendarData.getCalendarAddress())
						.setMaxResults(1)
						.setTimeMin(now)
						.setOrderBy("startTime")
						.setSingleEvents(true)
						.setShowDeleted(false)
						.execute();
				List<Event> items = events.getItems();
				if (items.size() == 0) {
					MessageManager.sendMessage(MessageManager.getMessage("Event.List.Found.None", settings), event);
				} else if (items.size() == 1) {
					MessageManager.sendMessage(EventMessageFormatter.getEventEmbed(items.get(0), settings), MessageManager.getMessage("Event.List.Found.One", settings), event);
				}
			} catch (Exception e) {
				MessageManager.sendMessage(MessageManager.getMessage("Notification.Error.Unknown", settings), event);
				Logger.getLogger().exception(event.getAuthor(), "Failed to list events.", e, this.getClass(), true);
				e.printStackTrace();
			}
		} else if (args.length == 1) {
            try {
                Integer eventNum = Integer.valueOf(args[0]);
                if (eventNum > 15) {
					MessageManager.sendMessage(MessageManager.getMessage("Event.List.Amount.Over", settings), event);
                    return;
                }
                if (eventNum < 1) {
					MessageManager.sendMessage(MessageManager.getMessage("Event.List.Amount.Under", settings), event);
                    return;
                }
                try {
					Calendar service = CalendarAuth.getCalendarService(settings);

                    DateTime now = new DateTime(System.currentTimeMillis());
                    CalendarData calendarData = DatabaseManager.getManager().getMainCalendar(event.getGuild().getLongID());
                    Events events = service.events().list(calendarData.getCalendarAddress())
                            .setMaxResults(eventNum)
                            .setTimeMin(now)
                            .setOrderBy("startTime")
                            .setSingleEvents(true)
                            .execute();
                    List<Event> items = events.getItems();
                    if (items.size() == 0) {
						MessageManager.sendMessage(MessageManager.getMessage("Event.List.Found.None", settings), event);
                    } else if (items.size() == 1) {
						MessageManager.sendMessage(EventMessageFormatter.getEventEmbed(items.get(0), settings), MessageManager.getMessage("Event.List.Found.One", settings), event);
                    } else {
                        //List events by Id only.
						MessageManager.sendMessage(MessageManager.getMessage("Event.List.Found.Many", "%amount%", items.size() + "", settings), event);
                        for (Event e : items) {
							MessageManager.sendMessage(EventMessageFormatter.getCondensedEventEmbed(e, settings), event);
                        }
                    }
                } catch (Exception e) {
					MessageManager.sendMessage(MessageManager.getMessage("Notification.Error.Unknown", settings), event);
					Logger.getLogger().exception(event.getAuthor(), "Failed to list events.", e, this.getClass(), true);
                    e.printStackTrace();
                }
            } catch (NumberFormatException e) {
				MessageManager.sendMessage(MessageManager.getMessage("Notification.Args.Value.Integer", settings), event);
            }
        } else {
			MessageManager.sendMessage(MessageManager.getMessage("Event.List.Args.Many", settings), event);
        }
    }

    private void moduleSearch(String[] args, MessageReceivedEvent event, GuildSettings settings) {

	}

	@SuppressWarnings("Duplicates")
	private void moduleDay(String[] args, MessageReceivedEvent event, GuildSettings settings) {
		if (args.length == 1) {
			//Get the upcoming events in the next 24 hours.
			try {
				Calendar service = CalendarAuth.getCalendarService(settings);

				DateTime now = new DateTime(System.currentTimeMillis());
				DateTime twentyFourHoursFromNow = new DateTime(now.getValue() + 86400000L);
				CalendarData calendarData = DatabaseManager.getManager().getMainCalendar(event.getGuild().getLongID());
				Events events = service.events().list(calendarData.getCalendarAddress())
						.setMaxResults(20)
						.setTimeMin(now)
						.setTimeMax(twentyFourHoursFromNow)
						.setOrderBy("startTime")
						.setSingleEvents(true)
						.setShowDeleted(false)
						.execute();
				List<Event> items = events.getItems();
				if (items.size() == 0) {
					MessageManager.sendMessage(MessageManager.getMessage("Event.List.Found.None", settings), event);
				} else if (items.size() == 1) {
					MessageManager.sendMessage(EventMessageFormatter.getEventEmbed(items.get(0), settings), MessageManager.getMessage("Event.List.Found.One", settings), event);
				} else {
					//List events by Id only.
					MessageManager.sendMessage(MessageManager.getMessage("Event.List.Found.Many", "%amount%", items.size() + "", settings), event);
					for (Event e : items) {
						MessageManager.sendMessage(EventMessageFormatter.getCondensedEventEmbed(e, settings), event);
					}
				}
			} catch (Exception e) {
				MessageManager.sendMessage(MessageManager.getMessage("Notification.Error.Unknown", settings), event);
				Logger.getLogger().exception(event.getAuthor(), "Failed to list events.", e, this.getClass(), true);
				e.printStackTrace();
			}
		}
	}
}