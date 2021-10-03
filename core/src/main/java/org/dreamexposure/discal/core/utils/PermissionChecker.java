package org.dreamexposure.discal.core.utils;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Role;
import discord4j.rest.util.Permission;
import org.dreamexposure.discal.core.object.GuildSettings;
import reactor.core.publisher.Mono;

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

    public static Mono<Boolean> hasManageServerRole(final MessageCreateEvent event) {
        return Mono.justOrEmpty(event.getMember())
            .flatMap(Member::getBasePermissions)
            .map(perms -> perms.contains(Permission.MANAGE_GUILD)
                || perms.contains(Permission.ADMINISTRATOR))
            .defaultIfEmpty(false);
    }
}
