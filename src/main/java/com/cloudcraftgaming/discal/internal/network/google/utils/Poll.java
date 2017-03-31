package com.cloudcraftgaming.discal.internal.network.google.utils;

import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

/**
 * Created by Nova Fox on 3/24/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class Poll {
    private final IUser user;
    private final IGuild guild;

    private int interval;
    private int expires_in;
    private int remainingSeconds;
    private String device_code;

    public Poll(IUser _user, IGuild _guild) {
        user = _user;
        guild = _guild;
    }

    //Getters
    public IUser getUser() {
        return user;
    }

    public IGuild getGuild() {
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