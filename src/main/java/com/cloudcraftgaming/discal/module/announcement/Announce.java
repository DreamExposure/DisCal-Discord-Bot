package com.cloudcraftgaming.discal.module.announcement;

import com.cloudcraftgaming.discal.Main;
import com.cloudcraftgaming.discal.database.DatabaseManager;
import com.cloudcraftgaming.discal.internal.calendar.CalendarAuth;
import com.cloudcraftgaming.discal.internal.calendar.event.EventMessageFormatter;
import com.cloudcraftgaming.discal.internal.data.CalendarData;
import com.cloudcraftgaming.discal.internal.email.EmailSender;
import com.cloudcraftgaming.discal.utils.EventColor;
import com.cloudcraftgaming.discal.utils.Message;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

import java.io.IOException;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * Created by Nova Fox on 3/4/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class Announce extends TimerTask {
    @Override
    public void run() {
        //EmailSender.getSender().sendDebugEmail(this.getClass(), "01", "Announcement Runnable Start");
        DateTime now = new DateTime(System.currentTimeMillis());
        Long nowMS = System.currentTimeMillis();
        try {
            Calendar service = CalendarAuth.getCalendarService();
            for (IGuild guild : Main.client.getGuilds()) {
                try {
                    String guildId = guild.getID();
                    //TODO: Add multiple calendar support...
                    CalendarData data = DatabaseManager.getManager().getMainCalendar(guildId);
                    for (Announcement a : DatabaseManager.getManager().getAnnouncements(guildId)) {
                        if (a.getAnnouncementType().equals(AnnouncementType.SPECIFIC)) {
                            try {
                                Event event = service.events().get(data.getCalendarAddress(), a.getEventId()).execute();

                                //Test for the time...
                                Long eventMS = event.getStart().getDateTime().getValue();
                                Long timeUntilEvent = eventMS - nowMS;
                                Long minutesToEvent = TimeUnit.MILLISECONDS.toMinutes(timeUntilEvent);
                                Long announcementTime = Integer.toUnsignedLong(a.getMinutesBefore() + (a.getHoursBefore() * 60));
                                Long difference = minutesToEvent - announcementTime;
                                if (difference >= 0) {
                                    if (difference <= 10) {
                                        //Right on time
                                        sendAnnouncementMessage(a, event, data);

                                        //Delete announcement to ensure it does not spam fire
                                        DatabaseManager.getManager().deleteAnnouncement(a.getAnnouncementId().toString());
                                    }
                                } else {
                                    //Event past... Delete announcement so we need not worry about useless data in the Db costing memory.
                                    DatabaseManager.getManager().deleteAnnouncement(a.getAnnouncementId().toString());
                                }
                            } catch (Exception e) {
                                //Check error first...
                                if (e instanceof GoogleJsonResponseException) {
                                    GoogleJsonResponseException ge = (GoogleJsonResponseException) e;
                                    if (ge.getStatusCode() == 410 || ge.getStatusCode() == 404) {
                                        //Event deleted or not found, delete announcement.
                                        DatabaseManager.getManager().deleteAnnouncement(a.getAnnouncementId().toString());
                                    } else {
                                        //Unknown cause, send email
                                        EmailSender.getSender().sendExceptionEmail(e, this.getClass());
                                    }
                                }
                            }
                        } else {
                            try {
                                Events events = service.events().list(data.getCalendarAddress())
                                        .setMaxResults(20)
                                        .setTimeMin(now)
                                        .setOrderBy("startTime")
                                        .setSingleEvents(true)
                                        .execute();
                                List<Event> items = events.getItems();
                                if (items.size() > 0) {
                                    for (Event event : items) {
                                        //Test for the time...
                                        Long eventMS = event.getStart().getDateTime().getValue();
                                        Long timeUntilEvent = eventMS - nowMS;
                                        Long minutesToEvent = TimeUnit.MILLISECONDS.toMinutes(timeUntilEvent);
                                        Long announcementTime = Integer.toUnsignedLong(a.getMinutesBefore() + (a.getHoursBefore() * 60));
                                        Long difference = minutesToEvent - announcementTime;
                                        if (difference >= 0 && difference <= 10) {
                                            //Right on time, let's check if universal or color specific.
                                            if (a.getAnnouncementType().equals(AnnouncementType.UNIVERSAL)) {
                                                sendAnnouncementMessage(a, event, data);
                                            } else if (a.getAnnouncementType().equals(AnnouncementType.COLOR)) {
                                                //Color, test for color.
                                                String colorId = event.getColorId();
                                                EventColor color = EventColor.fromNameOrHexOrID(colorId);
                                                if (color.name().equals(a.getEventColor().name())) {
                                                    //Color matches, announce
                                                    sendAnnouncementMessage(a, event, data);
                                                }
                                            } else if (a.getAnnouncementType().equals(AnnouncementType.RECUR)) {
                                                //Recurring event announcement.
                                                if (event.getId().startsWith(a.getEventId()) || event.getId().contains(a.getEventId())) {
                                                    sendAnnouncementMessage(a, event, data);
                                                }
                                            }
                                        }
                                    }
                                }
                            } catch (IOException e) {
                                EmailSender.getSender().sendExceptionEmail(e, this.getClass());
                            }
                        }
                    }
                } catch (Exception e) {
                    EmailSender.getSender().sendExceptionEmail(e, this.getClass());
                }
            }
        } catch (IOException e) {
            EmailSender.getSender().sendExceptionEmail(e, this.getClass());
        }
    }

    /**
     * Sends an embed with the announcement info in a proper format.
     *
     * @param announcement The announcement to send info about.
     * @param event        the calendar event the announcement is for.
     * @param data         The BotData belonging to the guild.
     */
    private void sendAnnouncementMessage(Announcement announcement, Event event, CalendarData data) {
        EmbedBuilder em = new EmbedBuilder();
        em.withAuthorIcon(Main.client.getGuildByID("266063520112574464").getIconURL());
        em.withAuthorName("DisCal");
        em.withTitle("!~Event Announcement~!");
        em.appendField("Event Name/Summary", event.getSummary(), true);
        em.appendField("Event Description", event.getDescription(), true);
        em.appendField("Event Date", EventMessageFormatter.getHumanReadableDate(event.getStart()), true);
        em.appendField("Event Time", EventMessageFormatter.getHumanReadableTime(event.getStart()), false);
        try {
            em.appendField("TimeZone", event.getStart().getTimeZone(), true);
        } catch (Exception e) {
            try {
                Calendar service = CalendarAuth.getCalendarService();
                String tz = service.calendars().get(data.getCalendarAddress()).execute().getTimeZone();
                em.appendField("TimeZone", tz, true);
            } catch (Exception e1) {
                em.appendField("TimeZone", "Unknown *Error Occurred", true);
            }
        }
        em.appendField("Event ID", event.getId(), false);
        em.appendField("Additional Info", announcement.getInfo(), false);
        em.withUrl(event.getHtmlLink());
        em.withFooterText("Announcement ID: " + announcement.getAnnouncementId().toString());
        try {
            EventColor ec = EventColor.fromNameOrHexOrID(event.getColorId());
            em.withColor(ec.getR(), ec.getG(), ec.getB());
        } catch (Exception e) {
            //I dunno, color probably null.
            em.withColor(36, 153, 153);
        }

        IGuild guild = Main.client.getGuildByID(announcement.getGuildId());

        StringBuilder userMentions = new StringBuilder();
        for (String userId : announcement.getSubscriberUserIds()) {
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
        for (String roleId : announcement.getSubscriberRoleIds()) {
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

        IChannel channel = guild.getChannelByID(announcement.getAnnouncementChannelId());
        Message.sendMessage(em.build(), message, channel, Main.client);
    }
}