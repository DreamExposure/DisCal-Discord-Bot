package com.cloudcraftgaming.discal.module.announcement;

import com.cloudcraftgaming.discal.internal.data.CalendarData;
import com.cloudcraftgaming.discal.internal.data.GuildSettings;
import com.google.api.services.calendar.model.Event;

/**
 * Created by Nova Fox on 7/23/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class AnnouncementQueueItem {
	private final long guildId;
	
	private  Announcement announcement;
	private Event event;
	private GuildSettings settings;
	private CalendarData calendarData;
	private long timeToAnnounceMs;
	
	private int timesErrored;

	public AnnouncementQueueItem(Announcement _announcement, Event _event) {
		announcement = _announcement;
		guildId = announcement.getGuildId();
		event = _event;
	}

	//Getters
	public Announcement getAnnouncement() {
		return announcement;
	}
	
	public long getGuildId() {
		return guildId;
	}

	public Event getEvent() {
		return event;
	}
	
	public GuildSettings getSettings() {
		return settings;
	}
	
	public CalendarData getData() {
		return calendarData;
	}
	
	public long getTimeToAnnounceMs() {
		return timeToAnnounceMs;
	}
	
	public int getTimesErrored() {
		return timesErrored;
	}

	//Setters
	public void setAnnouncement(Announcement a) {
		announcement = a;
	}
	
	public void setEvent(Event e) {
		event = e;
	}
	
	public void setSettings(GuildSettings _settings) {
		settings = _settings;
	}
	
	public void setCalendarData(CalendarData _data) {
		calendarData = _data;
	}

	public void setTimeToAnnounceMs(long _time) {
		timeToAnnounceMs = _time;
	}
	
	public void setTimesErrored(int _times) {
		timesErrored = _times;
	}
}