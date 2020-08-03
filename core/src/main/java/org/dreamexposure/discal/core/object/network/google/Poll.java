package org.dreamexposure.discal.core.object.network.google;

import org.dreamexposure.discal.core.object.GuildSettings;

import discord4j.core.object.entity.User;

/**
 * Created by Nova Fox on 11/10/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
public class Poll {
    private final User user;
    private final GuildSettings settings;

    private int interval;
    private int expires_in;
    private int remainingSeconds;
    private String device_code;

    public Poll(final User user, final GuildSettings settings) {
        this.user = user;
        this.settings = settings;
    }

    //Getters
    public User getUser() {
        return this.user;
    }

    public GuildSettings getSettings() {
        return this.settings;
    }

    public int getInterval() {
        return this.interval;
    }

    public int getExpires_in() {
        return this.expires_in;
    }

    public int getRemainingSeconds() {
        return this.remainingSeconds;
    }

    public String getDevice_code() {
        return this.device_code;
    }

    //Setters
    public void setInterval(final int _interval) {
        this.interval = _interval;
    }

    public void setExpires_in(final int _expiresIn) {
        this.expires_in = _expiresIn;
    }

    public void setRemainingSeconds(final int _remainingSeconds) {
        this.remainingSeconds = _remainingSeconds;
    }

    public void setDevice_code(final String _deviceCode) {
        this.device_code = _deviceCode;
    }
}