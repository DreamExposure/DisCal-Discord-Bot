package com.cloudcraftgaming.discal.internal.network.google;

import com.cloudcraftgaming.discal.internal.network.google.utils.Poll;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Nova Fox on 3/24/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class PollManager {
    private static PollManager instance;

    private Timer timer;

    //Prevent initialization.
    private PollManager() {
        //Use daemon because this is a util timer and there is no reason to keep the program running when this is polling Google, just assume it timed out and re-auth if all else fails.
        timer = new Timer(true);
    }

    public static PollManager getManager() {
        if (instance == null) {
            instance = new PollManager();
        }
        return instance;
    }

    //Timer methods.
    public void scheduleNextPoll(Poll poll) {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                //TODO: Call poller.
            }
        }, 1000 * poll.getInterval());
    }
}