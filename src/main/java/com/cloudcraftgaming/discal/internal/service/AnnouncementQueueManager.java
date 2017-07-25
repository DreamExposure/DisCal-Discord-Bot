package com.cloudcraftgaming.discal.internal.service;

import com.cloudcraftgaming.discal.module.announcement.Announcement;
import com.cloudcraftgaming.discal.module.announcement.AnnouncementQueueItem;
import com.google.api.services.calendar.model.Event;

import java.util.ArrayList;

/**
 * Created by Nova Fox on 7/23/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class AnnouncementQueueManager {
	private static AnnouncementQueueManager instance;

	private ArrayList<AnnouncementQueueItem> queue = new ArrayList<>();

	private AnnouncementQueueManager() {} //Prevent initialization

	public static AnnouncementQueueManager getManager() {
		if (instance == null) {
			instance = new AnnouncementQueueManager();
		}

		return instance;
	}

	public void queue(Announcement a, Event e) {
		AnnouncementQueueItem item = new AnnouncementQueueItem(a, e);
		//TODO: Set the MS time to announce.

		queue.add(item);
	}
}