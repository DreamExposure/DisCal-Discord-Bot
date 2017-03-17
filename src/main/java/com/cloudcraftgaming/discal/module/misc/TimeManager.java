package com.cloudcraftgaming.discal.module.misc;

import java.util.Timer;

/**
 * Created by Nova Fox on 3/5/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class TimeManager {
    private static TimeManager instance;

    private Timer timer;

    private TimeManager() {} //Prevent initialization

    public static TimeManager getManager() {
        if (instance == null) {
            instance = new TimeManager();
        }
        return instance;
    }

    public void init() {
        timer = new Timer();
        timer.schedule(new StatusChanger(), 10 * 1000, 10 * 1000);
    }

    public void shutdown() {
        timer.cancel();
    }
}