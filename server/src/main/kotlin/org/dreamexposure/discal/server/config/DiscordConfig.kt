package org.dreamexposure.discal.server.config

import discord4j.core.DiscordClient
import discord4j.core.DiscordClientBuilder
import org.dreamexposure.discal.core.config.Config
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DiscordConfig {
    @Bean
    fun discordClient(): DiscordClient {
        return DiscordClientBuilder.create(Config.SECRET_BOT_TOKEN.getString()).build()
    }
}
