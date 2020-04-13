package org.dreamexposure.discal.client.service;

import org.dreamexposure.discal.client.announcement.AnnouncementCreator;
import org.dreamexposure.discal.client.calendar.CalendarCreator;
import org.dreamexposure.discal.client.event.EventCreator;
import org.dreamexposure.discal.core.logger.LogFeed;
import org.dreamexposure.discal.core.logger.object.LogObject;
import org.dreamexposure.discal.core.object.announcement.Announcement;
import org.dreamexposure.discal.core.object.calendar.PreCalendar;
import org.dreamexposure.discal.core.object.event.PreEvent;

import java.util.ArrayList;
import java.util.TimerTask;

/**
 * Created by Nova Fox on 11/2/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
public class CreatorCleaner extends TimerTask {

    @Override
    public void run() {
        try {
            long target = 60 * 1000 * 60; //60 minutes

            ArrayList<PreCalendar> cals = new ArrayList<>();
            ArrayList<PreEvent> events = new ArrayList<>();
            ArrayList<Announcement> ans = new ArrayList<>();

            //Run through calendar creator
            for (PreCalendar cal : CalendarCreator.getCreator().getAllPreCalendars()) {
                long difference = System.currentTimeMillis() - cal.getLastEdit();

                if (difference <= target) {
                    //Last edited 60+ minutes ago, delete from creator and free up RAM.
                    cals.add(cal);
                }
            }

            //Run through event creator
            for (PreEvent event : EventCreator.getCreator().getAllPreEvents()) {
                long difference = System.currentTimeMillis() - event.getLastEdit();

                if (difference <= target) {
                    //Last edited 60+ minutes ago, delete from creator and free up RAM.
                    events.add(event);
                }
            }

            //Run through announcement creator
            for (Announcement an : AnnouncementCreator.getCreator().getAllAnnouncements()) {
                long difference = System.currentTimeMillis() - an.getLastEdit();

                if (difference <= target) {
                    //Last edited 60+ minutes ago, delete from creator and free up RAM.
                    ans.add(an);
                }
            }

            //Okay, actually go through it all and delete
            for (PreCalendar c : cals) {
                CalendarCreator.getCreator().terminate(c.getGuildId());
            }
            for (PreEvent e : events) {
                EventCreator.getCreator().terminate(e.getGuildId());
            }
            for (Announcement a : ans) {
                AnnouncementCreator.getCreator().terminate(a.getGuildId());
            }
        } catch (Exception e) {
            LogFeed.log(LogObject.forException("Error in cleaner", e, this.getClass()));
        }
    }
}