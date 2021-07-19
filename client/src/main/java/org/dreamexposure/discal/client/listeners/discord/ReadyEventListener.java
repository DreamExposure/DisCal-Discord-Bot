package org.dreamexposure.discal.client.listeners.discord;

import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.rest.util.Image;
import org.dreamexposure.discal.client.message.Messages;
import org.dreamexposure.discal.core.logger.LogFeed;
import org.dreamexposure.discal.core.logger.object.LogObject;
import org.dreamexposure.discal.core.utils.GlobalVal;
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
    public static Mono<Void> handle(final ReadyEvent event) {
        return event.getClient().getApplicationInfo()
            .doOnNext(info -> GlobalVal.setIconUrl(info.getIconUrl(Image.Format.PNG).get()))
            .doOnNext(info ->
                LogFeed.log(LogObject.forDebug("[ReadyEvent]",
                    "Connection success! Session ID: " + event.getSessionId()))
            )
            .doOnNext(info -> LogFeed.log(LogObject.forStatus("Ready Event Success!")))
            .then(Messages.reloadLangs())
            .onErrorResume(e -> {
                LogFeed.log(LogObject.forException("BAD!!!!!1!!", e, ReadyEventListener.class));
                return Mono.empty();
            })
            .then();

    }
}
