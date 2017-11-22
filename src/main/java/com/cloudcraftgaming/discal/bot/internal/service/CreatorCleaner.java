package com.cloudcraftgaming.discal.bot.internal.service;

import com.cloudcraftgaming.discal.api.object.announcement.Announcement;
import com.cloudcraftgaming.discal.api.object.calendar.PreCalendar;
import com.cloudcraftgaming.discal.api.object.event.PreEvent;
import com.cloudcraftgaming.discal.api.utils.ExceptionHandler;
import com.cloudcraftgaming.discal.bot.internal.calendar.calendar.CalendarCreator;
import com.cloudcraftgaming.discal.bot.internal.calendar.event.EventCreator;
import com.cloudcraftgaming.discal.bot.module.announcement.AnnouncementCreator;

import java.util.TimerTask;

/**
 * Created by Nova Fox on 11/2/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
public class CreatorCleaner extends TimerTask {

	@Override
	public void run() {
		try {
			long target = 60 * 1000 * 60; //60 minutes

			//Run through calendar creator
			for (PreCalendar cal : CalendarCreator.getCreator().getAllPreCalendars()) {
				long difference = System.currentTimeMillis() - cal.getLastEdit();

				if (difference <= target) {
					//Last edited 60+ minutes ago, delete from creator and free up RAM.
					CalendarCreator.getCreator().terminate(cal.getGuildId());
				}
			}

			//Run through event creator
			for (PreEvent event : EventCreator.getCreator().getAllPreEvents()) {
				long difference = System.currentTimeMillis() - event.getLastEdit();

				if (difference <= target) {
					//Last edited 60+ minutes ago, delete from creator and free up RAM.
					EventCreator.getCreator().terminate(event.getGuildId());
				}
			}

			//Run through announcement creator
			for (Announcement an : AnnouncementCreator.getCreator().getAllAnnouncements()) {
				long difference = System.currentTimeMillis() - an.getLastEdit();

				if (difference <= target) {
					//Last edited 60+ minutes ago, delete from creator and free up RAM.
					AnnouncementCreator.getCreator().terminate(an.getGuildId());
				}
			}
		} catch (Exception e) {
			ExceptionHandler.sendException(null, "Error in CreatorCleaner", e, this.getClass());
		}
	}
}