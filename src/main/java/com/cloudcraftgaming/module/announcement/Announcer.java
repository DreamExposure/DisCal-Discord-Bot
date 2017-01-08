package com.cloudcraftgaming.module.announcement;

import com.cloudcraftgaming.Main;
import com.cloudcraftgaming.database.DatabaseManager;
import sx.blah.discord.handle.obj.IGuild;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by Nova Fox on 1/5/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class Announcer {
    private static Announcer instance;

    private final ArrayList<Announcement> announcements = new ArrayList<>();

    private Announcer() {}

    public static Announcer getAnnouncer() {
        if (instance == null) {
            instance = new Announcer();
        }
        return instance;
    }

    public void init() {
        //This method will load all existing announcements from the database once supported.
        for (IGuild guild : Main.client.getGuilds()) {
            String guildId = guild.getID();
            for (Announcement a : DatabaseManager.getManager().getAnnouncements(guildId)) {
                addAnnouncement(a);
            }
        }
    }

    public void shutdown() {
        announcements.clear();
    }

    //Getters
    public Announcement getAnnouncement(UUID announcementId) {
        for (Announcement a : announcements) {
            if (a.getAnnouncementId().equals(announcementId)) {
                return a;
            }
        }
        return null;
    }

    public ArrayList<Announcement> getAnnouncements(String guildId) {
        ArrayList<Announcement> _announcements = new ArrayList<>();
        for (Announcement a : announcements) {
            if (a.getGuildId().equals(guildId)) {
                _announcements.add(a);
            }
        }
        return _announcements;
    }

    //Setters
    public void addAnnouncement(Announcement announcement) {
        announcements.add(announcement);
    }
}