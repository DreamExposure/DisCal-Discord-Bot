package org.dreamexposure.discal.client.listeners.discord;

import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.rest.util.Image;
import org.dreamexposure.discal.client.message.Messages;
import org.dreamexposure.discal.core.utils.GlobalVal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

/**
 * @author NovaFox161
 * Date Created: 9/8/2018
 * For Project: DisCal-Discord-Bot
 * Author Website: https://www.novamaday.com
 * Company Website: https://www.dreamexposure.org
 * Contact: nova@dreamexposure.org
 */
@SuppressWarnings("OptionalGetWithoutIsPresent")
public class ReadyEventListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReadyEventListener.class);

    public static Mono<Void> handle(final ReadyEvent event) {
        return event.getClient().getApplicationInfo()
            .doOnNext(info -> GlobalVal.setIconUrl(info.getIconUrl(Image.Format.PNG).get()))
            .doOnNext(info -> LOGGER.info(GlobalVal.getSTATUS(), "Ready event success!"))
            .then(Messages.reloadLangs())
            .onErrorResume(e -> {
                LOGGER.error(GlobalVal.getDEFAULT(), "Failed to handle ready event");
                return Mono.empty();
            })
            .then();

    }
}
