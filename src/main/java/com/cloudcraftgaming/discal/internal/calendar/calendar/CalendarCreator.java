package com.cloudcraftgaming.discal.internal.calendar.calendar;

import com.cloudcraftgaming.discal.database.DatabaseManager;
import com.cloudcraftgaming.discal.internal.calendar.CalendarAuth;
import com.cloudcraftgaming.discal.internal.data.CalendarData;
import com.cloudcraftgaming.discal.utils.ExceptionHandler;
import com.cloudcraftgaming.discal.utils.Message;
import com.cloudcraftgaming.discal.utils.MessageManager;
import com.cloudcraftgaming.discal.utils.PermissionChecker;
import com.google.api.services.calendar.model.AclRule;
import com.google.api.services.calendar.model.Calendar;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IMessage;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Nova Fox on 1/4/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class CalendarCreator {
    private static CalendarCreator instance;

    private ArrayList<PreCalendar> calendars = new ArrayList<>();

    private CalendarCreator() {} //Prevent initialization

    /**
     * Gets the instance of the CalendarCreator.
     * @return The instance of the CalendarCreator.
     */
    public static CalendarCreator getCreator() {
        if (instance == null) {
            instance = new CalendarCreator();
        }
        return instance;
    }

    //Functionals
    /**
     * Initiates the CalendarCreator for the guild involved in the event.
     * @param e The event received upon creation start.
     * @param calendarName The name of the calendar to create.
     * @return The PreCalendar object created.
     */
    public PreCalendar init(MessageReceivedEvent e, String calendarName, boolean handleCreatorMessage) {
        if (!hasPreCalendar(e.getMessage().getGuild().getID())) {
            PreCalendar calendar = new PreCalendar(e.getMessage().getGuild().getID(), calendarName);

            if (handleCreatorMessage) {
            	if (PermissionChecker.botHasMessageManagePerms(e)) {
					IMessage msg = Message.sendMessage(CalendarMessageFormatter.getPreCalendarEmbed(calendar), "Calendar Creator initialized! Please specify the description with `!calendar description <desc, spaces allowed>`", e);
					calendar.setCreatorMessage(msg);
				} else {
            		Message.sendMessage(MessageManager.getMessage("Creator.Notif.MANAGE_MESSAGES", e), e);
				}
            }
            calendars.add(calendar);
            return calendar;
        }
        return getPreCalendar(e.getMessage().getGuild().getID());
    }

    public PreCalendar edit(MessageReceivedEvent event, boolean handleCreatorMessage) {
        String guildId = event.getMessage().getGuild().getID();
        if (!hasPreCalendar(guildId)) {
            //TODO: Support multiple calendars
            CalendarData data = DatabaseManager.getManager().getMainCalendar(guildId);

            try {
                com.google.api.services.calendar.Calendar service = CalendarAuth.getCalendarService();

                Calendar calendar = service.calendars().get(data.getCalendarAddress()).execute();

                PreCalendar preCalendar = new PreCalendar(guildId, calendar);
                preCalendar.setEditing(true);
                preCalendar.setCalendarId(data.getCalendarAddress());

                if (handleCreatorMessage) {
					if (PermissionChecker.botHasMessageManagePerms(event)) {
						IMessage msg = Message.sendMessage(CalendarMessageFormatter.getPreCalendarEmbed(preCalendar), "Calendar Editor initialized!", event);
						preCalendar.setCreatorMessage(msg);
					} else {
						Message.sendMessage(MessageManager.getMessage("Creator.Notif.MANAGE_MESSAGES", event), event);
					}
				}

                calendars.add(preCalendar);
                return preCalendar;
            } catch (IOException e) {
                ExceptionHandler.sendException(event.getMessage().getAuthor(), "Failed to init calendar editor", e, this.getClass());
                return null;
            }
        } else {
            return getPreCalendar(guildId);
        }
    }

    /**
     * Gracefully closes down the CalendarCreator for the guild involved and DOES NOT create the calendar.
     * @param e The event received upon termination.
     * @return <codfe>true</codfe> if closed successfully, otherwise <code>false</code>.
     */
    public Boolean terminate(MessageReceivedEvent e) {
        if (hasPreCalendar(e.getMessage().getGuild().getID())) {
            calendars.remove(getPreCalendar(e.getMessage().getGuild().getID()));
            return true;
        }
        return false;
    }

    /**
     * Confirms the calendar and creates it within Google Calendar.
     * @param e The event received upon confirmation.
     * @return A CalendarCreatorResponse Object with detailed info about the confirmation.
     */
    public CalendarCreatorResponse confirmCalendar(MessageReceivedEvent e) {
        if (hasPreCalendar(e.getMessage().getGuild().getID())) {
            String guildId = e.getMessage().getGuild().getID();
            PreCalendar preCalendar = getPreCalendar(guildId);
            if (preCalendar.hasRequiredValues()) {
                if (!preCalendar.isEditing()) {
                    Calendar calendar = new Calendar();
                    calendar.setSummary(preCalendar.getSummary());
                    calendar.setDescription(preCalendar.getDescription());
                    calendar.setTimeZone(preCalendar.getTimezone());
                    try {
                        Calendar confirmed = CalendarAuth.getCalendarService().calendars().insert(calendar).execute();
                        AclRule rule = new AclRule();
                        AclRule.Scope scope = new AclRule.Scope();
                        scope.setType("default");
                        rule.setScope(scope).setRole("reader");
                        CalendarAuth.getCalendarService().acl().insert(confirmed.getId(), rule).execute();
                        CalendarData calendarData = new CalendarData(guildId, 1);
                        calendarData.setCalendarId(confirmed.getId());
                        calendarData.setCalendarAddress(confirmed.getId());
                        DatabaseManager.getManager().updateCalendar(calendarData);
                        terminate(e);
                        CalendarCreatorResponse response = new CalendarCreatorResponse(true, confirmed);
                        response.setEdited(false);
                        response.setCreatorMessage(preCalendar.getCreatorMessage());
                        return response;
                    } catch (IOException ex) {
                        ExceptionHandler.sendException(e.getMessage().getAuthor(), "Failed to confirm calendar.", ex, this.getClass());
                        CalendarCreatorResponse response = new CalendarCreatorResponse(false);
                        response.setEdited(false);
                        response.setCreatorMessage(preCalendar.getCreatorMessage());
                        return response;
                    }
                } else {
                    //Editing calendar...
                    Calendar calendar = new Calendar();
                    calendar.setSummary(preCalendar.getSummary());
                    calendar.setDescription(preCalendar.getDescription());
                    calendar.setTimeZone(preCalendar.getTimezone());

                    try {
                        Calendar confirmed = CalendarAuth.getCalendarService().calendars().update(preCalendar.getCalendarId(), calendar).execute();
                        AclRule rule = new AclRule();
                        AclRule.Scope scope = new AclRule.Scope();
                        scope.setType("default");
                        rule.setScope(scope).setRole("reader");
                        CalendarAuth.getCalendarService().acl().insert(confirmed.getId(), rule).execute();
                        terminate(e);
                        CalendarCreatorResponse response = new CalendarCreatorResponse(true, confirmed);
                        response.setEdited(true);
                        response.setCreatorMessage(preCalendar.getCreatorMessage());
                        return response;
                    } catch (IOException ex) {
                        ExceptionHandler.sendException(e.getMessage().getAuthor(), "Failed to update calendar.", ex, this.getClass());
                        CalendarCreatorResponse response = new CalendarCreatorResponse(false);
                        response.setEdited(true);
                        response.setCreatorMessage(preCalendar.getCreatorMessage());
                        return response;
                    }
                }
            }
        }
        return new CalendarCreatorResponse(false);
    }

    //Getters
    /**
     * Gets the PreCalendar for the guild in the creator.
     * @param guildId The ID of the guild whose PreCalendar is to be returned.
     * @return The PreCalendar belonging to the guild.
     */
    public PreCalendar getPreCalendar(String guildId) {
        for (PreCalendar c : calendars) {
            if (c.getGuildId().equals(guildId)) {
                return c;
            }
        }
        return null;
    }

    //Booleans/Checkers
    /**
     * Checks whether or not the specified Guild has a PreCalendar in the creator.
     * @param guildId The ID of the guild to check for.
     * @return <code>true</code> if a PreCalendar exists, else <code>false</code>.
     */
    public Boolean hasPreCalendar(String guildId) {
        for (PreCalendar c : calendars) {
            if (c.getGuildId().equals(guildId)) {
                return true;
            }
        }
        return false;
    }
}