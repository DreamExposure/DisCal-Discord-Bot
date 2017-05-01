package com.cloudcraftgaming.discal.module.announcement;

import java.util.Timer;

/**
 * Created by Nova Fox on 1/5/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class Announcer {
    private static Announcer instance;

    private final Timer timer;

    private Announcer() {
        timer = new Timer();
    }

    /**
     * Gets the instance of the Announcer
     * @return The instance of the Announcer
     */
    public static Announcer getAnnouncer() {
        if (instance == null) {
            instance = new Announcer();
        }
        return instance;
    }

    /**
     * Initiates the announcer and schedules the announcements.
     */
    public void init() {
        timer.schedule(new Announce(), 5 * 1000 * 60, 5 * 1000 * 60);
    }

    /**
     * Gracefully shuts down the announcer and closes all timer threads.
     */
    public void shutdown() {
        timer.cancel();
    }
}