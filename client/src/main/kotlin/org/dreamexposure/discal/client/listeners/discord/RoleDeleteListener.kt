package org.dreamexposure.discal.client.listeners.discord

import discord4j.common.util.Snowflake
import discord4j.core.event.domain.role.RoleDeleteEvent
import org.dreamexposure.discal.core.database.DatabaseManager
import reactor.core.publisher.Mono

object RoleDeleteListener {

    fun handle(event: RoleDeleteEvent): Mono<Void> {
        val updateRsvps = DatabaseManager.removeRsvpRole(event.guildId, event.roleId)

        val updateControlRole = DatabaseManager.getSettings(event.guildId)
              .filter { !"everyone".equals(it.controlRole, true) }
              .filter { event.roleId == Snowflake.of(it.controlRole) }
              .doOnNext { it.controlRole = "everyone" }
              .flatMap(DatabaseManager::updateSettings)

        return Mono.`when`(updateRsvps, updateControlRole)
    }
}
