package com.cloudcraftgaming.discal.module.announcement;

import com.cloudcraftgaming.discal.Main;
import com.cloudcraftgaming.discal.database.DatabaseManager;
import com.cloudcraftgaming.discal.internal.calendar.CalendarAuth;
import com.cloudcraftgaming.discal.internal.data.CalendarData;
import com.cloudcraftgaming.discal.internal.data.GuildSettings;
import com.cloudcraftgaming.discal.internal.service.AnnouncementQueueManager;
import com.cloudcraftgaming.discal.utils.EventColor;
import com.cloudcraftgaming.discal.utils.ExceptionHandler;
import com.cloudcraftgaming.discal.utils.UserUtils;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;

import java.io.IOException;
import java.util.ArrayList;
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
						} catch (IOException e) {
							ExceptionHandler.sendException(null, "Announcement failure CODE: A003", e, this.getClass());
						}
					}
				}
			} catch (Exception e) {
				ExceptionHandler.sendException(null, "Announcement failure CODE: A004", e, this.getClass());
			}
		}
	}
	
	//@Override
	public void runRewrite() {
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
							if (difference > 0) {
								if (difference <= 10) {
									//Add to queue
									AnnouncementQueueManager.getManager().queue(a, event, settings, data);
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
								//Unknown cause, send message
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
									if (difference > 0 && difference <= 10) {
										//Right on time, let's check if universal or color specific.
										if (a.getAnnouncementType().equals(AnnouncementType.UNIVERSAL)) {
											//Add to queue
											AnnouncementQueueManager.getManager().queue(a, event, settings, data);
										} else if (a.getAnnouncementType().equals(AnnouncementType.COLOR)) {
											//Color, test for color.
											String colorId = event.getColorId();
											EventColor color = EventColor.fromNameOrHexOrID(colorId);
											if (color.name().equals(a.getEventColor().name())) {
												//Add to queue
												AnnouncementQueueManager.getManager().queue(a, event, settings, data);
											}
										} else if (a.getAnnouncementType().equals(AnnouncementType.RECUR)) {
											//Recurring event announcement.
											if (event.getId().startsWith(a.getEventId()) || event.getId().contains(a.getEventId())) {
												//Add to queue
												AnnouncementQueueManager.getManager().queue(a, event, settings, data);
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
	}
	
	public static boolean accurateAnnounce(AnnouncementQueueItem i) {
		try {
			if (Main.client.getGuildByID(i.getGuildId()) == null) {
				return true;
			}
			
			//Check if times are correct (within 1 minute), if so, announce...
			long nowMs = System.currentTimeMillis();
			long announceMs = i.getTimeToAnnounceMs();
			
			long difference = nowMs - announceMs;
			
			//Check MS difference rather than minute difference for better accuracy and no rounding.
			if (difference > 60000) {
				//too early
				return false;
			} else if (difference < 0) {
				//Too late, remove from queue
				return true;
			} else {
				//Right in the 1 minute (60000 ms) range.
				sendAnnouncementMessage(i.getAnnouncement(), i.getEvent(), i.getData(), i.getSettings());
				//TODO: Send DM announcements
				
				return true;
			}
		} catch (Exception e) {
			ExceptionHandler.sendException(null, "Failed to accurate announce. CODE: A012", e, Announce.class);
			i.setTimesErrored(i.getTimesErrored() + 1);
		}
		return false;
	}

	private void doDmAnnouncements(Announcement announcement, Event event, CalendarData data, GuildSettings settings) {
		//Don't do DMs unless there is at least 1 subscriber of either role or user type.
		if (announcement.getSubscriberRoleIds().size() > 0 || announcement.getSubscriberUserIds().size() > 0) {

			IGuild guild = Main.client.getGuildByID(settings.getGuildID());
			IChannel channel = guild.getChannelByID(Long.valueOf(announcement.getAnnouncementChannelId()));

			if (announcement.getSubscriberRoleIds().contains("everyone") || announcement.getSubscriberRoleIds().contains("here")) {
				//Everyone in channel...
				for (String uId : settings.getDmAnnouncements()) {
					IUser user = UserUtils.getIUser(uId, null, guild);
					if (user != null) {
						//First check if they have DMs enabled
						if (channel.getUsersHere().contains(user)) {
							//Send DM
							AnnouncementMessageFormatter.sendAnnouncementDM(announcement, event, user, data, settings);
						}
					}
				}
			} else {
				//Let's only check for specific users...
				List<IUser> usersToDm = new ArrayList<>();

				for (String uId : settings.getDmAnnouncements()) {
					if (announcement.getSubscriberUserIds().contains(uId)) {
						//Verify user still exists and such...
						IUser u = UserUtils.getIUser(uId, null, guild);
						if (u != null && !usersToDm.contains(u)) {
							usersToDm.add(u);
						}
					} else {
						//Not specifically subscribed... lets just if their role is subscribed...
						IUser u = UserUtils.getIUser(uId, null, guild);
						if (u != null && !usersToDm.contains(u)) {
							for (IRole r : u.getRolesForGuild(guild)) {
								if (announcement.getSubscriberRoleIds().contains(r.getStringID())) {
									usersToDm.add(u);
									break;
								}
							}
						}
					}
				}

				//Now DM...
				for (IUser u : usersToDm) {
					AnnouncementMessageFormatter.sendAnnouncementDM(announcement, event, u, data, settings);
				}
			}
		}
	}
} 