package org.dreamexposure.discal.client.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Timer;

/**
 * Created by Nova Fox on 3/5/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class TimeManager {
    private static TimeManager instance;

    private final Collection<Timer> timers = new ArrayList<>();

    private TimeManager() {
    } //Prevent initialization

    /**
     * Gets the instance of the TimeManager that is loaded.
     *
     * @return The instance of the TimeManager.
     */
    public static TimeManager getManager() {
        if (instance == null)
            instance = new TimeManager();

        return instance;
    }

    /**
     * Initializes the TimeManager and schedules the appropriate Timers.
     */
    public void init() {
        final Timer cc = new Timer(true);
        cc.schedule(new CreatorCleaner(), Duration.ofHours(1).toMillis(), Duration.ofHours(1).toMillis());
        this.timers.add(cc);
    }

    /**
     * Gracefully shuts down the TimeManager and exits all timer threads preventing errors.
     */
    public void shutdown() {
        for (final Timer t : this.timers) {
            t.cancel();
        }
    }
}
