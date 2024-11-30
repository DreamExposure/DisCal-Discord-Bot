package org.dreamexposure.discal.core.extensions.discord4j

import discord4j.discordjson.json.RoleData
import discord4j.rest.entity.RestMember
import discord4j.rest.util.Permission
import discord4j.rest.util.PermissionSet
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.function.Predicate

@Deprecated("Prefer to use PermissionService implementation")
fun RestMember.hasPermissions(pred: Predicate<PermissionSet>): Mono<Boolean> {
    return this.guild().data.flatMap { guildData ->
        if (guildData.ownerId().asLong() == this.id.asLong()) {
            Mono.just(true)
        } else {
            this.data.flatMap { memberData ->
                Flux.fromIterable(guildData.roles())
                    .filter { memberData.roles().contains(it.id()) }
                    .map(RoleData::permissions)
                    .reduce(0L) { perm: Long, accumulator: Long -> accumulator or perm }
                    .map(PermissionSet::of)
                    .map(pred::test)
            }
        }
    }
}

@Deprecated("Prefer to use PermissionService implementation")
fun RestMember.hasElevatedPermissions(): Mono<Boolean> {
    return hasPermissions() {
        it.contains(Permission.MANAGE_GUILD) || it.contains(Permission.ADMINISTRATOR)
    }
}
