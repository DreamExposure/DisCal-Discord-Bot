package com.cloudcraftgaming.discal.module.announcement;

import com.cloudcraftgaming.discal.Main;
import com.cloudcraftgaming.discal.api.calendar.CalendarAuth;
import com.cloudcraftgaming.discal.api.database.DatabaseManager;
import com.cloudcraftgaming.discal.api.enums.announcement.AnnouncementType;
import com.cloudcraftgaming.discal.api.enums.event.EventColor;
import com.cloudcraftgaming.discal.api.object.GuildSettings;
import com.cloudcraftgaming.discal.api.object.announcement.Announcement;
import com.cloudcraftgaming.discal.api.object.calendar.CalendarData;
import com.cloudcraftgaming.discal.api.utils.EventUtils;
import com.cloudcraftgaming.discal.api.utils.ExceptionHandler;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.sun.javafx.scene.control.skin.VirtualFlow;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TimerTask;

/**
 * Created by Nova Fox on 10/9/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
public class AnnouncementTask extends TimerTask {
	private Calendar discalService;

	private HashMap<Long, GuildSettings> allSettings = new HashMap<>();
	private HashMap<Long, CalendarData> calendars = new HashMap<>();
	private HashMap<Long, Calendar> customServices = new HashMap<>();
	private HashMap<Long, List<Event>> allEvents = new HashMap<>();


	@Override
	public void run() {
		//Get the default stuff.
		try {
			discalService = CalendarAuth.getCalendarService();
		} catch (IOException e) {
			ExceptionHandler.sendException(null, "Failed to get service! 00a0101", e, this.getClass());
		}

		ArrayList<Announcement> allAnnouncements = DatabaseManager.getManager().getAnnouncements();

		for (Announcement a : allAnnouncements) {
			//Check if guild is part of DisCal's guilds. This way we can clear out the database...
			if (!active(a)) {
				DatabaseManager.getManager().deleteAnnouncement(a.getAnnouncementId().toString());
				continue;
			}
			//Get everything we need ready.
			GuildSettings settings = getSettings(a);
			CalendarData calendar = getCalendarData(a);
			Calendar service;
			try {
				service = getService(settings);
			} catch (Exception e) {
				ExceptionHandler.sendException(null, "Failed to handle custom service! 00a102", e, this.getClass());
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
								DatabaseManager.getManager().deleteAnnouncement(a.getAnnouncementId().toString());
							}
						} catch (IOException e) {
							//Event getting error, we know it exists tho
							ExceptionHandler.sendException(null, "Failed to get event! 00a103", e, this.getClass());
						}
					} else {
						//Event is gone, we can just delete this shit.
						DatabaseManager.getManager().deleteAnnouncement(a.getAnnouncementId().toString());
					}
					break;
				case UNIVERSAL:
					for (Event e : getEvents(settings, calendar, service)) {
						if (inRange(a, e)) {
							//It fits! Let's do it!
							AnnouncementMessageFormatter.sendAnnouncementMessage(a, e, calendar, settings);
						}
					}
					break;
				case COLOR:
					for (Event e : getEvents(settings, calendar, service)) {
						if (a.getEventColor() == EventColor.fromNameOrHexOrID(e.getColorId())) {
							if (inRange(a, e)) {
								//It fits! Let's do it!
								AnnouncementMessageFormatter.sendAnnouncementMessage(a, e, calendar, settings);
							}
						}
					}
					break;
				case RECUR:
					for (Event e : getEvents(settings, calendar, service)) {
						if (inRange(a, e)) {
							if (e.getId().contains("_") && e.getId().split("_")[0].equals(a.getEventId())) {
								//It fits! Lets announce!
								AnnouncementMessageFormatter.sendAnnouncementMessage(a, e, calendar, settings);
							}
						}
					}
					break;
			}
		}

		//Just clear everything immediately.
		allSettings.clear();
		calendars.clear();
		customServices.clear();
		allEvents.clear();
	}

	private boolean inRange(Announcement a, Event e) {
		long maxDifferenceMs = 5 * 60 * 1000; //5 minutes

		long announcementTimeMs = Integer.toUnsignedLong(a.getMinutesBefore() + (a.getHoursBefore() * 60)) * 60 * 1000;
		long timeUntilEvent = getEventStartMs(e) - System.currentTimeMillis();

		long difference = timeUntilEvent - announcementTimeMs;

		if (difference < 0) {
			//Event past, we can delete announcement depending on the type
			if (a.getAnnouncementType() == AnnouncementType.SPECIFIC) {
				DatabaseManager.getManager().deleteAnnouncement(a.getAnnouncementId().toString());
			}
			return false;
		} else {
			return difference <= maxDifferenceMs;
		}
	}

	private long getEventStartMs(Event e) {
		if (e.getStart().getDateTime() != null) {
			return e.getStart().getDateTime().getValue();
		} else {
			return e.getStart().getDate().getValue();
		}
	}

	private GuildSettings getSettings(Announcement a) {
		if (!allSettings.containsKey(a.getGuildId())) {
			allSettings.put(a.getGuildId(), DatabaseManager.getManager().getSettings(a.getGuildId()));
		}
		return allSettings.get(a.getGuildId());
	}

	private CalendarData getCalendarData(Announcement a) {
		if (!calendars.containsKey(a.getGuildId())) {
			calendars.put(a.getGuildId(), DatabaseManager.getManager().getMainCalendar(a.getGuildId()));
		}
		return calendars.get(a.getGuildId());
	}

	private Calendar getService(GuildSettings gs) throws Exception {
		if (gs.useExternalCalendar()) {
			if (!customServices.containsKey(gs.getGuildID())) {
				customServices.put(gs.getGuildID(), CalendarAuth.getCalendarService(gs));
			}
			return customServices.get(gs.getGuildID());
		}
		return discalService;
	}

	private List<Event> getEvents(GuildSettings gs, CalendarData cd, Calendar service) {
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
				ExceptionHandler.sendException(null, "Failed to get events list! 00x2304", e, this.getClass());
				allEvents.put(gs.getGuildID(), new VirtualFlow.ArrayLinkedList<>());
			}
		}
		return allEvents.get(gs.getGuildID());
	}


	private boolean active(Announcement a) {
		return Main.client.getGuildByID(a.getGuildId()) != null;
	}
}