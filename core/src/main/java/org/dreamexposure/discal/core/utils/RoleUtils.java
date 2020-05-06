package org.dreamexposure.discal.core.utils;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.Role;
import discord4j.rest.util.Snowflake;
import reactor.core.publisher.Mono;

/**
 * Created by Nova Fox on 3/29/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
@SuppressWarnings("ConstantConditions")
public class RoleUtils {
    public static Mono<Role> getRoleFromID(String id, MessageCreateEvent event) {
        return event.getGuild()
            .flatMapMany(Guild::getRoles)
            .filter(r -> id.equals(r.getId().asString()) || id.equalsIgnoreCase(r.getName()))
            .next();
    }

    public static Mono<Role> getRoleFromID(String id, Guild guild) {
        return Mono.just(id)
            .filter(s -> !s.isEmpty())
            .filter(s -> s.matches("[0-9]+"))
            .flatMap(s -> guild.getRoleById(Snowflake.of(id))
                .onErrorResume(e -> Mono.empty()));
    }

    public static Mono<Boolean> roleExists(String id, MessageCreateEvent event) {
        return getRoleFromID(id, event).hasElement();
    }

    public static Mono<String> getRoleNameFromID(String id, MessageCreateEvent event) {
        return getRoleFromID(id, event).map(Role::getName);
    }

    public static Mono<Snowflake> getRoleId(String toLookFor, Message m) {
        return m.getGuild().flatMap(g -> getRoleId(toLookFor, g));
    }

    public static Mono<Snowflake> getRoleId(String toLookFor, Guild guild) {
        toLookFor = GeneralUtils.trim(toLookFor);
        final String lower = toLookFor.toLowerCase();
        if (lower.matches("@&[0-9]+") || lower.matches("[0-9]+")) {
            return guild.getRoleById(Snowflake
                .of(Long.parseLong(toLookFor.replaceAll("[<@&>]", ""))))
                .map(Role::getId)
                .onErrorResume(e -> Mono.empty());
        }

        return guild.getRoles()
            .filter(r ->
                r.getName().equalsIgnoreCase(lower)
                    || r.getName().toLowerCase().contains(lower)
            )
            .next()
            .map(Role::getId);
    }

    public static Mono<Role> getRole(String toLookFor, Message m) {
        return m.getGuild().flatMap(g -> getRole(toLookFor, g));
    }

    public static Mono<Role> getRole(String toLookFor, Guild guild) {
        toLookFor = GeneralUtils.trim(toLookFor);
        final String lower = toLookFor.toLowerCase();
        if (lower.matches("@&[0-9]+") || lower.matches("[0-9]+")) {
            return guild.getRoleById(Snowflake
                .of(Long.parseLong(toLookFor.replaceAll("[<@&>]", ""))))
                .onErrorResume(e -> Mono.empty());
        }

        return guild.getRoles()
            .filter(r ->
                r.getName().equalsIgnoreCase(lower)
                    || r.getName().toLowerCase().contains(lower)
            )
            .next()
            .onErrorResume(e -> Mono.empty()); //Role not found, we don't really care about the error
    }
}