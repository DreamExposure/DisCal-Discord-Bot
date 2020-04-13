package org.dreamexposure.discal.client.module.announcement;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import org.dreamexposure.discal.client.DisCalClient;
import org.dreamexposure.discal.client.message.AnnouncementMessageFormatter;
import org.dreamexposure.discal.core.calendar.CalendarAuth;
import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.enums.announcement.AnnouncementType;
import org.dreamexposure.discal.core.enums.event.EventColor;
import org.dreamexposure.discal.core.logger.LogFeed;
import org.dreamexposure.discal.core.logger.object.LogObject;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.announcement.Announcement;
import org.dreamexposure.discal.core.object.calendar.CalendarData;
import org.dreamexposure.discal.core.utils.EventUtils;
import org.dreamexposure.discal.core.utils.GuildUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import discord4j.core.object.entity.Guild;
import discord4j.rest.util.Snowflake;

@SuppressWarnings({"WeakerAccess", "Duplicates"})
public class AnnouncementThread extends Thread {

    private Calendar discalService;

    private final HashMap<Snowflake, GuildSettings> allSettings = new HashMap<>();
    private final HashMap<Snowflake, CalendarData> calendars = new HashMap<>();
    private final HashMap<Snowflake, Calendar> customServices = new HashMap<>();
    private final HashMap<Snowflake, List<Event>> allEvents = new HashMap<>();

    public AnnouncementThread() {
    }


    @Override
    public void run() {
        try {
            //Verify the client is logged in
            if (DisCalClient.getClient() == null)
                return;

            //Get the default stuff.
            try {
                discalService = CalendarAuth.getCalendarService(null);
            } catch (IOException e) {
                LogFeed.log(LogObject.forException("Failed to get service", e, this.getClass()));
            }

            for (Guild g : DisCalClient.getClient().getGuilds().toIterable()) {
                List<Announcement> allAnnouncements = DatabaseManager.getEnabledAnnouncements(g.getId()).block();
                for (Announcement a : allAnnouncements) {
                    try {
                        //Check if guild is part of DisCal's guilds. This way we can clear out the database...
                        if (!GuildUtils.active(a.getGuildId())) {
                            DatabaseManager.deleteAnnouncement(a.getAnnouncementId().toString()).subscribe();
                            continue;
                        }
                        //Get everything we need ready.
                        GuildSettings settings = getSettings(a);
                        CalendarData calendar = getCalendarData(a);
                        Calendar service;

                        try {
                            service = getService(settings);
                        } catch (Exception e) {
                            LogFeed.log(LogObject
                                    .forException("Failed to handle server", e, this.getClass()));
                            continue;
                        }

                        //Now we can check the announcement type and do all the actual logic here.
                        switch (a.getAnnouncementType()) {
                            case SPECIFIC:
                                if (EventUtils.eventExists(settings, a.getEventId())) {
                                    try {
                                        Event e = service.events().get(calendar.getCalendarId(), a.getEventId()).execute();
                                        if (inRange(a, e)) {
                                            //We can announce it.
                                            AnnouncementMessageFormatter.sendAnnouncementMessage(a, e, calendar, settings);
                                            //And now lets delete it
                                            DatabaseManager.deleteAnnouncement(a.getAnnouncementId().toString()).subscribe();
                                        }
                                    } catch (IOException e) {
                                        //Event getting error, we know it exists tho
                                        LogFeed.log(LogObject
                                                .forException("Failed to get event", e,
                                                        this.getClass()));
                                    }
                                } else {
                                    //Event is gone, we can just delete this shit.
                                    DatabaseManager.deleteAnnouncement(a.getAnnouncementId().toString()).subscribe();
                                }
                                break;
                            case UNIVERSAL:
                                for (Event e : getEvents(settings, calendar, service, a)) {
                                    if (inRange(a, e)) {
                                        //It fits! Let's do it!
                                        AnnouncementMessageFormatter.sendAnnouncementMessage(a, e, calendar, settings);
                                    }
                                }
                                break;
                            case COLOR:
                                for (Event e : getEvents(settings, calendar, service, a)) {
                                    if (a.getEventColor() == EventColor.fromNameOrHexOrID(e.getColorId())) {
                                        if (inRange(a, e)) {
                                            //It fits! Let's do it!
                                            AnnouncementMessageFormatter.sendAnnouncementMessage(a, e, calendar, settings);
                                        }
                                    }
                                }
                                break;
                            case RECUR:
                                for (Event e : getEvents(settings, calendar, service, a)) {
                                    if (inRange(a, e)) {
                                        if (e.getId().contains("_") && e.getId().split("_")[0].equals(a.getEventId())) {
                                            //It fits! Lets announce!
                                            AnnouncementMessageFormatter.sendAnnouncementMessage(a, e, calendar, settings);
                                        }
                                    }
                                }
                                break;
                        }
                    } catch (Exception e) {
                        LogFeed.log(LogObject
                                .forException("Announcement Failed",
                                        "ID: " + a.getAnnouncementId() + ", GUILD: " +
                                                a.getGuildId(), e, this.getClass()));
                    }
                }
            }


            //Just clear everything immediately.
            allSettings.clear();
            calendars.clear();
            customServices.clear();
            allEvents.clear();
        } catch (Exception e) {
            LogFeed.log(LogObject
                    .forException("SOMETHING BAD IN THE ANNOUNCER", e, this.getClass()));

            //Clear everything because why take up RAM after is broke???
            allSettings.clear();
            calendars.clear();
            customServices.clear();
            allEvents.clear();
        }
    }

