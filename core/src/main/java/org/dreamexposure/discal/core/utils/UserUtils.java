package org.dreamexposure.discal.core.utils;

import java.util.ArrayList;
import java.util.List;

import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.rest.util.Snowflake;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Created by Nova Fox on 3/29/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
@SuppressWarnings("ConstantConditions")
public class UserUtils {

    public static Mono<Snowflake> getUserId(String toLookFor, Message m) {
        return m.getGuild().flatMap(g -> getUserId(toLookFor, g));
    }

    public static Mono<Snowflake> getUserId(String toLookFor, Guild guild) {
        return getUser(toLookFor, guild).map(Member::getId);
    }

    public static Mono<Member> getUser(String toLookFor, Guild guild) {
        if (toLookFor.isEmpty())
            return Mono.empty();

        toLookFor = GeneralUtils.trim(toLookFor);
        final String lower = toLookFor.toLowerCase();
        if (lower.matches("@!?[0-9]+") || lower.matches("[0-9]+")) {
            return guild.getMemberById(Snowflake.of(Long.parseLong(lower.replaceAll("[<@!>]", ""))))
                .onErrorResume(e -> Mono.empty()); //User not found, we don't care about the error in this case.
        }

        return guild.getMembers()
            .filter(m ->
                m.getUsername().equalsIgnoreCase(lower)
                    || m.getUsername().toLowerCase().contains(lower)
                    || (m.getUsername() + "#" + m.getDiscriminator()).equalsIgnoreCase(lower)
                    || m.getDiscriminator().equalsIgnoreCase(lower)
                    || m.getDisplayName().equalsIgnoreCase(lower)
                    || m.getDisplayName().toLowerCase().contains(lower)
            )
            .next()
            .onErrorResume(e -> Mono.empty()); //User not found, we don't care about the error in this case.
    }

    public static Mono<Member> getUserFromID(String id, Guild guild) {
        return Mono.just(id)
            .filter(s -> !s.isEmpty())
            .filter(s -> s.matches("[0-9]+"))
            .flatMap(s -> guild.getMemberById(Snowflake.of(s))
                .onErrorResume(e -> Mono.empty()));
    }

    public static Mono<List<Member>> getUsers(ArrayList<String> userIds, Guild guild) {
        return Flux.fromIterable(userIds)
            .filter(s -> !s.isEmpty())
            .filter(s -> s.matches("[0-9]+"))
            .flatMap(s -> getUserFromID(s, guild)
                .onErrorResume(e -> Mono.empty()))
            .collectList();
    }
}