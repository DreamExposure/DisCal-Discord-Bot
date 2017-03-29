package com.cloudcraftgaming.discal.utils;

import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IUser;

/**
 * Created by Nova Fox on 3/29/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class UserUtils {
    public static IUser getUserFromMention(String mention, MessageReceivedEvent event) {
        for (IUser u : event.getMessage().getGuild().getUsers()) {
            if (mention.equalsIgnoreCase("<@" + u.getID() + ">") || mention.equalsIgnoreCase("<@!" + u.getID() + ">")) {
                return u;
            }
        }

        return null;
    }
}