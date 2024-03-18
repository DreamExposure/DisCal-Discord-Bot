package org.dreamexposure.discal.client.listeners.discord

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.dreamexposure.discal.client.commands.SlashCommand
import org.dreamexposure.discal.core.business.MetricService
import org.dreamexposure.discal.core.database.DatabaseManager
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.utils.GlobalVal.DEFAULT
import org.dreamexposure.discal.core.utils.getCommonMsg
import org.springframework.stereotype.Component
import org.springframework.util.StopWatch
import java.util.*

@Component
class SlashCommandListener(
    private val commands: List<SlashCommand>,
    private val metricService: MetricService,
) : EventListener<ChatInputInteractionEvent> {

    override suspend fun handle(event: ChatInputInteractionEvent) {
        val timer = StopWatch()
        timer.start()

        if (!event.interaction.guildId.isPresent) {
            event.reply(getCommonMsg("error.dm.not-supported", Locale.ENGLISH)).awaitSingleOrNull()
            return
        }

        val command = commands.firstOrNull { it.name == event.commandName }
        val subCommand = if (command?.hasSubcommands == true) event.options[0].name else null

        if (command != null) {
            event.deferReply().withEphemeral(command.ephemeral).awaitSingleOrNull()

            try {
                val settings = DatabaseManager.getSettings(event.interaction.guildId.get()).awaitSingle()

                command.suspendHandle(event, settings)
            } catch (e: Exception) {
                LOGGER.error(DEFAULT, "Error handling slash command | $event", e)

                // Attempt to provide a message if there's an unhandled exception
                event.createFollowup(getCommonMsg("error.unknown", Locale.ENGLISH))
                    .withEphemeral(command.ephemeral)
                    .awaitSingleOrNull()
            }
        } else {
            event.createFollowup(getCommonMsg("error.unknown", Locale.ENGLISH))
                .withEphemeral(true)
                .awaitSingleOrNull()
        }

        timer.stop()

        val computedInteractionName = if (subCommand != null) "/${event.commandName}#$subCommand" else "/${event.commandName}"
        metricService.recordInteractionDuration(computedInteractionName, "chat-input", timer.totalTimeMillis)
    }
}
