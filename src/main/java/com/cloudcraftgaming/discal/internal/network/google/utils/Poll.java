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

    //Setters
    public void setInterval(int _interval) {
        interval = _interval;
    }
}