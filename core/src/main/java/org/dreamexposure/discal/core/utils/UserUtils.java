package org.dreamexposure.discal.core.utils;

import java.util.List;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Created by Nova Fox on 3/29/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class UserUtils {

    public static Mono<Snowflake> getUserId(final String toLookFor, final Message message) {
        return message.getGuild().flatMap(g -> getUserId(toLookFor, g));
    }

    public static Mono<Snowflake> getUserId(final String toLookFor, final Guild guild) {
        return getUser(toLookFor, guild).map(Member::getId);
    }

    public static Mono<Member> getUser(String toLookFor, final Guild guild) {
        if (toLookFor.isEmpty())
            return Mono.empty();

        toLookFor = GeneralUtils.trim(toLookFor);
        final String lower = toLookFor.toLowerCase();
        if (lower.matches("@!?[0-9]+") || lower.matches("[0-9]+")) {
            return guild.getMemberById(Snowflake.of(Long.parseLong(lower.replaceAll("[<@!>]", ""))))
                .onErrorResume(e -> Mono.empty()); //User not found, we don't care about the error in this case.
        }

        return guild.getMembers()
            .filter(member ->
                member.getUsername().equalsIgnoreCase(lower)
                    || member.getUsername().toLowerCase().contains(lower)
                    || (member.getUsername() + "#" + member.getDiscriminator()).equalsIgnoreCase(lower)
                    || member.getDiscriminator().equalsIgnoreCase(lower)
                    || member.getDisplayName().equalsIgnoreCase(lower)
                    || member.getDisplayName().toLowerCase().contains(lower)
            )
            .next()
            .onErrorResume(e -> Mono.empty()); //User not found, we don't care about the error in this case.
    }

    public static Mono<Member> getUserFromID(final String id, final Guild guild) {
        return Mono.just(id)
            .filter(s -> !s.isEmpty())
            .filter(s -> s.matches("[0-9]+"))
            .flatMap(s -> guild.getMemberById(Snowflake.of(s))
                .onErrorResume(e -> Mono.empty()));
    }

    public static Mono<List<Member>> getUsers(final List<String> userIds, final Guild guild) {
        return Flux.fromIterable(userIds)
            .filter(s -> !s.isEmpty())
            .filter(s -> s.matches("[0-9]+"))
            .flatMap(s -> getUserFromID(s, guild)
                .onErrorResume(e -> Mono.empty()))
            .collectList();
    }
}