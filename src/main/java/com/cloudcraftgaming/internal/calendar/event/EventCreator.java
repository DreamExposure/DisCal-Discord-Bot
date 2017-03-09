package com.cloudcraftgaming.internal.calendar.event;

import com.cloudcraftgaming.database.DatabaseManager;
import com.cloudcraftgaming.internal.calendar.CalendarAuth;
import com.cloudcraftgaming.internal.email.EmailSender;
import com.google.api.services.calendar.model.Event;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Nova Fox on 1/3/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class EventCreator {
    private static EventCreator instance;

    private ArrayList<PreEvent> events = new ArrayList<>();

    private EventCreator() {}

    public static EventCreator getCreator() {
        if (instance == null) {
            instance = new EventCreator();
        }
        return instance;
    }

    //Functionals
    public PreEvent init(MessageReceivedEvent e) {
        if (!hasPreEvent(e.getMessage().getGuild().getID())) {
            PreEvent event = new PreEvent(e.getMessage().getGuild().getID());
            try {
                String calId = DatabaseManager.getManager().getData(e.getMessage().getGuild().getID()).getCalendarAddress();
                event.setTimeZone(CalendarAuth.getCalendarService().calendars().get(calId).execute().getTimeZone());
            } catch (IOException exc) {
                //Failed to get timezone, ignore safely.
            }
            events.add(event);
            return event;
        }
        return getPreEvent(e.getMessage().getGuild().getID());
    }

    public Boolean terminate(MessageReceivedEvent e) {
        if (hasPreEvent(e.getMessage().getGuild().getID())) {
            events.remove(getPreEvent(e.getMessage().getGuild().getID()));
            return true;
        }
        return false;
    }

    public EventCreatorResponse confirmEvent(MessageReceivedEvent e) {
        if (hasPreEvent(e.getMessage().getGuild().getID())) {
            String guildId = e.getMessage().getGuild().getID();
            PreEvent preEvent = getPreEvent(guildId);
            if (preEvent.hasRequiredValues()) {
                Event event = new Event();
                event.setSummary(preEvent.getSummary());
                event.setDescription(preEvent.getDescription());
                /* NOT NEEDED?
                if (!preEvent.getTimeZone().equalsIgnoreCase("unknown")) {
                    preEvent.getStartDateTime().setTimeZone(preEvent.getTimeZone());
                    preEvent.getEndDateTime().setTimeZone(preEvent.getTimeZone());
                }
                */
                event.setStart(preEvent.getStartDateTime());
                event.setEnd(preEvent.getEndDateTime());
                event.setVisibility("public");

                String calendarId = DatabaseManager.getManager().getData(guildId).getCalendarAddress();
                try {
                   Event confirmed = CalendarAuth.getCalendarService().events().insert(calendarId, event).execute();
                    terminate(e);
                    return new EventCreatorResponse(true, confirmed);
                } catch (IOException ex) {
                    EmailSender.getSender().sendExceptionEmail(ex);
                    return new EventCreatorResponse(false);
                }
            }
        }
        return new EventCreatorResponse(false);
    }

    //Getters
    public PreEvent getPreEvent(String guildId) {
        for (PreEvent e : events) {
            if (e.getGuildId().equals(guildId)) {
                return e;
            }
        }
        return null;
    }

    //Booleans/Checkers
    public Boolean hasPreEvent(String guildId) {
        for (PreEvent e : events) {
            if (e.getGuildId().equals(guildId)) {
                return true;
            }
        }
        return false;
    }
}