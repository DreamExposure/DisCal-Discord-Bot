package org.dreamexposure.discal.client.interaction

import discord4j.core.event.domain.interaction.ButtonInteractionEvent
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.dreamexposure.discal.core.business.StaticMessageService
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.`object`.new.GuildSettings
import org.dreamexposure.discal.core.utils.getCommonMsg
import org.springframework.stereotype.Component

@Component
class StaticMessageRefreshButton(
    private val staticMessageService: StaticMessageService,
): InteractionHandler<ButtonInteractionEvent> {
    override val ids = arrayOf("refresh-static-message")
    override val ephemeral = true

    override suspend fun handle(event: ButtonInteractionEvent, settings: GuildSettings) {
        try {
            // Defer, this process can take a bit
            event.deferEdit()
                .withEphemeral(ephemeral)
                .awaitSingleOrNull()

            staticMessageService.updateStaticMessage(settings.guildId, event.messageId)
        } catch (ex: Exception) {
            LOGGER.error("Error handling static message refresh button | guildId:${settings.guildId.asLong()} | messageId: ${event.messageId.asLong()}", ex)

            event.createFollowup(getCommonMsg("error.unknown", settings.locale))
                .withEphemeral(ephemeral)
                .awaitSingleOrNull()
        }
    }
}
