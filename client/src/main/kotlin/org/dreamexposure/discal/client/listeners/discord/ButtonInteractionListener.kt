package org.dreamexposure.discal.client.listeners.discord

import discord4j.core.event.domain.interaction.ButtonInteractionEvent
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.dreamexposure.discal.client.interaction.InteractionHandler
import org.dreamexposure.discal.core.business.MetricService
import org.dreamexposure.discal.core.database.DatabaseManager
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.utils.GlobalVal.DEFAULT
import org.dreamexposure.discal.core.utils.getCommonMsg
import org.springframework.stereotype.Component
import org.springframework.util.StopWatch
import java.util.*

@Component
class ButtonInteractionListener(
    private val buttons: List<InteractionHandler<ButtonInteractionEvent>>,
    private val metricService: MetricService,
): EventListener<ButtonInteractionEvent> {
    override suspend fun handle(event: ButtonInteractionEvent) {
        val timer = StopWatch()
        timer.start()

        if (!event.interaction.guildId.isPresent) {
            event.reply(getCommonMsg("error.dm.not-supported", Locale.ENGLISH))
            return
        }

        val button = buttons.firstOrNull { it.ids.contains(event.customId) }

        if (button != null) {
            try {
                val settings = DatabaseManager.getSettings(event.interaction.guildId.get()).awaitSingle()

                button.handle(event, settings)
            } catch (e: Exception) {
                LOGGER.error(DEFAULT, "Error handling button interaction | $event", e)

                // Attempt to provide a message if there's an unhandled exception
                event.createFollowup(getCommonMsg("error.unknown", Locale.ENGLISH))
                    .withEphemeral(true)
                    .awaitSingleOrNull()
            }
        } else {
            event.createFollowup(getCommonMsg("error.unknown", Locale.ENGLISH))
                .withEphemeral(true)
                .awaitSingleOrNull()
        }

        timer.stop()
        metricService.recordInteractionDuration(event.customId, "button", timer.totalTimeMillis)
    }
}
