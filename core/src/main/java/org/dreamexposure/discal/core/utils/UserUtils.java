package org.dreamexposure.discal.core.utils;

import java.util.ArrayList;
import java.util.List;

import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.rest.util.Snowflake;
import reactor.core.publisher.Mono;

/**
 * Created by Nova Fox on 3/29/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
@SuppressWarnings("ConstantConditions")
public class UserUtils {

    public static Snowflake getUser(String toLookFor, Message m) {
        return getUser(toLookFor, m.getGuild().block());
    }

    /**
     * Grabs a user from a string
     *
     * @param toLookFor The String to look with
     * @param guild     The guild
     * @return The user if found, null otherwise
     */
    public static Snowflake getUser(String toLookFor, Guild guild) {
        toLookFor = GeneralUtils.trim(toLookFor);
        final String lower = toLookFor.toLowerCase();
        if (lower.matches("@!?[0-9]+") || lower.matches("[0-9]+")) {
            Member exists = guild.getMemberById(Snowflake.of(Long.parseLong(toLookFor.replaceAll("[<@!>]", "")))).onErrorResume(e -> Mono.empty()).block();
            if (exists != null)
                return exists.getId();
        }


        List<Member> users = new ArrayList<>();

        users.addAll(guild.getMembers().filter(m -> m.getUsername().equalsIgnoreCase(lower)).collectList().block());
        users.addAll(guild.getMembers().filter(m -> m.getUsername().toLowerCase().contains(lower)).collectList().block());
        users.addAll(guild.getMembers().filter(m -> (m.getUsername() + "#" + m.getDiscriminator()).equalsIgnoreCase(lower)).collectList().block());
        users.addAll(guild.getMembers().filter(m -> m.getDiscriminator().equalsIgnoreCase(lower)).collectList().block());
        users.addAll(guild.getMembers().filter(m -> m.getDisplayName().equalsIgnoreCase(lower)).collectList().block());
        users.addAll(guild.getMembers().filter(m -> m.getDisplayName().toLowerCase().contains(lower)).collectList().block());


        if (!users.isEmpty())
            return users.get(0).getId();

        return null;
    }

    private static Member getUserFromID(String id, Guild guild) {
        try {
            return guild.getMemberById(Snowflake.of(Long.parseUnsignedLong(id))).block();
        } catch (Exception e) {
            //Ignore. Probably invalid ID.
            return null;
        }
    }

    public static ArrayList<Member> getUsers(ArrayList<String> userIds, Guild guild) {
        ArrayList<Member> users = new ArrayList<>();
        for (String u : userIds) {
            Member user = getUserFromID(u, guild);
            if (user != null)
                users.add(user);
        }
        return users;
    }
}