package com.cloudcraftgaming.discal.module.announcement;

import com.cloudcraftgaming.discal.internal.service.AnnouncementQueueManager;
import com.cloudcraftgaming.discal.utils.ExceptionHandler;

import java.util.ArrayList;
import java.util.TimerTask;

/**
 * Created by Nova Fox on 9/21/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
public class AnnouncementQueueMainTimer extends TimerTask {
	@Override
	public void run() {
		try {
			//Send to Announce to announce the events if accurate.
			ArrayList<AnnouncementQueueItem> toRemove = new ArrayList<>();
			for (AnnouncementQueueItem i : AnnouncementQueueManager.getManager().queue) {
				if (Announce.accurateAnnounce(i)) {
					//No matter what happened, if true, remove from queue.
					toRemove.add(i);
				}
			}

			//Remove announced items
			AnnouncementQueueManager.getManager().queue.removeAll(toRemove);
		} catch (Exception e) {
			ExceptionHandler.sendException(null, "Something failed with the accurate announcer. CODE: A101", e, this.getClass());
		}
	}
}