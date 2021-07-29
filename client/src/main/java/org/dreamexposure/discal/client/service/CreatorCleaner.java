package org.dreamexposure.discal.client.service;

import org.dreamexposure.discal.client.announcement.AnnouncementCreator;
import org.dreamexposure.discal.client.calendar.CalendarCreator;
import org.dreamexposure.discal.client.event.EventCreator;
import org.dreamexposure.discal.core.object.announcement.Announcement;
import org.dreamexposure.discal.core.object.calendar.PreCalendar;
import org.dreamexposure.discal.core.object.event.PreEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

/**
 * Created by Nova Fox on 11/2/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
public class CreatorCleaner extends TimerTask {
    @Override
    public void run() {
        final long target = 60 * 1000 * 60; //60 minutes

        final List<PreCalendar> cals = new ArrayList<>();
        final List<PreEvent> events = new ArrayList<>();
        final List<Announcement> ans = new ArrayList<>();

        //Run through calendar creator
        for (final PreCalendar cal : CalendarCreator.getCreator().getAllPreCalendars()) {
            final long difference = System.currentTimeMillis() - cal.getLastEdit();

            if (difference <= target) {
                //Last edited 60+ minutes ago, delete from creator and free up RAM.
                cals.add(cal);
            }
        }

        //Run through event creator
        for (final PreEvent event : EventCreator.getCreator().getAllPreEvents()) {
            final long difference = System.currentTimeMillis() - event.getLastEdit();

            if (difference <= target) {
                //Last edited 60+ minutes ago, delete from creator and free up RAM.
                events.add(event);
            }
        }

        //Run through announcement creator
        for (final Announcement an : AnnouncementCreator.getCreator().getAllAnnouncements()) {
            final long difference = System.currentTimeMillis() - an.getLastEdit();

            if (difference <= target) {
                //Last edited 60+ minutes ago, delete from creator and free up RAM.
                ans.add(an);
            }
        }

        //Okay, actually go through it all and delete
        for (final PreCalendar c : cals) {
            CalendarCreator.getCreator().terminate(c.getGuildId());
        }
        for (final PreEvent e : events) {
            EventCreator.getCreator().terminate(e.getGuildId());
        }
        for (final Announcement a : ans) {
            AnnouncementCreator.getCreator().terminate(a.getGuildId());
        }
    }
}
