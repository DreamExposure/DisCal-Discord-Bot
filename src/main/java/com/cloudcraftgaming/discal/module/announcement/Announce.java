package com.cloudcraftgaming.discal.module.announcement;

import com.cloudcraftgaming.discal.Main;
import com.cloudcraftgaming.discal.database.DatabaseManager;
import com.cloudcraftgaming.discal.internal.calendar.CalendarAuth;
import com.cloudcraftgaming.discal.internal.calendar.calendar.CalendarUtils;
import com.cloudcraftgaming.discal.internal.calendar.event.EventUtils;
import com.cloudcraftgaming.discal.internal.data.CalendarData;
import com.cloudcraftgaming.discal.internal.data.GuildSettings;
import com.cloudcraftgaming.discal.utils.EventColor;
import com.cloudcraftgaming.discal.utils.ExceptionHandler;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import sx.blah.discord.handle.obj.IGuild;

import java.io.*;
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
		try {
			Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("announcement-log.txt"), "utf-8"));
			writer.write("Starting announcements: " + System.currentTimeMillis() + "\r\n");

			ExceptionHandler.sendDebug(null, "Starting announcements!", null, this.getClass());
			DateTime now = new DateTime(System.currentTimeMillis());
			Long nowMS = System.currentTimeMillis();
			Calendar discalService;
			try {
				discalService = CalendarAuth.getCalendarService();
			} catch (IOException e) {
				ExceptionHandler.sendException(null, "Failed to connect to google calendar CODE A007", e, this.getClass());
				return;
			}
			for (IGuild guild : Main.client.getGuilds()) {
				writer.write("Starting guild: " + guild.getStringID() + " | Time: " + System.currentTimeMillis() + "\r\n");
				GuildSettings settings = DatabaseManager.getManager().getSettings(guild.getLongID());
				Calendar service;
				try {
					if (settings.useExternalCalendar()) {
						service = CalendarAuth.getCalendarService(settings);
					} else {
						service = discalService;
					}
				} catch (Exception e) {
					ExceptionHandler.sendException(null, "Failed to connect to google calendar CODE A005", e, this.getClass());
					continue;
				}
				try {
					long guildId = guild.getLongID();
					//TODO: Add multiple calendar support...
					CalendarData data = DatabaseManager.getManager().getMainCalendar(guildId);
					if (!CalendarUtils.calendarExists(data, settings)) {
						//Calendar does not exist... skip this guild.
						continue;
					}
					for (Announcement a : DatabaseManager.getManager().getAnnouncements(guildId)) {
						writer.write("Starting announcement: " + a.getAnnouncementId() + " | Time: " + System.currentTimeMillis() + "\r\n");
						if (a.getAnnouncementType().equals(AnnouncementType.SPECIFIC)) {
							try {
								Event event = service.events().get(data.getCalendarAddress(), a.getEventId()).execute();

								//Test for the time...
								Long eventMs;
								if (event.getStart().getDateTime() != null) {
									eventMs = event.getStart().getDateTime().getValue();
								} else {
									eventMs = event.getStart().getDate().getValue();
								}
								Long timeUntilEvent = eventMs - nowMS;
								Long minutesToEvent = TimeUnit.MILLISECONDS.toMinutes(timeUntilEvent);
								Long announcementTime = Integer.toUnsignedLong(a.getMinutesBefore() + (a.getHoursBefore() * 60));
								Long difference = minutesToEvent - announcementTime;
								if (difference > 0) {
									if (difference <= 10) {
										//Right on time
										sendAnnouncementMessage(a, event, data, settings);
										//doDmAnnouncements(a, event, data, settings);

										//Delete announcement to ensure it does not spam fire
										DatabaseManager.getManager().deleteAnnouncement(a.getAnnouncementId().toString());
									}
								} else {
									//Event past... Delete announcement so we need not worry about useless data in the Db costing memory.
									DatabaseManager.getManager().deleteAnnouncement(a.getAnnouncementId().toString());
								}
							} catch (GoogleJsonResponseException ge) {
								if (ge.getStatusCode() == 410 || ge.getStatusCode() == 404) {
									//Event deleted or not found, delete announcement.
									DatabaseManager.getManager().deleteAnnouncement(a.getAnnouncementId().toString());
								} else {
									//Unknown cause, send email
									ExceptionHandler.sendException(null, "Announcement failure caused by google. CODE: A001", ge, this.getClass());
								}
							} catch (Exception e) {
								ExceptionHandler.sendException(null, "Announcement failure CODE: A002", e, this.getClass());
							}
						} else {
							try {
								Events events = service.events().list(data.getCalendarAddress())
										.setMaxResults(20)
										.setTimeMin(now)
										.setOrderBy("startTime")
										.setSingleEvents(true)
										.setShowDeleted(false)
										.execute();
								List<Event> items = events.getItems();
								if (items.size() > 0) {
									for (Event event : items) {
										if (event != null && EventUtils.eventExists(settings, event.getId())) {
											//Test for the time...
											Long eventMs;
											if (event.getStart().getDateTime() != null) {
												eventMs = event.getStart().getDateTime().getValue();
											} else {
												eventMs = event.getStart().getDate().getValue();
											}
											Long timeUntilEvent = eventMs - nowMS;
											Long minutesToEvent = TimeUnit.MILLISECONDS.toMinutes(timeUntilEvent);
											Long announcementTime = Integer.toUnsignedLong(a.getMinutesBefore() + (a.getHoursBefore() * 60));
											Long difference = minutesToEvent - announcementTime;
											if (difference > 0 && difference <= 10) {
												//Right on time, let's check if universal or color specific.
												if (a.getAnnouncementType().equals(AnnouncementType.UNIVERSAL)) {
													sendAnnouncementMessage(a, event, data, settings);
													//doDmAnnouncements(a, event, data, settings);
												} else if (a.getAnnouncementType().equals(AnnouncementType.COLOR)) {
													//Color, test for color.
													String colorId = event.getColorId();
													EventColor color = EventColor.fromNameOrHexOrID(colorId);
													if (color.name().equals(a.getEventColor().name())) {
														//Color matches, announce
														sendAnnouncementMessage(a, event, data, settings);
														//doDmAnnouncements(a, event, data, settings);
													}
												} else if (a.getAnnouncementType().equals(AnnouncementType.RECUR)) {
													//Recurring event announcement.
													if (event.getId().startsWith(a.getEventId()) || event.getId().contains(a.getEventId())) {
														sendAnnouncementMessage(a, event, data, settings);
														//doDmAnnouncements(a, event, data, settings);
													}
												}
											}
										}
									}
								}
							} catch (IOException e) {
								ExceptionHandler.sendException(null, "Announcement failure CODE: A003", e, this.getClass());
							}
						}
					}
				} catch (Exception e) {
					ExceptionHandler.sendException(null, "Announcement failure CODE: A004", e, this.getClass());
				}
			}
			ExceptionHandler.sendDebug(null, "Finished announcements!", null, this.getClass());
			writer.write("Finished announcements! Time: " + System.currentTimeMillis() + "\r\n");
			writer.close();
		} catch (Exception e) {
			ExceptionHandler.sendException(null, "I dont even know", e, this.getClass());
		}
	}
}