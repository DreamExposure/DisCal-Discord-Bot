package com.cloudcraftgaming.discal.utils;

import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

import java.util.List;
import java.util.stream.Collectors;

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


    /**
     * Gets a user on the guild
     *
     * @param toLookFor The name or ID, if the user was mentioned this can be anything
     * @param m         The message, incase of mention
     * @return The ID of the user found.
     */
    public static String getUser(String toLookFor, IMessage m) {
        toLookFor = toLookFor.trim();
        final String lower = toLookFor.toLowerCase();

        String res = "";

        if (!m.getMentions().isEmpty())
            res = m.getMentions().get(0).getID();

        List<IUser> users = m.getGuild().getUsers().stream()
                .filter(u -> u.getName().toLowerCase().contains(lower)
                        || u.getName().equalsIgnoreCase(lower) || u.getID().equals(lower)
                        || u.getDisplayName(m.getGuild()).toLowerCase().contains(lower)
                        || u.getDisplayName(m.getGuild()).equalsIgnoreCase(lower))
                .collect(Collectors.toList());
        if (!users.isEmpty())
            res = users.get(0).getID();

        return res;
    }
}