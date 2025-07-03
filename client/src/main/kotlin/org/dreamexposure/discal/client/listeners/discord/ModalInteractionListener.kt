package org.dreamexposure.discal.client.listeners.discord

import discord4j.core.event.domain.interaction.ModalSubmitInteractionEvent
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
class ModalInteractionListener(
    private val settingsService: GuildSettingsService,
    private val modals: List<InteractionHandler<ModalSubmitInteractionEvent>>,
    private val metricService: MetricService,
) : EventListener<ModalSubmitInteractionEvent> {

    override suspend fun handle(event: ModalSubmitInteractionEvent) {
        val timer = StopWatch()
        timer.start()

        if (!event.interaction.guildId.isPresent) {
            event.reply(getCommonMsg("error.dm.not-supported", Locale.ENGLISH)).awaitSingleOrNull()
            return
        }

        val modal = modals.firstOrNull { it.ids.any(event.customId::startsWith) }

        if (modal != null) {
            try {
                if (modal.shouldDefer(event)) event.deferReply().withEphemeral(modal.ephemeral).awaitSingleOrNull()

                modal.handle(event, settingsService.getSettings(event.interaction.guildId.get()))
            } catch (e: Exception) {
                LOGGER.error(DEFAULT, "Error handling modal interaction | ${event.customId} | $event", e)

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
        metricService.recordInteractionDuration(modal?.ids?.joinToString("|") ?: event.customId, "button", timer.totalTimeMillis)
    }
}