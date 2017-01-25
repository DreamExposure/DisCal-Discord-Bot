package com.cloudcraftgaming.internal.calendar.calendar;

import com.cloudcraftgaming.database.DatabaseManager;
import com.cloudcraftgaming.internal.calendar.CalendarAuth;
import com.cloudcraftgaming.internal.data.BotData;
import com.google.api.services.calendar.model.AclRule;
import com.google.api.services.calendar.model.Calendar;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;

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

    private CalendarCreator() {}

    public static CalendarCreator getCreator() {
        if (instance == null) {
            instance = new CalendarCreator();
        }
        return instance;
    }

    //Functionals
    public PreCalendar init(MessageReceivedEvent e, String calendarName) {
        if (!hasPreCalendar(e.getMessage().getGuild().getID())) {
            PreCalendar event = new PreCalendar(e.getMessage().getGuild().getID(), calendarName);
            calendars.add(event);
            return event;
        }
        return getPreCalendar(e.getMessage().getGuild().getID());
    }

    public Boolean terminate(MessageReceivedEvent e) {
        if (hasPreCalendar(e.getMessage().getGuild().getID())) {
            calendars.remove(getPreCalendar(e.getMessage().getGuild().getID()));
            return true;
        }
        return false;
    }

    public CalendarCreatorResponse confirmCalendar(MessageReceivedEvent e) {
        if (hasPreCalendar(e.getMessage().getGuild().getID())) {
            String guildId = e.getMessage().getGuild().getID();
            PreCalendar preCalendar = getPreCalendar(guildId);
            if (preCalendar.hasRequiredValues()) {
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
                    BotData bd = new BotData(guildId);
                    bd.setCalendarId(confirmed.getId());
                    bd.setCalendarAddress(confirmed.getId());
                    DatabaseManager.getManager().updateData(bd);
                    terminate(e);
                    return new CalendarCreatorResponse(true, confirmed);
                } catch (IOException ex) {
                    return new CalendarCreatorResponse(false);
                }
            }
        }
        return new CalendarCreatorResponse(false);
    }

    //Getters
    public PreCalendar getPreCalendar(String guildId) {
        for (PreCalendar c : calendars) {
            if (c.getGuildId().equals(guildId)) {
                return c;
            }
        }
        return null;
    }

    //Booleans/Checkers
    public Boolean hasPreCalendar(String guildId) {
        for (PreCalendar c : calendars) {
            if (c.getGuildId().equals(guildId)) {
                return true;
            }
        }
        return false;
    }
}