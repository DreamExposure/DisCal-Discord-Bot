package com.cloudcraftgaming.discal.internal.service;

import com.cloudcraftgaming.discal.internal.network.discordpw.TimedUpdate;
import com.cloudcraftgaming.discal.module.announcement.AnnouncementTask;
import com.cloudcraftgaming.discal.module.misc.StatusChanger;

import java.util.ArrayList;
import java.util.Timer;

/**
 * Created by Nova Fox on 3/5/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class TimeManager {
    private static TimeManager instance;

	private final ArrayList<Timer> timers = new ArrayList<>();

	private TimeManager() {
	} //Prevent initialization

    /**
     * Gets the instance of the TimeManager that is loaded.
     * @return The instance of the TimeManager.
     */
    public static TimeManager getManager() {
        if (instance == null) {
            instance = new TimeManager();
        }
        return instance;
    }

    /**
     * Initializes the TimeManager and schedules the appropriate Timers.
     */
    public void init() {
		Timer timer = new Timer(true);
        timer.schedule(new StatusChanger(), 10 * 1000, 10 * 1000);
        timer.schedule(new TimedUpdate(), 60 * 60 * 1000, 60 * 60 * 1000);

		timers.add(timer);

		Timer at = new Timer(true);
		at.schedule(new AnnouncementTask(), 5 * 1000 * 60, 5 * 1000 * 60);
		timers.add(at);
    }

    /**
     * Gracefully shuts down the TimeManager and exits all timer threads preventing errors.
     */
    public void shutdown() {
		for (Timer t : timers) {
			t.cancel();
		}
    }
}