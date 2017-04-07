package com.cloudcraftgaming.discal.module.announcement;

import com.cloudcraftgaming.discal.Main;
import com.cloudcraftgaming.discal.database.DatabaseManager;
import com.cloudcraftgaming.discal.internal.calendar.CalendarAuth;
import com.cloudcraftgaming.discal.internal.data.CalendarData;
import com.cloudcraftgaming.discal.utils.ChannelUtils;
import com.cloudcraftgaming.discal.utils.EventColor;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

import java.io.IOException;

/**
 * Created by Nova Fox on 3/4/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class AnnouncementMessageFormatter {

    /**
     * Gets the EmbedObject for an Announcement.
     * @param a The Announcement to embed.
     * @return The EmbedObject for the Announcement.
     */
    public static EmbedObject getFormatAnnouncementEmbed(Announcement a) {
        EmbedBuilder em = new EmbedBuilder();
        em.withAuthorIcon(Main.client.getGuildByID("266063520112574464").getIconURL());
        em.withAuthorName("DisCal");
        em.withTitle("Announcement Info");
        em.appendField("Announcement ID", a.getAnnouncementId().toString(), true);
        em.appendField("Announcement Type", a.getAnnouncementType().name(), true);
        if (a.getAnnouncementType().equals(AnnouncementType.SPECIFIC)) {
            em.appendField("Event ID", a.getEventId(), true);
        } else if (a.getAnnouncementType().equals(AnnouncementType.COLOR)) {
            em.appendField("Event Color", a.getEventColor().name(), true);
        } else if (a.getAnnouncementType().equals(AnnouncementType.RECUR)) {
            em.appendField("Recurring Event ID", a.getEventId(), true);
        }
        em.appendField("Hours Before", String.valueOf(a.getHoursBefore()), true);
        em.appendField("Minutes Before", String.valueOf(a.getMinutesBefore()), true);
        em.appendField("In Channel (Name)", ChannelUtils.getChannelNameFromNameOrId(a.getAnnouncementChannelId(), a.getGuildId()), true);
        em.appendField("Additional Info", a.getInfo(), false);
        if (a.getAnnouncementType().equals(AnnouncementType.COLOR)) {
            EventColor c = a.getEventColor();
            em.withColor(c.getR(), c.getG(), c.getB());
        } else {
            em.withColor(36, 153, 153);
        }

        return em.build();
    }

    /**
     * Gets the EmbedObject for a Condensed Announcement.
     * @param a The Announcement to embed.
     * @return The EmbedObject for a Condensed Announcement.
     */
    public static EmbedObject getCondensedAnnouncementEmbed(Announcement a) {
        EmbedBuilder em = new EmbedBuilder();
        em.withAuthorIcon(Main.client.getGuildByID("266063520112574464").getIconURL());
        em.withAuthorName("DisCal");
        em.withTitle("Condensed Announcement Info");
        em.appendField("Announcement ID", a.getAnnouncementId().toString(), false);
        em.appendField("Time Before", condensedTime(a), false);

        if (a.getAnnouncementType().equals(AnnouncementType.SPECIFIC)) {
            em.appendField("Event ID", a.getEventId(), false);
            try {
                Calendar service = CalendarAuth.getCalendarService();
                //TODO: Handle multiple calendars...
                CalendarData data = DatabaseManager.getManager().getMainCalendar(a.getGuildId());
                Event event = service.events().get(data.getCalendarAddress(), a.getEventId()).execute();

                em.appendField("Event Summary", event.getSummary(), true);
            } catch (IOException e) {
                em.appendField("Event Summary", "Unknown (Error)", true);
            }
        } else if (a.getAnnouncementType().equals(AnnouncementType.COLOR)) {
            em.appendField("Event Color", a.getEventColor().name(), true);
        } else if (a.getAnnouncementType().equals(AnnouncementType.RECUR)) {
            em.appendField("Recurring Event ID", a.getEventId(), true);
        }
        em.withFooterText("Type: " + a.getAnnouncementType().name());

        if (a.getAnnouncementType().equals(AnnouncementType.COLOR)) {
            EventColor c = a.getEventColor();
            em.withColor(c.getR(), c.getG(), c.getB());
        } else {
            em.withColor(36, 153, 153);
        }

        return em.build();
    }

    /**
     * Gets the formatted time from an Announcement.
     * @param a The Announcement.
     * @return The formatted time from an Announcement.
     */
    private static String condensedTime(Announcement a) {
        return a.getHoursBefore() + "H" + a.getMinutesBefore() + "m";
    }

    public static String getSubscriberNames(Announcement a) {
        //Loop and get subs without mentions...
        IGuild guild = Main.client.getGuildByID(a.getGuildId());

        StringBuilder userMentions = new StringBuilder();
        for (String userId : a.getSubscriberUserIds()) {
            try {
                IUser user = guild.getUserByID(userId);
                if (user != null) {
                    userMentions.append(user.getName()).append(" ");
                }
            } catch (Exception e) {
                //User does not exist, safely ignore.
            }
        }

        StringBuilder roleMentions = new StringBuilder();
        Boolean mentionEveryone = false;
        Boolean mentionHere = false;
        for (String roleId : a.getSubscriberRoleIds()) {
            if (roleId.equalsIgnoreCase("everyone")) {
                mentionEveryone = true;
            } else if (roleId.equalsIgnoreCase("here")) {
                mentionHere = true;
            } else {
                try {
                    IRole role = guild.getRoleByID(roleId);
                    if (role != null) {
                        roleMentions.append(role.getName()).append(" ");
                    }
                } catch (Exception e) {
                    //Role does not exist, safely ignore.
                }
            }
        }

        String message = "Subscribers: " + userMentions + " " + roleMentions;
        if (mentionEveryone) {
            message = message + " " + guild.getEveryoneRole().getName();
        }
        if (mentionHere) {
            message = message + " here";
        }

        return message;
    }
}