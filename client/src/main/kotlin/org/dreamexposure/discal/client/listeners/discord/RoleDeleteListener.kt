package org.dreamexposure.discal.client.listeners.discord

import discord4j.core.event.domain.role.RoleDeleteEvent
import org.dreamexposure.discal.core.business.GuildSettingsService
import org.dreamexposure.discal.core.business.RsvpService
import org.springframework.stereotype.Component

@Component
class RoleDeleteListener(
    private val rsvpService: RsvpService,
    private val settingsService: GuildSettingsService,
) : EventListener<RoleDeleteEvent> {

    override suspend fun handle(event: RoleDeleteEvent) {
        rsvpService.removeRoleForAll(event.guildId, event.roleId)

        val settings = settingsService.getSettings(event.guildId)
        if (settings.controlRole != null && event.roleId == settings.controlRole) {
            settingsService.upsertSettings(settings.copy(controlRole = null))
        }
    }
}
