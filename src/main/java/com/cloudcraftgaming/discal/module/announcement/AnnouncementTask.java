package com.cloudcraftgaming.discal.module.announcement;

import com.cloudcraftgaming.discal.Main;
import com.cloudcraftgaming.discal.database.DatabaseManager;
import com.cloudcraftgaming.discal.internal.calendar.CalendarAuth;
import com.cloudcraftgaming.discal.internal.calendar.event.EventUtils;
import com.cloudcraftgaming.discal.internal.data.CalendarData;
import com.cloudcraftgaming.discal.internal.data.GuildSettings;
import com.cloudcraftgaming.discal.utils.EventColor;
import com.cloudcraftgaming.discal.utils.ExceptionHandler;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import sx.blah.discord.handle.obj.IGuild;

import java.io.IOException;
import java.util.List;
import java.util.TimerTask;

import static com.cloudcraftgaming.discal.module.announcement.AnnouncementMessageFormatter.sendAnnouncementMessage;

/**
 * Created by Nova Fox on 10/9/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
public class AnnouncementTask extends TimerTask {
	private final int shard;

	public AnnouncementTask(int _shard) {
		shard = _shard;
	}

	@Override
	public void run() {
		try {
			//Get base calendar service
			Calendar discalService = CalendarAuth.getCalendarService();
			for (IGuild guild : Main.client.getShards().get(shard).getGuilds()) {
				GuildSettings settings = DatabaseManager.getManager().getSettings(guild.getLongID());
				CalendarData calendar = DatabaseManager.getManager().getMainCalendar(guild.getLongID());
				Calendar service = discalService;
				if (settings.useExternalCalendar()) {
					try {
						service = CalendarAuth.getCalendarService(settings);
					} catch (Exception e) {
						ExceptionHandler.sendException(null, "Failed to get external service! 00a101", e, this.getClass());
						continue;
					}
				}

				//Loop through announcements...
				for (Announcement a : DatabaseManager.getManager().getAnnouncements(guild.getLongID())) {
					try {
						if (a.getAnnouncementType() == AnnouncementType.SPECIFIC) {
							if (EventUtils.eventExists(settings, a.getEventId())) {
								Event e = service.events().get(calendar.getCalendarId(), a.getEventId()).execute();
								if (inRange(a, getEventStartMs(e))) {
									//Okay, we can announce...
									AnnouncementMessageFormatter.sendAnnouncementMessage(a, e, calendar, settings);

									//Since its specific, we can delete it
									DatabaseManager.getManager().deleteAnnouncement(a.getAnnouncementId().toString());
								}
							} else {
								//Event does not exist, delete announcement.
								DatabaseManager.getManager().deleteAnnouncement(a.getAnnouncementId().toString());
							}
						} else {
							//Check the next 20 events, if matches, we can announce...
							try {
								Events events = service.events().list(calendar.getCalendarAddress())
										.setMaxResults(20)
										.setTimeMin(new DateTime(System.currentTimeMillis()))
										.setOrderBy("startTime")
										.setSingleEvents(true)
										.setShowDeleted(false)
										.setQuotaUser(guild.getStringID())
										.execute();
								List<Event> items = events.getItems();
								if (items.size() > 0) {
									for (Event e : items) {
										if (e != null && EventUtils.eventExists(settings, e.getId())) {
											if (inRange(a, getEventStartMs(e))) {
												if (a.getAnnouncementType() == AnnouncementType.UNIVERSAL) {
													//It fits! Lets announce!
													AnnouncementMessageFormatter.sendAnnouncementMessage(a, e, calendar, settings);
												} else if (a.getAnnouncementType() == AnnouncementType.COLOR) {
													EventColor color = EventColor.fromNameOrHexOrID(e.getColorId());
													if (color.name().equals(a.getEventColor().name())) {
														//Color matches! Lets announce!
														sendAnnouncementMessage(a, e, calendar, settings);
													}
												} else if (a.getAnnouncementType() == AnnouncementType.RECUR) {
													if (e.getId().startsWith(a.getEventId()) || e.getId().contains(a.getEventId())) {
														//It fits! Lets announce!
														sendAnnouncementMessage(a, e, calendar, settings);
													}
												}
											}
										}
									}
								}
							} catch (Exception e) {
								ExceptionHandler.sendException(null, "Failed to get events 00a203", e, this.getClass());
							}

						}
					} catch (Exception e) {
						ExceptionHandler.sendException(null, "Something with announcement: " + a.getAnnouncementId() + " failed. 00a202", e, this.getClass());
					}
				}


			}
		} catch (IOException e) {
			ExceptionHandler.sendException(null, "Failed to get calendar service! 00a001", e, this.getClass());
		}
	}

	private boolean inRange(Announcement a, long eventTime) {
		long maxDifferenceMs = 10 * 60 * 1000; //10 minutes

		long announcementTimeMs = Integer.toUnsignedLong(a.getMinutesBefore() + (a.getHoursBefore() * 60)) * 60 * 1000;
		long timeUntilEvent = eventTime - System.currentTimeMillis();

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
}