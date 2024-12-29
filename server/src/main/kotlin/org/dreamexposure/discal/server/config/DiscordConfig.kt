package org.dreamexposure.discal.server.config

import discord4j.common.ReactorResources
import discord4j.core.DiscordClient
import discord4j.core.DiscordClientBuilder
import org.dreamexposure.discal.core.config.Config
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DiscordConfig {
    @Bean
    fun discordClient(): DiscordClient {
        return DiscordClientBuilder.create(Config.SECRET_BOT_TOKEN.getString())
            .setReactorResources(ReactorResources.builder()
                .httpClient(ReactorResources.DEFAULT_HTTP_CLIENT.get().metrics(true) { s -> s })
                .build()
            ).build()
    }
}
