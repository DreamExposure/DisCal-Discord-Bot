package org.dreamexposure.discal.client.listeners.discord

import discord4j.core.event.domain.lifecycle.ReadyEvent
import discord4j.rest.util.Image
import org.dreamexposure.discal.client.message.Messages
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.utils.GlobalVal.DEFAULT
import org.dreamexposure.discal.core.utils.GlobalVal.STATUS
import org.dreamexposure.discal.core.utils.GlobalVal.iconUrl
import reactor.core.publisher.Mono

object ReadyEventListener {
    fun handle(event: ReadyEvent): Mono<Void> {
        return event.client.applicationInfo
              .doOnNext { iconUrl = it.getIconUrl(Image.Format.PNG).get() }
              .doOnNext { LOGGER.info(STATUS, "Ready event success!") }
              .then(Messages.reloadLangs())
              .onErrorResume {
                  LOGGER.error(DEFAULT, "Failed to handle ready event")
                  Mono.empty()
              }.then()
    }
}
