package com.cloudcraftgaming.discal.module.announcement;

import com.cloudcraftgaming.discal.database.DatabaseManager;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;

import java.util.ArrayList;

/**
 * Created by Nova Fox on 3/4/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class AnnouncementCreator {
    private static AnnouncementCreator instance;

    private ArrayList<Announcement> announcements = new ArrayList<>();

    private AnnouncementCreator() {} //Prevent initialization

    public static AnnouncementCreator getCreator() {
        if (instance == null) {
            instance = new AnnouncementCreator();
        }
        return instance;
    }

    //Functionals
    public Announcement init(MessageReceivedEvent e) {
        if (!hasAnnouncement(e.getMessage().getGuild().getID())) {
            Announcement a = new Announcement(e.getMessage().getGuild().getID());
            announcements.add(a);
            return a;
        }
        return getAnnouncement(e.getMessage().getGuild().getID());
    }

    public Boolean terminate(MessageReceivedEvent e) {
        if (hasAnnouncement(e.getMessage().getGuild().getID())) {
            announcements.remove(getAnnouncement(e.getMessage().getGuild().getID()));
            return true;
        }
        return false;
    }

    public AnnouncementCreatorResponse confirmEvent(MessageReceivedEvent e) {
        if (hasAnnouncement(e.getMessage().getGuild().getID())) {
            String guildId = e.getMessage().getGuild().getID();
            Announcement a = getAnnouncement(guildId);
            if (a.hasRequiredValues()) {
                DatabaseManager.getManager().updateAnnouncement(a);
                terminate(e);
                return new AnnouncementCreatorResponse(true, a);
            }
        }
        return new AnnouncementCreatorResponse(false);
    }

    //Getters
    public Announcement getAnnouncement(String guildId) {
        for (Announcement a : announcements) {
            if (a.getGuildId().equals(guildId)) {
                return a;
            }
        }
        return null;
    }

    //Booleans/Checkers
    public Boolean hasAnnouncement(String guildId) {
        for (Announcement a : announcements) {
            if (a.getGuildId().equals(guildId)) {
                return true;
            }
        }
        return false;
    }
}