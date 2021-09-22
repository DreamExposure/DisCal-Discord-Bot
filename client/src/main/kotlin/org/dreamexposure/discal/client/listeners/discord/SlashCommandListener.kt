package org.dreamexposure.discal.client.listeners.discord

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import org.dreamexposure.discal.client.commands.SlashCommand
import org.dreamexposure.discal.core.database.DatabaseManager
import org.dreamexposure.discal.core.logger.LOGGER
import org.springframework.context.ApplicationContext
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class SlashCommandListener(applicationContext: ApplicationContext) {
    private val cmds = applicationContext.getBeansOfType(SlashCommand::class.java).values

    fun handle(event: ChatInputInteractionEvent): Mono<Void> {
        if (!event.interaction.guildId.isPresent) {
            return event.reply("Commands not supported in DMs.")
        }

        return Flux.fromIterable(cmds)
                .filter { it.name == event.commandName }
                .next()
                .flatMap { command ->
                    val mono =
                            if (command.ephemeral) event.acknowledgeEphemeral()
                            else event.acknowledge()

                    mono.then(DatabaseManager.getSettings(event.interaction.guildId.get()))
                            .flatMap { command.handle(event, it) }
                }.doOnError {
                    LOGGER.error("Unhandled slash command error", it)
                }.onErrorResume { Mono.empty() }
    }
}
