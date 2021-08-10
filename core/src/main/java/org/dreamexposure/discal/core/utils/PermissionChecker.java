package org.dreamexposure.discal.core.utils;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.channel.Channel;
import discord4j.discordjson.json.GuildUpdateData;
import discord4j.discordjson.json.MemberData;
import discord4j.discordjson.json.RoleData;
import discord4j.rest.entity.RestGuild;
import discord4j.rest.entity.RestMember;
import discord4j.rest.util.Permission;
import discord4j.rest.util.PermissionSet;
import org.dreamexposure.discal.core.object.BotSettings;
import org.dreamexposure.discal.core.object.GuildSettings;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Predicate;

/**
 * Created by Nova Fox on 1/19/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
@Deprecated
public class PermissionChecker {
    public static Mono<Boolean> hasDisCalRole(final MessageCreateEvent event, final GuildSettings settings) {
        if ("everyone".equalsIgnoreCase(settings.getControlRole()))
            return Mono.just(true);
        if (Snowflake.of(settings.getControlRole()).equals(settings.getGuildID())) //also everyone
            return Mono.just(true);

        final Mono<Member> member = Mono.justOrEmpty(event.getMember());

        //User doesn't need bot control role if they have admin permissions.
        return member.flatMap(Member::getBasePermissions)
            .map(perms ->
                perms.contains(Permission.ADMINISTRATOR)
                    || perms.contains(Permission.MANAGE_GUILD)
            ).flatMap(hasAdmin -> {
                if (hasAdmin) {
                    return Mono.just(true);
                } else {
                    return member.flatMapMany(Member::getRoles)
                        .map(Role::getId)
                        .any(id -> id.equals(Snowflake.of(settings.getControlRole())));
                }
            });
    }

    public static Mono<Boolean> hasDisCalRole(final Member member, final GuildSettings settings) {
        if ("everyone".equalsIgnoreCase(settings.getControlRole()))
            return Mono.just(true);
        if (Snowflake.of(settings.getControlRole()).equals(settings.getGuildID())) //also everyone
            return Mono.just(true);

        //User doesn't need bot control role if they have admin permissions.
        final Mono<Boolean> hasAdmin = Mono.just(member).flatMap(Member::getBasePermissions).map(perms ->
            perms.contains(Permission.ADMINISTRATOR) || perms.contains(Permission.MANAGE_GUILD));

        return hasAdmin.flatMap(has -> {
            if (has) {
                return Mono.just(true);
            } else {
                return Mono.just(member).flatMapMany(Member::getRoles)
                    .map(Role::getId)
                    .any(id -> id.equals(Snowflake.of(settings.getControlRole())));
            }
        });
    }

    @Deprecated
    public static Mono<Boolean> hasSufficientRole(final MessageCreateEvent event, final GuildSettings settings) {
        if ("everyone".equalsIgnoreCase(settings.getControlRole()))
            return Mono.just(true);
        if (Snowflake.of(settings.getControlRole()).equals(settings.getGuildID())) //also everyone
            return Mono.just(true);

        return Mono.justOrEmpty(event.getMember())
            .flatMapMany(Member::getRoles)
            .map(Role::getId)
            .any(snowflake -> snowflake.equals(Snowflake.of(settings.getControlRole())));
    }

    @Deprecated
    public static Mono<Boolean> hasSufficientRole(final Member member, final GuildSettings settings) {
        if ("everyone".equalsIgnoreCase(settings.getControlRole()))
            return Mono.just(true);
        if (Snowflake.of(settings.getControlRole()).equals(settings.getGuildID())) //also everyone
            return Mono.just(true);

        return Mono.from(member.getRoles()
            .map(Role::getId)
            .any(snowflake -> snowflake.equals(Snowflake.of(settings.getControlRole())))
        );
    }

    public static Mono<Boolean> hasSufficientRole(final RestMember member, final GuildSettings settings) {
        if ("everyone".equalsIgnoreCase(settings.getControlRole()))
            return Mono.just(true);
        if (Snowflake.of(settings.getControlRole()).equals(settings.getGuildID())) //also everyone
            return Mono.just(true);

        return member.getData()
            .map(MemberData::roles)
            .map(roles -> roles.contains(settings.getControlRole()));
    }

    public static Mono<Boolean> hasManageServerRole(final MessageCreateEvent event) {
        return Mono.justOrEmpty(event.getMember())
            .flatMap(Member::getBasePermissions)
            .map(perms -> perms.contains(Permission.MANAGE_GUILD)
                || perms.contains(Permission.ADMINISTRATOR))
            .defaultIfEmpty(false);
    }

    public static Mono<Boolean> hasManageServerRole(final Member m) {
        return m.getBasePermissions()
            .map(perms -> perms.contains(Permission.MANAGE_GUILD)
                || perms.contains(Permission.ADMINISTRATOR)
            );
    }

    public static Mono<Boolean> hasManageServerRole(final RestMember m, final RestGuild g) {
        return hasPermissions(m, g, permissions ->
            permissions.contains(Permission.MANAGE_GUILD)
                || permissions.contains(Permission.ADMINISTRATOR));
    }

    public static Mono<Boolean> hasPermissions(final RestMember m, final RestGuild g,
                                               final Predicate<PermissionSet> pred) {
        return m.getData().flatMap(memberData ->
            g.getData().map(GuildUpdateData::roles)
                .flatMapMany(Flux::fromIterable)
                .filter(roleData -> memberData.roles().contains(roleData.id()))
                .map(RoleData::permissions)
                .reduce(0L, (perm, accumulator) -> accumulator | perm)
                .map(PermissionSet::of)
                .map(pred::test)
        );
    }

    public static Mono<Boolean> isCorrectChannel(final MessageCreateEvent event, final GuildSettings settings) {
        if ("all".equalsIgnoreCase(settings.getDiscalChannel()))
            return Mono.just(true);

        return Mono.from(event.getMessage().getChannel()
            .map(Channel::getId)
            .map(snowflake -> snowflake.equals(Snowflake.of(settings.getDiscalChannel())))
            .onErrorResume(e -> Mono.just(true)) //If channel not found, allow.
        );
    }

    public static Mono<Boolean> botHasMessageManagePerms(final MessageCreateEvent event) {
        return event.getGuild()
            .flatMap(guild -> guild.getMemberById(Snowflake.of(BotSettings.ID.get()))
                .flatMap(Member::getBasePermissions)
                .map(perms -> perms.contains(Permission.MANAGE_MESSAGES))
            );
    }
}
