package org.dreamexposure.discal.client.listeners.discord

import discord4j.core.event.domain.interaction.ButtonInteractionEvent
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.dreamexposure.discal.client.interaction.InteractionHandler
import org.dreamexposure.discal.core.business.GuildSettingsService
import org.dreamexposure.discal.core.business.MetricService
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.utils.GlobalVal.DEFAULT
import org.dreamexposure.discal.core.utils.getCommonMsg
import org.springframework.stereotype.Component
import org.springframework.util.StopWatch
import java.util.*

@Component
class ButtonInteractionListener(
    private val buttons: List<InteractionHandler<ButtonInteractionEvent>>,
    private val settingsService: GuildSettingsService,
    private val metricService: MetricService,
): EventListener<ButtonInteractionEvent> {
    override suspend fun handle(event: ButtonInteractionEvent) {
        val timer = StopWatch()
        timer.start()

        if (!event.interaction.guildId.isPresent) {
            event.reply(getCommonMsg("error.dm.not-supported", Locale.ENGLISH))
            return
        }

        val button = buttons.firstOrNull { it.ids.any(event.customId::startsWith) }

        if (button != null) {
            try {
                val settings = settingsService.getSettings(event.interaction.guildId.get())

                button.handle(event, settings)
            } catch (e: Exception) {
                LOGGER.error(DEFAULT, "Error handling button interaction | #${event.customId} | $event", e)

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
        metricService.recordInteractionDuration(button?.ids?.joinToString("|") ?: event.customId, "button", timer.totalTimeMillis)
    }
}
