package com.cloudcraftgaming.discal.module.announcement;

import com.cloudcraftgaming.discal.database.DatabaseManager;
import com.cloudcraftgaming.discal.utils.AnnouncementUtils;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by Nova Fox on 3/4/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class AnnouncementCreator {
    private static AnnouncementCreator instance;

    private ArrayList<Announcement> announcements = new ArrayList<>();

    private AnnouncementCreator() {} //Prevent initialization

    /**
     * Gets the instance of the AnnouncementCreator.
     * @return The instance of the AnnouncementCreator.
     */
    public static AnnouncementCreator getCreator() {
        if (instance == null) {
            instance = new AnnouncementCreator();
        }
        return instance;
    }

    //Functionals
    /**
     * Initiates the creator for the guild involved.
     * @param e The event received upon init.
     * @return A new Announcement.
     */
    public Announcement init(MessageReceivedEvent e) {
        if (!hasAnnouncement(e.getMessage().getGuild().getID())) {
            Announcement a = new Announcement(e.getMessage().getGuild().getID());
            a.setAnnouncementChannelId(e.getMessage().getChannel().getID());
            announcements.add(a);
            return a;
        }
        return getAnnouncement(e.getMessage().getGuild().getID());
    }

    public Announcement init(MessageReceivedEvent e, String announcementId) {
        if (!hasAnnouncement(e.getMessage().getGuild().getID()) && AnnouncementUtils.announcementExists(announcementId, e)) {
            Announcement toCopy = DatabaseManager.getManager().getAnnouncement(UUID.fromString(announcementId), e.getMessage().getGuild().getID());

            //Copy
            Announcement a = new Announcement(toCopy);

            announcements.add(a);
            return a;
        }
        return getAnnouncement(e.getMessage().getGuild().getID());
    }

    public Announcement edit(MessageReceivedEvent e, String announcementId) {
        if (!hasAnnouncement(e.getMessage().getGuild().getID()) && AnnouncementUtils.announcementExists(announcementId, e)) {
            Announcement edit = DatabaseManager.getManager().getAnnouncement(UUID.fromString(announcementId), e.getMessage().getGuild().getID());

            //Copy
            Announcement a = new Announcement(edit, true);
            a.setEditing(true);

            announcements.add(a);
            return a;
        }
        return getAnnouncement(e.getMessage().getGuild().getID());
    }

    /**
     * Terminates the creator gracefully.
     * @param e The event received upon termination.
     * @return Whether or not the Creator was successfully terminated.
     */
    public Boolean terminate(MessageReceivedEvent e) {
        if (hasAnnouncement(e.getMessage().getGuild().getID())) {
            announcements.remove(getAnnouncement(e.getMessage().getGuild().getID()));
            return true;
        }
        return false;
    }

    /**
     * Confirms the announcement and enters it into the Database.
     * @param e The event received upon confirmation.
     * @return An AnnouncementCreatorResponse with detailed information.
     */
    public AnnouncementCreatorResponse confirmAnnouncement(MessageReceivedEvent e) {
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
    /**
     * Gets the Announcement in the creator for the guild.
     * @param guildId The ID of the guild
     * @return The Announcement in the creator for the guild.
     */
    public Announcement getAnnouncement(String guildId) {
        for (Announcement a : announcements) {
            if (a.getGuildId().equals(guildId)) {
                return a;
            }
        }
        return null;
    }

    //Booleans/Checkers
    /**
     * Whether or not the Guild has an announcement in the creator.
     * @param guildId The ID of the guild.
     * @return <code>true</code> if active, else <code>false</code>.
     */
    public Boolean hasAnnouncement(String guildId) {
        for (Announcement a : announcements) {
            if (a.getGuildId().equals(guildId)) {
                return true;
            }
        }
        return false;
    }
}