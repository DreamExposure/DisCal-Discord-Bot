package com.cloudcraftgaming.internal.calendar;

import sx.blah.discord.handle.impl.events.MessageReceivedEvent;

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

    public PreEvent init(MessageReceivedEvent e, String eventName) {
        if (!hasPreEvent(e.getMessage().getGuild().getID())) {
            PreEvent event = new PreEvent(e.getMessage().getGuild().getID(), eventName);
            events.add(event);
            return event;
        }
        return getPreEvent(e.getMessage().getGuild().getID());
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