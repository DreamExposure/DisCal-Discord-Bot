package com.cloudcraftgaming.discal.bot.module.announcement;

import com.cloudcraftgaming.discal.api.enums.announcement.AnnouncementType;

import java.util.Timer;
import java.util.TimerTask;

public class AnnouncementThreader {
	private static AnnouncementThreader threader;

	private Timer timer;

	private AnnouncementThreader() {
	}

	public static AnnouncementThreader getThreader() {
		if (threader == null)
			threader = new AnnouncementThreader();

		return threader;
	}

	public void init() {
		timer = new Timer(true);

		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				for (AnnouncementType t: AnnouncementType.values()) {
					AnnouncementThread at = new AnnouncementThread(t);
					at.setDaemon(true);
					at.run();
				}
			}
		}, 5 * 1000 * 60, 5 * 1000 * 60);
	}

	public void shutdown() {
		timer.cancel();
	}
}