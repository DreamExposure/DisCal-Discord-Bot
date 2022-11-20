package org.dreamexposure.discal.client.listeners.discord

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.dreamexposure.discal.client.commands.SlashCommand
import org.dreamexposure.discal.core.database.DatabaseManager
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.utils.GlobalVal.DEFAULT
import org.springframework.stereotype.Component

@Component
class SlashCommandListener(
    private val commands: List<SlashCommand>
) : EventListener<ChatInputInteractionEvent> {

    override suspend fun handle(event: ChatInputInteractionEvent) {
        if (!event.interaction.guildId.isPresent) {
            event.reply("Commands not supported in DMs.").awaitSingleOrNull()
            return
        }

        val command = commands.firstOrNull { it.name == event.commandName }

        if (command != null) {
            event.deferReply().withEphemeral(command.ephemeral).awaitSingleOrNull()

            try {
                val settings = DatabaseManager.getSettings(event.interaction.guildId.get()).awaitSingle()
                command.handle(event, settings).awaitSingleOrNull()
            } catch (e: Exception) {
                LOGGER.error(DEFAULT, "Error handling slash command | $event", e)

                // Attempt to provide a message if there's an unhandled exception
                event.createFollowup("An unknown error has occurred")
                    .withEphemeral(command.ephemeral)
                    .awaitSingleOrNull()
            }
        } else {
            event.createFollowup("An unknown error has occurred. Please try again and/or contact DisCal support.")
                .withEphemeral(true)
                .awaitSingleOrNull()
        }

    }
}
