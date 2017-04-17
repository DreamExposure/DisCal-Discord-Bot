package com.cloudcraftgaming.discal.internal.calendar.event;

import com.cloudcraftgaming.discal.Main;
import com.cloudcraftgaming.discal.database.DatabaseManager;
import com.cloudcraftgaming.discal.internal.calendar.CalendarAuth;
import com.cloudcraftgaming.discal.utils.ExceptionHandler;
import com.cloudcraftgaming.discal.utils.Message;
import com.cloudcraftgaming.discal.utils.PermissionChecker;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Nova Fox on 1/3/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class EventCreator {
    private static EventCreator instance;

    private ArrayList<PreEvent> events = new ArrayList<>();

    private EventCreator() {} //Prevent initialization.

    /**
     * Gets the instance of the EventCreator.
     * @return The instance of the EventCreator
     */
    public static EventCreator getCreator() {
        if (instance == null) {
            instance = new EventCreator();
        }
        return instance;
    }

    //Functionals
    /**
     * Initiates the EventCreator for a specific guild.
     * @param e The event received upon initialization.
     * @return The PreEvent for the guild.
     */
    public PreEvent init(MessageReceivedEvent e, boolean handleMessage) {
        if (!hasPreEvent(e.getMessage().getGuild().getID())) {
            PreEvent event = new PreEvent(e.getMessage().getGuild().getID());
            try {

                //TODO: Handle multiple calendars...
                String calId = DatabaseManager.getManager().getMainCalendar(e.getMessage().getGuild().getID()).getCalendarAddress();
                event.setTimeZone(CalendarAuth.getCalendarService().calendars().get(calId).execute().getTimeZone());
            } catch (IOException exc) {
                //Failed to get timezone, ignore safely.
            }
            if (handleMessage) {
            	if (PermissionChecker.botHasMessageManagePerms(e)) {
					IMessage message = Message.sendMessage(EventMessageFormatter.getPreEventEmbed(event), "Event Creator Initiated!", e, Main.client);
					event.setCreatorMessage(message);
				} else {
					Message.sendMessage("DisCal does not have \"MANAGE MESSAGES\" permissions in this channel! In order to use the the new creators DisCal must have these permissions! (HINT: Restart Creator after permissions are added!)", e, Main.client);
				}
			}

            events.add(event);
            return event;
        }
        return getPreEvent(e.getMessage().getGuild().getID());
    }

    public PreEvent init(MessageReceivedEvent e, String eventId, boolean handleMessage) {
        if (!hasPreEvent(e.getMessage().getGuild().getID())) {
            //TODO: Handle multiple calendars...
            try {
                String calId = DatabaseManager.getManager().getMainCalendar(e.getMessage().getGuild().getID()).getCalendarAddress();
                Calendar service = CalendarAuth.getCalendarService();
                Event calEvent = service.events().get(calId, eventId).execute();

                PreEvent event = EventUtils.copyEvent(e.getMessage().getGuild().getID(), calEvent);

                try {
                    event.setTimeZone(service.calendars().get(calId).execute().getTimeZone());
                } catch (IOException e1) {
                    //Failed to get tz, ignore safely.
                }

				if (handleMessage) {
                	if (PermissionChecker.botHasMessageManagePerms(e)) {
						IMessage message = Message.sendMessage(EventMessageFormatter.getPreEventEmbed(event), "Event copied! Please specify the date/times for the event!", e, Main.client);
						event.setCreatorMessage(message);
					} else {
						Message.sendMessage("DisCal does not have \"MANAGE MESSAGES\" permissions in this channel! In order to use the the new creators DisCal must have these permissions! (HINT: Restart Creator after permissions are added!)", e, Main.client);
					}
				}

                events.add(event);
                return event;
            } catch (IOException exc) {
               //Something failed...
            }
            return null;
        }
        return getPreEvent(e.getMessage().getGuild().getID());
    }

    public PreEvent edit(MessageReceivedEvent e, String eventId, boolean handleMessage) {
        String guildId = e.getMessage().getGuild().getID();
        if (!hasPreEvent(guildId)) {
            //TODO: Handle multiple calendars...
            try {
                String calId = DatabaseManager.getManager().getMainCalendar(guildId).getCalendarAddress();
                Calendar service = CalendarAuth.getCalendarService();
                Event calEvent = service.events().get(calId, eventId).execute();

                PreEvent event = new PreEvent(guildId, calEvent);
                event.setEditing(true);

                try {
                    event.setTimeZone(service.calendars().get(calId).execute().getTimeZone());
                } catch (IOException e1) {
                    //Failed to get tz, ignore safely.
                }

				if (handleMessage) {
                	if (PermissionChecker.botHasMessageManagePerms(e)) {
						IMessage message = Message.sendMessage(EventMessageFormatter.getPreEventEmbed(event), "Event Editor Initiated!", e, Main.client);
						event.setCreatorMessage(message);
					} else {
						Message.sendMessage("DisCal does not have \"MANAGE MESSAGES\" permissions in this channel! In order to use the the new creators DisCal must have these permissions! (HINT: Restart Editor after permissions are added!)", e, Main.client);
					}
				}

                events.add(event);
                return event;
            } catch (IOException exc) {
                //Oops
            }
            return null;
        }
        return getPreEvent(guildId);
    }

    /**
     * Gracefully terminates the EventCreator for a specific guild.
     * @param e The event received upon termination.
     * @return <code>true</code> if successful, else <code>false</code>.
     */
    public Boolean terminate(MessageReceivedEvent e) {
        if (hasPreEvent(e.getMessage().getGuild().getID())) {
            events.remove(getPreEvent(e.getMessage().getGuild().getID()));
            return true;
        }
        return false;
    }

    /**
     * Confirms the event in the creator for the specific guild.
     * @param e The event received upon confirmation.
     * @return The response containing detailed info about the confirmation.
     */
    public EventCreatorResponse confirmEvent(MessageReceivedEvent e) {
        if (hasPreEvent(e.getMessage().getGuild().getID())) {
            String guildId = e.getMessage().getGuild().getID();
            PreEvent preEvent = getPreEvent(guildId);
            if (preEvent.hasRequiredValues()) {
                Event event = new Event();
                event.setSummary(preEvent.getSummary());
                event.setDescription(preEvent.getDescription());
                event.setStart(preEvent.getStartDateTime().setTimeZone(preEvent.getTimeZone()));
                event.setEnd(preEvent.getEndDateTime().setTimeZone(preEvent.getTimeZone()));
                event.setVisibility("public");
                event.setColorId(String.valueOf(preEvent.getColor().getId()));

                //Set recurrence
                if (preEvent.shouldRecur()) {
                    String[] recurrence = new String[] {preEvent.getRecurrence().toRRule()};
                    event.setRecurrence(Arrays.asList(recurrence));
                }

                //TODO handle multiple calendars...
                String calendarId = DatabaseManager.getManager().getMainCalendar(guildId).getCalendarAddress();

                if (!preEvent.isEditing()) {
                    try {
                        Event confirmed = CalendarAuth.getCalendarService().events().insert(calendarId, event).execute();
                        terminate(e);
                        return new EventCreatorResponse(true, confirmed);
                    } catch (IOException ex) {
                        ExceptionHandler.sendException(e.getMessage().getAuthor(), "Failed to create event.", ex, this.getClass());
                        return new EventCreatorResponse(false);
                    }
                } else {
                    try {
                        Event confirmed = CalendarAuth.getCalendarService().events().update(calendarId, preEvent.getEventId(), event).execute();
                        terminate(e);
                        return new EventCreatorResponse(true, confirmed);
                    } catch (IOException ex) {
                        ExceptionHandler.sendException(e.getMessage().getAuthor(), "Failed to update event.", ex, this.getClass());
                        return new EventCreatorResponse(false);
                    }
                }
            }
        }
        return new EventCreatorResponse(false);
    }

    //Getters
    /**
     * gets the PreEvent for the specified guild.
     * @param guildId The ID of the guild.
     * @return The PreEvent belonging to the guild.
     */
    public PreEvent getPreEvent(String guildId) {
        for (PreEvent e : events) {
            if (e.getGuildId().equals(guildId)) {
                return e;
            }
        }
        return null;
    }

    //Booleans/Checkers
    /**
     * Checks if the specified guild has a PreEvent in the creator.
     * @param guildId The ID of the guild.
     * @return <code>true</code> if a PreEvent exists, otherwise <code>false</code>.
     */
    public Boolean hasPreEvent(String guildId) {
        for (PreEvent e : events) {
            if (e.getGuildId().equals(guildId)) {
                return true;
            }
        }
        return false;
    }
}