    private boolean inRange(Announcement a, Event e) {
        long maxDifferenceMs = 5 * 60 * 1000; //5 minutes

        long announcementTimeMs = Integer.toUnsignedLong(a.getMinutesBefore() + (a.getHoursBefore() * 60)) * 60 * 1000;
        long timeUntilEvent = getEventStartMs(e) - System.currentTimeMillis();

        long difference = timeUntilEvent - announcementTimeMs;

        if (difference < 0) {
            //Event past, we can delete announcement depending on the type
            if (a.getAnnouncementType() == AnnouncementType.SPECIFIC)
                DatabaseManager.deleteAnnouncement(a.getAnnouncementId().toString()).subscribe();

            return false;
        } else {
            return difference <= maxDifferenceMs;
        }
    }

    private long getEventStartMs(Event e) {
        if (e.getStart().getDateTime() != null)
            return e.getStart().getDateTime().getValue();
        else
            return e.getStart().getDate().getValue();

    }

    private GuildSettings getSettings(Announcement a) {
        if (!allSettings.containsKey(a.getGuildId()))
            allSettings.put(a.getGuildId(), DatabaseManager.getSettings(a.getGuildId()).block());

        return allSettings.get(a.getGuildId());
    }

    private CalendarData getCalendarData(Announcement a) {
        if (!calendars.containsKey(a.getGuildId()))
            calendars.put(a.getGuildId(), DatabaseManager.getMainCalendar(a.getGuildId()).block());

        return calendars.get(a.getGuildId());
    }

    private Calendar getService(GuildSettings gs) throws Exception {
        if (gs.useExternalCalendar()) {
            if (!customServices.containsKey(gs.getGuildID()))
                customServices.put(gs.getGuildID(), CalendarAuth.getCalendarService(gs));

            return customServices.get(gs.getGuildID());
        }
        return discalService;
    }

    private List<Event> getEvents(GuildSettings gs, CalendarData cd, Calendar service, Announcement a) {
        if (!allEvents.containsKey(gs.getGuildID())) {
            try {
                Events events = service.events().list(cd.getCalendarAddress())
                        .setMaxResults(15)
                        .setTimeMin(new DateTime(System.currentTimeMillis()))
                        .setOrderBy("startTime")
                        .setSingleEvents(true)
                        .setShowDeleted(false)
                        .execute();
                List<Event> items = events.getItems();
                allEvents.put(gs.getGuildID(), items);
            } catch (IOException e) {
                LogFeed.log(LogObject
                        .forException("Failed to event events list",
                                "Guild: " + gs.getGuildID() + " | Announcement: "
                                        + a.getAnnouncementId(), e, this.getClass()));
                return new ArrayList<>();
            }
        }
        return allEvents.get(gs.getGuildID());
    }
}