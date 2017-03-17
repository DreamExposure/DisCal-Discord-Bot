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

    public static Announcer getAnnouncer() {
        if (instance == null) {
            instance = new Announcer();
        }
        return instance;
    }

    public void init() {
        timer.schedule(new Announce(), 10 * 1000 * 60, 10 * 1000 * 60);
    }

    public void shutdown() {
        timer.cancel();
    }
}