package com.cloudcraftgaming.discal.internal.service;

import com.cloudcraftgaming.discal.internal.data.CalendarData;
import com.cloudcraftgaming.discal.internal.data.GuildSettings;
import com.cloudcraftgaming.discal.module.announcement.Announce;
import com.cloudcraftgaming.discal.module.announcement.Announcement;
import com.cloudcraftgaming.discal.module.announcement.AnnouncementQueueItem;
import com.cloudcraftgaming.discal.utils.AnnouncementUtils;
import com.cloudcraftgaming.discal.utils.ExceptionHandler;
import com.google.api.services.calendar.model.Event;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/**
 * Created by Nova Fox on 7/23/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class AnnouncementQueueManager {
	private static AnnouncementQueueManager instance;
	
	private final Timer timer;

	private ArrayList<AnnouncementQueueItem> queue = new ArrayList<>();

	private AnnouncementQueueManager() {
		timer = new Timer();
	} //Prevent initialization

	public static AnnouncementQueueManager getManager() {
		if (instance == null) {
			instance = new AnnouncementQueueManager();
		}
		return instance;
	}
	
	public void init() {
		//Start main queue
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					//Send to Announce to announce the events if accurate.
					ArrayList<AnnouncementQueueItem> toRemove = new ArrayList<>();
					for (AnnouncementQueueItem i : queue) {
						if (Announce.accurateAnnounce(i)) {
							//No matter what happened, if true, remove from queue.
							toRemove.add(i);
						}
					}
					
					//Remove announced items
					queue.removeAll(toRemove);
				} catch (Exception e) {
					ExceptionHandler.sendException(null, "Something failed with the accurate announcer. CODE: A101", e, this.getClass());
				}
			}
		}, 1000 * 60, 1000 * 60);
		
		//Start error handling queue
	}

	public void queue(Announcement a, Event e, GuildSettings s, CalendarData cd) {
		AnnouncementQueueItem item = new AnnouncementQueueItem(a, e);
		//Set the MS time to announce.
		Long eventMs = e.getStart().getDateTime().getValue();
		
		Long announcePreTime = Integer.toUnsignedLong(a.getMinutesBefore() + (a.getHoursBefore() * 60));
		
		Long announceTime = eventMs - announcePreTime;
		
		item.setTimeToAnnounceMs(announceTime);
		item.setSettings(s);
		item.setCalendarData(cd);

		queue.add(item);
	}
	
	public void dequeue(String eventId) {
		ArrayList<AnnouncementQueueItem> toRemove = new ArrayList<>();
		for (AnnouncementQueueItem i : queue) {
			if (i.getEvent().getId().equals(eventId)) {
				toRemove.add(i);
			}
		}
		
		queue.removeAll(toRemove);
	}
	
	public void dequeue(UUID announcementId) {
		ArrayList<AnnouncementQueueItem> toRemove = new ArrayList<>();
		for (AnnouncementQueueItem i : queue) {
			if (i.getAnnouncement().getAnnouncementId().equals(announcementId)) {
				toRemove.add(i);
			}
		}
		
		queue.removeAll(toRemove);
	}
	
	public void dequeue(long guildId) {
		ArrayList<AnnouncementQueueItem> toRemove = new ArrayList<>();
		for (AnnouncementQueueItem i : queue) {
			if (i.getGuildId() == guildId) {
				toRemove.add(i);
			}
		}
		
		queue.removeAll(toRemove);
	}
	
	public void update(Event e) {
		//Event changed, update info
		ArrayList<AnnouncementQueueItem> toRemove = new ArrayList<>();
		
		for (AnnouncementQueueItem i : queue) {
			if (i.getEvent().getId().equals(e.getId())) {
				//Check if event/announcement compatible still
				if (AnnouncementUtils.isCompatible(i.getAnnouncement(), e)) {
					Long eventMs = e.getStart().getDateTime().getValue();
					
					Long announcePreTime = Integer.toUnsignedLong(i.getAnnouncement().getMinutesBefore() + (i.getAnnouncement().getHoursBefore() * 60));
					
					Long announceTime = eventMs - announcePreTime;
					
					i.setTimeToAnnounceMs(announceTime);
					i.setEvent(e);
				} else {
					toRemove.add(i);
				}
			}
		}
		
		queue.removeAll(toRemove);
	}
	
	public void update(Announcement a) {
		//Announcement changed, update announcement.
		ArrayList<AnnouncementQueueItem> toRemove = new ArrayList<>();
		
		for (AnnouncementQueueItem i : queue) {
			if (i.getAnnouncement().getAnnouncementId().equals(a.getAnnouncementId())) {
				//Make sure changes don't break the announcement that is queued.
				if (AnnouncementUtils.isCompatible(a, i.getEvent())) {
					Long eventMs = i.getEvent().getStart().getDateTime().getValue();
					
					Long announcePreTime = Integer.toUnsignedLong(a.getMinutesBefore() + (a.getHoursBefore() * 60));
					
					Long announceTime = eventMs - announcePreTime;
					
					i.setTimeToAnnounceMs(announceTime);
					i.setAnnouncement(a);
				} else {
					toRemove.add(i);
				}
			}
		}
		
		queue.removeAll(toRemove);
	}
	
	public void update(GuildSettings settings) {
		//Guild's settings changed, update affected objects
		for (AnnouncementQueueItem i : queue) {
			if (i.getGuildId() == settings.getGuildID()) {
				i.setSettings(settings);
			}
		}
	}
}