package com.cloudcraftgaming.discal.module.announcement;

import com.cloudcraftgaming.discal.Main;
import com.cloudcraftgaming.discal.database.DatabaseManager;
import com.cloudcraftgaming.discal.internal.calendar.CalendarAuth;
import com.cloudcraftgaming.discal.internal.data.CalendarData;
import com.cloudcraftgaming.discal.internal.data.GuildSettings;
import com.cloudcraftgaming.discal.internal.email.EmailSender;
import com.cloudcraftgaming.discal.utils.EventColor;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import sx.blah.discord.handle.obj.IGuild;

import java.io.IOException;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import static com.cloudcraftgaming.discal.module.announcement.AnnouncementMessageFormatter.sendAnnouncementMessage;

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
                GuildSettings settings = DatabaseManager.getManager().getSettings(guild.getID());
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
                                        sendAnnouncementMessage(a, event, data, settings);

                                        //Delete announcement to ensure it does not spam fire
                                        DatabaseManager.getManager().deleteAnnouncement(a.getAnnouncementId().toString());
                                    }
                                } else {
                                    //Event past... Delete announcement so we need not worry about useless data in the Db costing memory.
                                    DatabaseManager.getManager().deleteAnnouncement(a.getAnnouncementId().toString());
                                }
                            }  catch (GoogleJsonResponseException ge) {
                                if (ge.getStatusCode() == 410 || ge.getStatusCode() == 404) {
                                    //Event deleted or not found, delete announcement.
                                    DatabaseManager.getManager().deleteAnnouncement(a.getAnnouncementId().toString());
                                } else {
                                    //Unknown cause, send email
                                    EmailSender.getSender().sendExceptionEmail(ge, this.getClass());
                                }
                            } catch (Exception e) {
                                EmailSender.getSender().sendExceptionEmail(e, this.getClass());
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
                                                sendAnnouncementMessage(a, event, data, settings);
                                            } else if (a.getAnnouncementType().equals(AnnouncementType.COLOR)) {
                                                //Color, test for color.
                                                String colorId = event.getColorId();
                                                EventColor color = EventColor.fromNameOrHexOrID(colorId);
                                                if (color.name().equals(a.getEventColor().name())) {
                                                    //Color matches, announce
                                                    sendAnnouncementMessage(a, event, data, settings);
                                                }
                                            } else if (a.getAnnouncementType().equals(AnnouncementType.RECUR)) {
                                                //Recurring event announcement.
                                                if (event.getId().startsWith(a.getEventId()) || event.getId().contains(a.getEventId())) {
                                                    sendAnnouncementMessage(a, event, data, settings);
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
}