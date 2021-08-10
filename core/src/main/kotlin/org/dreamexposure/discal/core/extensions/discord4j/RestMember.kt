package org.dreamexposure.discal.core.extensions.discord4j

import discord4j.common.util.Snowflake
import discord4j.discordjson.Id
import discord4j.discordjson.json.GuildUpdateData
import discord4j.discordjson.json.MemberData
import discord4j.discordjson.json.RoleData
import discord4j.rest.entity.RestMember
import discord4j.rest.util.Permission
import discord4j.rest.util.PermissionSet
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.function.Predicate

fun RestMember.hasPermissions(pred: Predicate<PermissionSet>): Mono<Boolean> {
    return this.data.flatMap { memberData ->
        this.guild().data.map(GuildUpdateData::roles)
              .flatMapMany { Flux.fromIterable(it) }
              .filter { memberData.roles().contains(it.id()) }
              .map(RoleData::permissions)
              .reduce(0L) { perm: Long, accumulator: Long -> accumulator or perm }
              .map(PermissionSet::of)
              .map(pred::test)
    }
}

fun RestMember.hasElevatedPermissions(): Mono<Boolean> {
    return hasPermissions() {
        it.contains(Permission.MANAGE_GUILD) || it.contains(Permission.ADMINISTRATOR)
    }
}

fun RestMember.hasControlRole(): Mono<Boolean> {
    return this.guild().getSettings().flatMap { settings ->
        if (settings.controlRole.equals("everyone", true))
            return@flatMap Mono.just(true)

        if (Snowflake.of(settings.controlRole).equals(settings.guildID)) // Also everyone (older guilds)
            return@flatMap Mono.just(true)

        this.data
              .map(MemberData::roles)
              .flatMapMany { Flux.fromIterable(it) }
              .map(Id::asString)
              .collectList()
              .map { it.contains(settings.controlRole) }
    }
}
