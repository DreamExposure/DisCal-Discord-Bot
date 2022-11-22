package org.dreamexposure.discal.client.service

import discord4j.core.GatewayDiscordClient
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.utils.GlobalVal.STATUS
import org.springframework.stereotype.Component
import javax.annotation.PreDestroy

@Component
class ShutdownHook(private val discordClient: GatewayDiscordClient) {
    @PreDestroy
    fun onShutdown() {
        LOGGER.info(STATUS, "Shutting down shard")

        discordClient.logout().subscribe()
    }
}
