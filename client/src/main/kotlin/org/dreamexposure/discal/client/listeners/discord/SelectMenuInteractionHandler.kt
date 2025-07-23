package org.dreamexposure.discal.client.listeners.discord

import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent
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
class SelectMenuInteractionListener(
    private val settingsService: GuildSettingsService,
    private val dropdowns: List<InteractionHandler<SelectMenuInteractionEvent>>,
    private val metricService: MetricService,
) : EventListener<SelectMenuInteractionEvent> {

    override suspend fun handle(event: SelectMenuInteractionEvent) {
        val timer = StopWatch()
        timer.start()

        if (!event.interaction.guildId.isPresent) {
            event.reply(getCommonMsg("error.dm.not-supported", Locale.ENGLISH)).awaitSingleOrNull()
            return
        }

        val dropdown = dropdowns.firstOrNull { it.ids.any(event.customId::startsWith) }

        if (dropdown != null) {
            if (dropdown.shouldDefer(event)) event.deferReply().withEphemeral(dropdown.ephemeral).awaitSingleOrNull()

            try {
                dropdown.handle(event, settingsService.getSettings(event.interaction.guildId.get()))
            } catch (e: Exception) {
                LOGGER.error(DEFAULT, "Error handling select menu interaction | id:${event.customId} | $event", e)

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
        metricService.recordInteractionDuration(dropdown?.ids?.joinToString("|") ?: event.customId, "select-menu", timer.totalTimeMillis)
    }
}