package org.dreamexposure.discal.core.utils;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.rest.util.Permission;
import reactor.core.publisher.Mono;

/**
 * Created by Nova Fox on 1/19/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
@Deprecated
public class PermissionChecker {
    public static Mono<Boolean> hasManageServerRole(final MessageCreateEvent event) {
        return Mono.justOrEmpty(event.getMember())
            .flatMap(Member::getBasePermissions)
            .map(perms -> perms.contains(Permission.MANAGE_GUILD)
                || perms.contains(Permission.ADMINISTRATOR))
            .defaultIfEmpty(false);
    }
}
