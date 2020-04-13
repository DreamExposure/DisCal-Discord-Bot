package org.dreamexposure.discal.core.object.network.google;

import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;

/**
 * Created by Nova Fox on 11/10/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
public class Poll {
    private final User user;
    private final Guild guild;

    private int interval;
    private int expires_in;
    private int remainingSeconds;
    private String device_code;

    public Poll(User _user, Guild _guild) {
        user = _user;
        guild = _guild;
    }

    //Getters
    public User getUser() {
        return user;
    }

    public Guild getGuild() {
        return guild;
    }

    public int getInterval() {
        return interval;
    }

    public int getExpires_in() {
        return expires_in;
    }

    public int getRemainingSeconds() {
        return remainingSeconds;
    }

    public String getDevice_code() {
        return device_code;
    }

    //Setters
    public void setInterval(int _interval) {
        interval = _interval;
    }

    public void setExpires_in(int _expiresIn) {
        expires_in = _expiresIn;
    }

    public void setRemainingSeconds(int _remainingSeconds) {
        remainingSeconds = _remainingSeconds;
    }

    public void setDevice_code(String _deviceCode) {
        device_code = _deviceCode;
    }
}