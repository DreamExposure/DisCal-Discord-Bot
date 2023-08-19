package org.dreamexposure.discal.client.service

import discord4j.core.GatewayDiscordClient
import jakarta.annotation.PreDestroy
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.utils.GlobalVal.STATUS
import org.springframework.stereotype.Component

// Required to be a component for shutdown hook due to lifecycle management of the discord client
@Component
class ShutdownHook(private val discordClient: GatewayDiscordClient) {
    @PreDestroy
    fun onShutdown() {
        LOGGER.info(STATUS, "Shutting down shard")

        discordClient.logout().subscribe()
    }
}
