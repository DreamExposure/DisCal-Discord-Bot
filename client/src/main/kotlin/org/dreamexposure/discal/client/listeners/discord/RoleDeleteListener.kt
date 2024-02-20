package org.dreamexposure.discal.client.listeners.discord

import discord4j.common.util.Snowflake
import discord4j.core.event.domain.role.RoleDeleteEvent
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.dreamexposure.discal.core.business.RsvpService
import org.dreamexposure.discal.core.database.DatabaseManager
import org.springframework.stereotype.Component

@Component
class RoleDeleteListener(
    private val rsvpService: RsvpService,
) : EventListener<RoleDeleteEvent> {

    override suspend fun handle(event: RoleDeleteEvent) {
        rsvpService.removeRoleForAll(event.guildId, event.roleId)

        DatabaseManager.getSettings(event.guildId)
            .filter { !"everyone".equals(it.controlRole, true) }
            .filter { event.roleId == Snowflake.of(it.controlRole) }
            .doOnNext { it.controlRole = "everyone" }
            .flatMap(DatabaseManager::updateSettings)
            .awaitSingleOrNull()
    }
}
