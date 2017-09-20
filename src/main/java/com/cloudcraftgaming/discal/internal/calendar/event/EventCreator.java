package com.cloudcraftgaming.discal.internal.calendar.event;

import com.cloudcraftgaming.discal.database.DatabaseManager;
import com.cloudcraftgaming.discal.internal.calendar.CalendarAuth;
import com.cloudcraftgaming.discal.internal.data.GuildSettings;
import com.cloudcraftgaming.discal.internal.service.AnnouncementQueueManager;
import com.cloudcraftgaming.discal.utils.*;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Nova Fox on 1/3/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
@SuppressWarnings("Duplicates")
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
    public PreEvent init(MessageReceivedEvent e, GuildSettings settings, boolean handleMessage) {
        if (!hasPreEvent(e.getGuild().getLongID())) {
            PreEvent event = new PreEvent(e.getGuild().getLongID());
            try {

                //TODO: Handle multiple calendars...
                String calId = DatabaseManager.getManager().getMainCalendar(e.getGuild().getLongID()).getCalendarAddress();
                event.setTimeZone(CalendarAuth.getCalendarService().calendars().get(calId).execute().getTimeZone());
            } catch (IOException exc) {
                //Failed to get timezone, ignore safely.
            }
            if (handleMessage) {
            	if (PermissionChecker.botHasMessageManagePerms(e)) {
					IMessage message = Message.sendMessage(EventMessageFormatter.getPreEventEmbed(event, settings), MessageManager.getMessage("Creator.Event.Create.Init", settings), e);
					event.setCreatorMessage(message);
					Message.deleteMessage(e);
				} else {
					Message.sendMessage(MessageManager.getMessage("Creator.Notif.MANAGE_MESSAGES", settings), e);
				}
			}

            events.add(event);
            return event;
        }
        return getPreEvent(e.getGuild().getLongID());
    }

    public PreEvent init(MessageReceivedEvent e, GuildSettings settings, String summary, boolean handleMessage) {
		if (!hasPreEvent(e.getGuild().getLongID())) {
			PreEvent event = new PreEvent(e.getGuild().getLongID());
			event.setSummary(summary);
			try {

				//TODO: Handle multiple calendars...
				String calId = DatabaseManager.getManager().getMainCalendar(e.getGuild().getLongID()).getCalendarAddress();
				if (!settings.useExternalCalendar()) {
					event.setTimeZone(CalendarAuth.getCalendarService().calendars().get(calId).execute().getTimeZone());
				} else {
					event.setTimeZone(CalendarAuth.getCalendarService(settings).calendars().get(calId).execute().getTimeZone());
				}
			} catch (Exception exc) {
				//Failed to get timezone, ignore safely.
			}
			if (handleMessage) {
				if (PermissionChecker.botHasMessageManagePerms(e)) {
					IMessage message = Message.sendMessage(EventMessageFormatter.getPreEventEmbed(event, settings), MessageManager.getMessage("Creator.Event.Create.Init", settings), e);
					event.setCreatorMessage(message);
					Message.deleteMessage(e);
				} else {
					Message.sendMessage(MessageManager.getMessage("Creator.Notif.MANAGE_MESSAGES", settings), e);
				}
			}

			events.add(event);
			return event;
		}
		return getPreEvent(e.getGuild().getLongID());
	}

    //Copy event
    public PreEvent init(MessageReceivedEvent e, String eventId, GuildSettings settings, boolean handleMessage) {
        if (!hasPreEvent(e.getGuild().getLongID())) {
            //TODO: Handle multiple calendars...
            try {
                String calId = DatabaseManager.getManager().getMainCalendar(e.getGuild().getLongID()).getCalendarAddress();
                Calendar service;
                if (settings.useExternalCalendar()) {
                	service = CalendarAuth.getCalendarService(settings);
				} else {
					service = CalendarAuth.getCalendarService();
				}
                Event calEvent = service.events().get(calId, eventId).execute();

                PreEvent event = EventUtils.copyEvent(e.getGuild().getLongID(), calEvent);

                try {
                    event.setTimeZone(service.calendars().get(calId).execute().getTimeZone());
                } catch (IOException e1) {
                    //Failed to get tz, ignore safely.
                }

				if (handleMessage) {
                	if (PermissionChecker.botHasMessageManagePerms(e)) {
						IMessage message = Message.sendMessage(EventMessageFormatter.getPreEventEmbed(event, settings), MessageManager.getMessage("Creator.Event.Copy.Init", settings), e);
						event.setCreatorMessage(message);
						Message.deleteMessage(e);
					} else {
						Message.sendMessage(MessageManager.getMessage("Creator.Notif.MANAGE_MESSAGES", settings), e);
					}
				}

                events.add(event);
                return event;
            } catch (Exception exc) {
               //Something failed...
            }
            return null;
        }
        return getPreEvent(e.getGuild().getLongID());
    }

    public PreEvent edit(MessageReceivedEvent e, String eventId, GuildSettings settings, boolean handleMessage) {
        long guildId = e.getGuild().getLongID();
        if (!hasPreEvent(guildId)) {
            //TODO: Handle multiple calendars...
            try {
                String calId = DatabaseManager.getManager().getMainCalendar(guildId).getCalendarAddress();
                Calendar service;
                if (settings.useExternalCalendar()) {
                	service = CalendarAuth.getCalendarService(settings);
				} else {
                	service = CalendarAuth.getCalendarService();
				}
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
						IMessage message = Message.sendMessage(EventMessageFormatter.getPreEventEmbed(event, settings), MessageManager.getMessage("Creator.Event.Edit.Init", settings), e);
						event.setCreatorMessage(message);
						Message.deleteMessage(e);
					} else {
						Message.sendMessage(MessageManager.getMessage("Creator.Notif.MANAGE_MESSAGES", settings), e);
					}
				}

                events.add(event);
                return event;
            } catch (Exception exc) {
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
        if (hasPreEvent(e.getGuild().getLongID())) {
            events.remove(getPreEvent(e.getGuild().getLongID()));
            return true;
        }
        return false;
    }

    /**
     * Confirms the event in the creator for the specific guild.
     * @param e The event received upon confirmation.
     * @return The response containing detailed info about the confirmation.
     */
    public EventCreatorResponse confirmEvent(MessageReceivedEvent e, GuildSettings settings) {
        if (hasPreEvent(e.getGuild().getLongID())) {
            long guildId = e.getGuild().getLongID();
            PreEvent preEvent = getPreEvent(guildId);
            if (preEvent.hasRequiredValues()) {
                Event event = new Event();
                event.setSummary(preEvent.getSummary());
                event.setDescription(preEvent.getDescription());
                event.setStart(preEvent.getStartDateTime().setTimeZone(preEvent.getTimeZone()));
                event.setEnd(preEvent.getEndDateTime().setTimeZone(preEvent.getTimeZone()));
                event.setVisibility("public");
                if (!preEvent.getColor().equals(EventColor.NONE)) {
					event.setColorId(String.valueOf(preEvent.getColor().getId()));
				}
				if (preEvent.getLocation() != null && !preEvent.getLocation().equalsIgnoreCase("")) {
					event.setLocation(preEvent.getLocation());
				}


                //Set recurrence
                if (preEvent.shouldRecur()) {
                    String[] recurrence = new String[] {preEvent.getRecurrence().toRRule()};
                    event.setRecurrence(Arrays.asList(recurrence));
                }

                //TODO handle multiple calendars...
                String calendarId = DatabaseManager.getManager().getMainCalendar(guildId).getCalendarAddress();

                if (!preEvent.isEditing()) {
                    try {
                    	Event confirmed;
                    	if (settings.useExternalCalendar()) {
                    		confirmed = CalendarAuth.getCalendarService(settings).events().insert(calendarId, event).execute();
						} else {
							confirmed = CalendarAuth.getCalendarService().events().insert(calendarId, event).execute();
						}
                        if (preEvent.getEventData().shouldBeSaved()) {
                        	preEvent.getEventData().setEventId(confirmed.getId());
                        	preEvent.getEventData().setEventEnd(confirmed.getEnd().getDateTime().getValue());
                        	DatabaseManager.getManager().updateEventData(preEvent.getEventData());
						}
                        terminate(e);
                        EventCreatorResponse response = new EventCreatorResponse(true, confirmed);
                        response.setEdited(false);
                        return response;
                    } catch (Exception ex) {
                        ExceptionHandler.sendException(e.getAuthor(), "Failed to create event.", ex, this.getClass());
                        EventCreatorResponse response = new EventCreatorResponse(false);
                        response.setEdited(false);
                        return response;
                    }
                } else {
                    try {
                    	Event confirmed;
                    	if (settings.useExternalCalendar()) {
                    		confirmed = CalendarAuth.getCalendarService(settings).events().update(calendarId, preEvent.getEventId(), event).execute();
						} else {
							confirmed = CalendarAuth.getCalendarService().events().update(calendarId, preEvent.getEventId(), event).execute();
						}
                        if (preEvent.getEventData().shouldBeSaved()) {
                        	preEvent.getEventData().setEventId(confirmed.getId());
                        	preEvent.getEventData().setEventEnd(confirmed.getEnd().getDateTime().getValue());
                        	DatabaseManager.getManager().updateEventData(preEvent.getEventData());
						}
                        terminate(e);
	                    AnnouncementQueueManager.getManager().update(confirmed);

						EventCreatorResponse response = new EventCreatorResponse(true, confirmed);
                        response.setEdited(true);
                        return response;
                    } catch (Exception ex) {
                        ExceptionHandler.sendException(e.getAuthor(), "Failed to update event.", ex, this.getClass());
                        EventCreatorResponse response = new EventCreatorResponse(false);
                        response.setEdited(true);
                        return response;
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
    public PreEvent getPreEvent(long guildId) {
        for (PreEvent e : events) {
            if (e.getGuildId() == guildId) {
                return e;
            }
        }
        return null;
    }

    public IMessage getCreatorMessage(long guildId) {
    	if (hasPreEvent(guildId)) {
    		return getPreEvent(guildId).getCreatorMessage();
		}
		return null;
	}

    //Booleans/Checkers
    /**
     * Checks if the specified guild has a PreEvent in the creator.
     * @param guildId The ID of the guild.
     * @return <code>true</code> if a PreEvent exists, otherwise <code>false</code>.
     */
    public Boolean hasPreEvent(long guildId) {
        for (PreEvent e : events) {
            if (e.getGuildId() == guildId) {
                return true;
            }
        }
        return false;
    }

    public boolean hasCreatorMessage(long guildId) {
    	return hasPreEvent(guildId) && getPreEvent(guildId).getCreatorMessage() != null;
	}

	//Setters
	public void setCreatorMessage(IMessage msg) {
		if (msg != null) {
			if (hasPreEvent(msg.getGuild().getLongID())) {
				getPreEvent(msg.getGuild().getLongID()).setCreatorMessage(msg);
			}
		}
	}
}