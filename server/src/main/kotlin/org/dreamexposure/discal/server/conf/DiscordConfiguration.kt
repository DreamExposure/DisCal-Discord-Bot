package org.dreamexposure.discal.server.conf

import discord4j.core.DiscordClient
import discord4j.core.DiscordClientBuilder
import org.dreamexposure.discal.core.`object`.BotSettings
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DiscordConfiguration {

    @Bean
    fun discordClient(): DiscordClient {
        return DiscordClientBuilder.create(BotSettings.TOKEN.get()).build()
    }
}
