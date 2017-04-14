package com.cloudcraftgaming.discal.module.announcement;

import com.cloudcraftgaming.discal.Main;
import com.cloudcraftgaming.discal.database.DatabaseManager;
import com.cloudcraftgaming.discal.internal.calendar.CalendarAuth;
import com.cloudcraftgaming.discal.internal.calendar.event.EventMessageFormatter;
import com.cloudcraftgaming.discal.internal.data.CalendarData;
import com.cloudcraftgaming.discal.internal.data.GuildSettings;
import com.cloudcraftgaming.discal.utils.ChannelUtils;
import com.cloudcraftgaming.discal.utils.EventColor;
import com.cloudcraftgaming.discal.utils.ExceptionHandler;
import com.cloudcraftgaming.discal.utils.Message;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IChannel;
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
@SuppressWarnings("Duplicates")
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
            em.withColor(56, 138, 237);
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

                if (event.getSummary() != null) {
                    em.appendField("Event Summary", event.getSummary(), true);
                }
            } catch (IOException e) {
                //Failed to get from google cal.
                ExceptionHandler.sendException(null, "Failed to get event for announcement.", e, AnnouncementMessageFormatter.class);
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
            em.withColor(56, 138, 237);
        }

        return em.build();
    }

    /**
     * Sends an embed with the announcement info in a proper format.
     *
     * @param announcement The announcement to send info about.
     * @param event        the calendar event the announcement is for.
     * @param data         The BotData belonging to the guild.
     */
    static void sendAnnouncementMessage(Announcement announcement, Event event, CalendarData data, GuildSettings settings) {
        EmbedBuilder em = new EmbedBuilder();
        em.withAuthorIcon(Main.client.getGuildByID("266063520112574464").getIconURL());
        em.withAuthorName("DisCal");
        em.withTitle("!~Event Announcement~!");
        if (event.getSummary() != null) {
            em.appendField("Event Name/Summary", event.getSummary(), true);
        }
        if (event.getDescription() != null) {
            em.appendField("Event Description", event.getDescription(), true);
        }
        if (!settings.usingSimpleAnnouncements()) {
            em.appendField("Event Date", EventMessageFormatter.getHumanReadableDate(event.getStart()), true);
            em.appendField("Event Time", EventMessageFormatter.getHumanReadableTime(event.getStart()), true);
            try {
                Calendar service = CalendarAuth.getCalendarService();
                String tz = service.calendars().get(data.getCalendarAddress()).execute().getTimeZone();
                em.appendField("TimeZone", tz, true);
            } catch (Exception e1) {
                em.appendField("TimeZone", "Unknown *Error Occurred", true);
            }
        } else {
            String start = EventMessageFormatter.getHumanReadableDate(event.getStart()) + " at " + EventMessageFormatter.getHumanReadableTime(event.getStart());
            try {
                Calendar service = CalendarAuth.getCalendarService();
                String tz = service.calendars().get(data.getCalendarAddress()).execute().getTimeZone();
                start = start + " " + tz;
            } catch (Exception e1) {
                start = start + " (TZ UNKNOWN/ERROR)";
            }

            em.appendField("Event Start", start, false);
        }

        if (!settings.usingSimpleAnnouncements()) {
            em.appendField("Event ID", event.getId(), false);
        }
        em.appendField("Additional Info", announcement.getInfo(), false);
        em.withUrl(event.getHtmlLink());
        if (!settings.usingSimpleAnnouncements()) {
            em.withFooterText("Announcement ID: " + announcement.getAnnouncementId().toString());
        }
        try {
            EventColor ec = EventColor.fromNameOrHexOrID(event.getColorId());
            em.withColor(ec.getR(), ec.getG(), ec.getB());
        } catch (Exception e) {
            //I dunno, color probably null.
            em.withColor(56, 138, 237);
        }

        IGuild guild = Main.client.getGuildByID(announcement.getGuildId());

        IChannel channel = guild.getChannelByID(announcement.getAnnouncementChannelId());

        Message.sendMessage(em.build(), getSubscriberMentions(announcement, guild), channel, Main.client);
    }

    static void sendAnnouncementDM(Announcement announcement, Event event, IUser user, CalendarData data, GuildSettings settings) {
        EmbedBuilder em = new EmbedBuilder();
        em.withAuthorIcon(Main.client.getGuildByID("266063520112574464").getIconURL());
        em.withAuthorName("DisCal");
        em.withTitle("!~Event Announcement~!");
        if (event.getSummary() != null) {
            em.appendField("Event Name/Summary", event.getSummary(), true);
        }
        if (event.getDescription() != null) {
            em.appendField("Event Description", event.getDescription(), true);
        }
        if (!settings.usingSimpleAnnouncements()) {
            em.appendField("Event Date", EventMessageFormatter.getHumanReadableDate(event.getStart()), true);
            em.appendField("Event Time", EventMessageFormatter.getHumanReadableTime(event.getStart()), true);
            try {
                Calendar service = CalendarAuth.getCalendarService();
                String tz = service.calendars().get(data.getCalendarAddress()).execute().getTimeZone();
                em.appendField("TimeZone", tz, true);
            } catch (Exception e1) {
                em.appendField("TimeZone", "Unknown *Error Occurred", true);
            }
        } else {
            String start = EventMessageFormatter.getHumanReadableDate(event.getStart()) + " at " + EventMessageFormatter.getHumanReadableTime(event.getStart());
            try {
                Calendar service = CalendarAuth.getCalendarService();
                String tz = service.calendars().get(data.getCalendarAddress()).execute().getTimeZone();
                start = start + " " + tz;
            } catch (Exception e1) {
                start = start + " (TZ UNKNOWN/ERROR)";
            }

            em.appendField("Event Start", start, false);
        }

        if (!settings.usingSimpleAnnouncements()) {
            em.appendField("Event ID", event.getId(), false);
        }
        em.appendField("Additional Info", announcement.getInfo(), false);
        em.withUrl(event.getHtmlLink());
        if (!settings.usingSimpleAnnouncements()) {
            em.withFooterText("Announcement ID: " + announcement.getAnnouncementId().toString());
        }
        try {
            EventColor ec = EventColor.fromNameOrHexOrID(event.getColorId());
            em.withColor(ec.getR(), ec.getG(), ec.getB());
        } catch (Exception e) {
            //I dunno, color probably null.
            em.withColor(56, 138, 237);
        }

        IGuild guild = Main.client.getGuildByID(announcement.getGuildId());

        String msg = "Announcement in Guild: `" + guild.getName() + "`" + Message.lineBreak + Message.lineBreak + "You are receiving this DM because you enabled DM announcements for the respective guild." + Message.lineBreak + "To disable this, go to the guild and use `discal dmAnnouncements`";

        Message.sendDirectMessage(msg, em.build(), user);
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

    public static String getSubscriberMentions(Announcement a, IGuild guild) {
        StringBuilder userMentions = new StringBuilder();
        for (String userId : a.getSubscriberUserIds()) {
            try {
                IUser user = guild.getUserByID(userId);
                if (user != null) {
                    userMentions.append(user.mention(true)).append(" ");
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
                        roleMentions.append(role.mention()).append(" ");
                    }
                } catch (Exception e) {
                    //Role does not exist, safely ignore.
                }
            }
        }

        String message = "Subscribers: " + userMentions + " " + roleMentions;
        if (mentionEveryone) {
            message = message + " " + guild.getEveryoneRole().mention();
        }
        if (mentionHere) {
            message = message + " @here";
        }

        return message;
    }
}