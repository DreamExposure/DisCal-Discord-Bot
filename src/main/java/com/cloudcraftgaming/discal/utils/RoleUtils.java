package com.cloudcraftgaming.discal.utils;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Nova Fox on 3/29/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class RoleUtils {
    public static IRole getRoleFromMention(String mention, MessageReceivedEvent event) {
        for (IRole r : event.getMessage().getGuild().getRoles()) {
            if (mention.equalsIgnoreCase("<@&" + r.getStringID() + ">") || mention.equalsIgnoreCase("<@&!" + r.getStringID() + ">")) {
                return r;
            }
        }
        return null;
    }

    public static IRole getRoleFromID(String id, MessageReceivedEvent event) {
        for (IRole r : event.getMessage().getGuild().getRoles()) {
            if (id.equals(r.getStringID()) || id.equals(r.getName())) {
                return r;
            }
        }
        return null;
    }

    public static boolean roleExists(String id, MessageReceivedEvent event) {
        for (IRole r : event.getMessage().getGuild().getRoles()) {
            if (id.equals(r.getStringID())) {
                return true;
            }
        }
        return false;
    }

    public static String getRoleNameFromID(String id, MessageReceivedEvent event) {
        IRole role = getRoleFromID(id, event);
        if (role != null) {
            return role.getName();
        } else {
            return "ERROR";
        }
    }

    public static long getRole(String toLookFor, IMessage m) {
        toLookFor = toLookFor.trim();
        final String lower = toLookFor.toLowerCase();
        long res = 0;

        if (!m.getRoleMentions().isEmpty()) {
            res = m.getRoleMentions().get(0).getLongID();
        }

        List<IRole> roles = m.getGuild().getRoles().stream().filter(r -> r.getName().toLowerCase().contains(lower) || r.getName().equalsIgnoreCase(lower) || r.getStringID().equals(lower)).collect(Collectors.toList());
        if (res == 0) {
            if (!roles.isEmpty()) {
                res = roles.get(0).getLongID();
            }
        }

        return res;
    }
}