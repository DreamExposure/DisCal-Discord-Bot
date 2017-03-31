package com.cloudcraftgaming.discal.utils;

import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IRole;

/**
 * Created by Nova Fox on 3/29/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class RoleUtils {
    public static IRole getRoleFromMention(String mention, MessageReceivedEvent event) {
        for (IRole r : event.getMessage().getGuild().getRoles()) {
            if (mention.equalsIgnoreCase("<@&" + r.getID() + ">") || mention.equalsIgnoreCase("<@&!" + r.getID() + ">")) {
                return r;
            }
        }
        return null;
    }
}