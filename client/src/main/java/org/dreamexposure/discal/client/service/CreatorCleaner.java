package org.dreamexposure.discal.client.service;

import org.dreamexposure.discal.client.announcement.AnnouncementCreator;
import org.dreamexposure.discal.core.object.announcement.Announcement;

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

        final List<Announcement> ans = new ArrayList<>();

        //Run through announcement creator
        for (final Announcement an : AnnouncementCreator.getCreator().getAllAnnouncements()) {
            final long difference = System.currentTimeMillis() - an.getLastEdit();

            if (difference <= target) {
                //Last edited 60+ minutes ago, delete from creator and free up RAM.
                ans.add(an);
            }
        }

        for (final Announcement a : ans) {
            AnnouncementCreator.getCreator().terminate(a.getGuildId());
        }
    }
}
