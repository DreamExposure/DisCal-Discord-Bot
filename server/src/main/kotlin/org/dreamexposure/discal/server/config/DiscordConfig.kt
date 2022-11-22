package org.dreamexposure.discal.server.config

import discord4j.core.DiscordClient
import discord4j.core.DiscordClientBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DiscordConfig {
    @Bean
    fun discordClient(@Value("\${bot.secret.token}") token: String): DiscordClient {
        return DiscordClientBuilder.create(token).build()
    }
}
