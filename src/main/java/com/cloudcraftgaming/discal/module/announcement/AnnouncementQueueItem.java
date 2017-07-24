package com.cloudcraftgaming.discal.module.announcement;

import com.google.api.services.calendar.model.Event;

/**
 * Created by Nova Fox on 7/23/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class AnnouncementQueueItem {
	private final Announcement announcement;
	
	private Event event;
	private long timeToAnnounceMs;
	
	public AnnouncementQueueItem(Announcement _announcement, Event _event) {
		announcement = _announcement;
		event = _event;
		
		timeToAnnounceMs = event.getStart().getDateTime().getTimeZoneShift();
	}
	
	//Getters
	public Announcement getAnnouncement() {
		return announcement;
	}
	
	public Event getEvent() {
		return event;
	}
	
	public long getTimeToAnnounceMs() {
		return timeToAnnounceMs;
	}
	
	//Setters
	public void setTimeToAnnounceMs(long _time) {
		timeToAnnounceMs = _time;
	}
}