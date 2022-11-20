package org.dreamexposure.discal.client.listeners.discord

import discord4j.core.event.domain.lifecycle.ReadyEvent
import discord4j.rest.util.Image
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.dreamexposure.discal.client.message.Messages
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.utils.GlobalVal.DEFAULT
import org.dreamexposure.discal.core.utils.GlobalVal.STATUS
import org.dreamexposure.discal.core.utils.GlobalVal.iconUrl
import org.springframework.stereotype.Component

@Component
class ReadyEventListener : EventListener<ReadyEvent> {
    override suspend fun handle(event: ReadyEvent) {
        try {
            iconUrl = event.client.applicationInfo
                .map { it.getIconUrl(Image.Format.PNG).orElse("") }
                .awaitSingle()

            Messages.reloadLangs().awaitSingleOrNull()

            LOGGER.info(STATUS, "Ready event success!")
        } catch (e: Exception) {
            LOGGER.error(DEFAULT, "Failed to handle ready event", e)
        }
    }
}
