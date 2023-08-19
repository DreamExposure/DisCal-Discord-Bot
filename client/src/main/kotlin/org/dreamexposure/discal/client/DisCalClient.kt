package org.dreamexposure.discal.client

import discord4j.core.GatewayDiscordClient
import jakarta.annotation.PreDestroy
import org.dreamexposure.discal.Application
import org.dreamexposure.discal.client.message.Messages
import org.dreamexposure.discal.core.config.Config
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.utils.GlobalVal
import org.dreamexposure.discal.core.utils.GlobalVal.DEFAULT
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.stereotype.Component
import kotlin.system.exitProcess

@Component
class DisCalClient {
    
    @PreDestroy
    fun onShutdown(gatewayDiscordClient: GatewayDiscordClient) {
        LOGGER.info(GlobalVal.STATUS, "Shutting down shard")

        gatewayDiscordClient.logout().subscribe()
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            Config.init()

            //Load lang files
            Messages.reloadLangs().subscribe()

            //Start Spring
            try {
                SpringApplicationBuilder(Application::class.java).run(*args)
            } catch (e: Exception) {
                LOGGER.error(DEFAULT, "Spring error!", e)
                exitProcess(4)
            }
        }
    }
}

